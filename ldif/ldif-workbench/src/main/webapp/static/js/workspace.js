/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var tooltips = {
    "label":"label",
    "description":"description",
    "homepage":"homepage",
    "internalId":"A unique ID, which will be used internally to track the import job and its files like data and provenance.",
    "dataSource":"A reference to a data source to state from which source this job imports data.",
    "refreshSchedule":"Specify how often the integration is expected to be run.",
    "dumpLocation":"dumpLocation",
    "endpointLocation":"endpointLocation",
    "tripleLimit":"tripleLimit",
    "pattern":"pattern",
    "uri":"uri",
    "levels":"The maximum distance to one of the seed URIs.",
    "resourceLimit":"the maximum number of resources to crawl",
    "linkSpecifications":"Specify a directory containing the Silk link specifications.",
    "mappings":"Specify a directory containing the R2R mappings.",
    "output":"Specify the name of the file to which the output should be written.",
    "runSchedule":"Specify how often the integration is expected to be run.",
    "output2":"Specify if all input quads from the input should be included in the output file or only the quads that were mapped/translated by LDIF",
    "rewriteURIs":"Specify if URI aliases in the input data should be rewritten to a single target URI in the output data.",
    "provenanceGraphURI":"Specify the graph containing the provenance information. As of LDIF V0.3.1, Quads from this graph are only written to the final output data set and are not processed any further in the integration workflow.",
    "validateSources":"Source data sets, R2R mappings and Silk link specifications are all validated before starting with the actual integration. Since the syntax validation of the sources (N-Triples / N-Quads files) takes some time (about 15s/GB), if you already know that they are correct, it is possible to disable this step by setting the property to false.",
    "discardFaultyQuads":"If LDIF finds a (syntax) error - like spaces in URIs - in the source data, it does not progress with the integration to give you the opportunity to fix these errors first. However, sometimes you just don't care that some quads are faulty and want them to be ignored instead, so that the overall integration can still proceed. Set this property to true in order to ignore syntax errors in the source data sets.",
    "useExternalSameAsLinks ":"Besides discovering equal entities in the identity resolution phase, LDIF also offers the opportunity to input these relationships in form of owl:sameAs links. The NT/N-Quads file with these sameAs-links has to be placed in the source directory with the other data sets. If you don’t want to use sameAs-links from the input data, set this property to false.",
    "outputFormat":"Although the default output format is N-Quads, LDIF also offers a triple output as N-Triple.",
    "uriMinting":"Specify if output resources should be given an URI within the target namespace.",
    "uriMintNamespace":"Specify the namespace into which the URIs of all output resources are translated, if URI minting is enabled.",
    "oneTimeExecution":"If true the scheduler executes all the Jobs at most once. Import Jobs are evaluated first and then (as all of these have finished) the integration job starts.",
    "type":"Different mechanisms to import external data.",
    "useExternalSameAsLinks":"Besides discovering equal entities in the identity resolution phase, LDIF also offers the opportunity to input these relationships in form of owl:sameAs links. The NT/N-Quads file with these sameAs-links has to be placed in the source directory with the other data sets. If you don’t want to use sameAs-links from the input data, set this property to false.",
    "sparql":"sparql",
    "seed":"seeds",
    "predicate":"predicates",
    "mlpredicate":"A list of property URIs, which will be used to expand the name space URI specified with uriMintNamespace. For each entity one value of the specified URIs is used to act as the local part of the minted URI. If there are many values to pick from, the max value (lexicographic order) is taken. If no value could be found for any of the properties, the URI of the entity is not minted. Note that there is no way to prevent name clashes at the moment.",
    "properties":"The path to a Java properties file for configuration parameters.",
    "integrationJob":"A document containing the Integration Job configurations."
};


// -- init
$(document).ready(
    function(){
        initLoadingDialog();
    }
);

$(function () {

     var workspaceObj = workspaceVar;

    // Tooltip events
    $(".help").mouseover(function() {
        var offset = $(this).offset();
        var width = $(this).width();
        var tooltip = $(this).prev().attr('name');
        if (!tooltip) tooltip = $(this).attr('id');
        var text =  eval('tooltips.'+tooltip);
        if (text !== undefined && text != "")
            Tip(text, FIX, [offset.left+width+8, offset.top]);
    });
    $(".help").mouseout(function() {
        UnTip();
    });

//    $("form").submit(function() {
//        return false;
//    });

    // Error messages wrapper
    $(".dialog").prepend('<div class="error description ui-state-error ui-corner-all" style="display: none"><span class="error-icon"></span><div class="message"></div></div>');

    $("#new-project-form").dialog({
                title: '<span class="title-icon new-project-icon"></span>New scheduler',
                autoOpen: false,
                height: 320,
                width: 520,
                modal: true,
                resizable: false,
                buttons: {
                    "Create": function() {
                        var selectedTab = $("#new-project-form").tabs("option","selected");
                        resetForm('new-project-form', false);
                        var errors = new Array();
                        var fields = new Array();
                        var name = $("#new-project-form-tabs-1 > form > input[name='name']").val();
                        var nameImp = $("#new-project-form-tabs-2 > form > input[name='name']").val();
                        var file = $("#new-project-form-tabs-2 > form > input[name='file']").val();

                        if (selectedTab == 1) {
                            if (!file) {
                                errors.push('No file chosen.<br/>'); fields.push('file');
                            }
                            if (!nameImp) {
                                errors.push('Scheduler name is empty.'); fields.push('name');
                            }
                            if (nameImp.search(/[^a-zA-Z0-9_.-]+/) !== -1) {
                                errors.push('Scheduler name may only contain the following characters (a - z, A - Z, 0 - 9, ., _, -).<br/>'); fields.push('name');
                            }
                            for (var p in workspaceObj.workspace.project) {
                                var project_name = workspaceObj.workspace.project[p].name;
                                if (nameImp == project_name) {
                                    errors.push("A scheduler the the name '" + name + "' already exists.<br/>"); fields.push('name');
                                }
                            }
                        } else {
                            if (!name) {
                                errors.push('Scheduler name  is empty.'); fields.push('name');
                            }
                            if (name.search(/[^a-zA-Z0-9_.-]+/) !== -1) {
                                errors.push('Scheduler name may only contain the following characters (a - z, A - Z, 0 - 9, ., _, -).<br/>'); fields.push('name');
                            }
                            for (var p in workspaceObj.workspace.project) {
                                var project_name = workspaceObj.workspace.project[p].name;
                                if (name == project_name) {
                                    errors.push("A scheduler with the name '" + name + "' already exists.<br/>"); fields.push('name');
                                }
                            }
                        }

                        if (errors.length > 0) {
                            showErrors('new-project-form', errors, fields);
                        } else {
                            if (selectedTab == 1) {
                                importProject(name, file);
                            } else {
                                createProject(name);
                            }
                        }
                        $(this).dialog("close");
                        loadingShow;
                    },
                    Cancel: function() {
                        $(this).dialog("close");
                    }
                }
    });
    $("#data-source-form").dialog({
                title: '<span class="title-icon source-icon"></span>Data source',
                autoOpen: false,
                height: 460,
                width: 520,
                modal: true,
                resizable: false,
                buttons: {
                    "Save": function() {
                        var selectedTab = $("#data-source-form").tabs("option","selected");
                        resetForm('data-source-form', false);
                        var dataSourceName;
                        var errors = new Array();
                        var fields = new Array();
                        var projectName = $("#data-source-form-tabs-1 > form > input[name='projectName']").val();
                        var projectIndex = $("#data-source-form-tabs-1 > form > input[name='projectIndex']").val();
                        var dataSourceIndex = $("#data-source-form-tabs-1 > form > input[name='dataSourceIndex']").val();
                        var label = $("#data-source-form-tabs-1 > form > input[name='label']").val();
                        var description = $("#data-source-form-tabs-1 > form > input[name='description']").val();
                        var homepage = $("#data-source-form-tabs-1 > form > input[name='homepage']").val();
                        var file = $("#data-source-form-tabs-2 > form > input[name='file']").val();

                        if (selectedTab == 1) {
                            if (!file) {
                                errors.push('No file chosen.<br/>'); fields.push('file');
                            }
                        } else {
                            if (!label) {
                                errors.push('Label value is empty.<br/>'); fields.push('label');
                            }
                            if (label.search(/[^a-zA-Z0-9_]+/) !== -1) {
                                errors.push('Label identifier may only contain the following characters: (a - z, A - Z, 0 - 9, _).<br/>'); fields.push('label');
                            }
                            for (var d in workspaceObj.workspace.project[projectIndex].scheduler.dataSources) {
                                var datasource_name = workspaceObj.workspace.project[projectIndex].scheduler.dataSources[d].label;
                                if (dataSourceIndex !== d && label == datasource_name) {
                                    errors.push("A data source with label '" + label + "' already exists in "+projectName+".<br/>"); fields.push('label');
                                }
                            }
                        }

                        if (errors.length > 0) {
                            showErrors('data-source-form', errors, fields);
                        } else {
                            var xml = $('<dataSource></dataSource>');
                            xml.append($('<label>'+label+'</label>'));
                            if (description)
                                xml.append($('<description>'+description+'</description>'));
                            if (homepage)
                                xml.append($('<homepage>'+homepage+'</homepage>'));

//                            if (dataSourceIndex != '')
//                                dataSourceName = workspaceObj.workspace.project[projectIndex].scheduler.dataSources[dataSourceIndex].label;

                            if (selectedTab == 1) {
                                // TODO
                                importDataSource(projectName, file);
                            } else {
                                dataSourceName = label;
                                var xmlString = '<dataSource>'+$(xml).html()+'</dataSource>'
                                saveDataSource(dataSourceName, xmlString);
                            }
                            $(this).dialog("close");
                            loadingShow;
                        }
                    },
                    Cancel: function() {
                        $(this).dialog("close");
                    }
                }
    });
    $("#import-job-form").dialog({
                title: '<span class="title-icon importjob-icon"></span>Import job',
                autoOpen: false,
                height: 540,
                width: 580,
                modal: true,
                resizable: false,
                buttons: {
                    "Save": function() {
                        var selectedTab = $("#import-job-form").tabs("option","selected");
                        resetForm('import-job-form', false);
                        var importJobName;
                        var sparqlPatterns = new Array();
                        var seedUris = new Array();
                        var predicates = new Array();
                        var errors = new Array();
                        var fields = new Array();
                        var projectName = $("#import-job-form-tabs-1 > form > input[name='projectName']").val();
                        var projectIndex = $("#import-job-form-tabs-1 > form > input[name='projectIndex']").val();
                        var importJobIndex = $("#import-job-form-tabs-1 > form > input[name='importJobIndex']").val();
                        var internalId = $("#import-job-form-tabs-1 > form > input[name='internalId']").val();

                        var dataSource = $("#import-job-form-tabs-1 > form > select[name='dataSource']").val();
                        var refreshSchedule = $("#import-job-form-tabs-1 > form > select[name='refreshSchedule']").val();
                        var type = $("#import-job-form-tabs-1 > form > select[name='type']").val();

                        var dumpLocationQuad = $("#import-job-form-tabs-1 > form > #quadImportJob > input[name='dumpLocation']").val();
                        var dumpLocationTriple = $("#import-job-form-tabs-1 > form > #tripleImportJob > input[name='dumpLocation']").val();

                        var endpointLocation = $("#import-job-form-tabs-1 > form > #sparqlImportJob > input[name='endpointLocation']").val();
                        var tripleLimit = $("#import-job-form-tabs-1 > form > #sparqlImportJob > input[name='tripleLimit']").val();

                        var levels = $("#import-job-form-tabs-1 > form > #crawlImportJob > input[name='levels']").val();
                        var resourceLimit = $("#import-job-form-tabs-1 > form > #crawlImportJob > input[name='resourceLimit']").val();

                        var file = $("#import-job-form-tabs-2 > form > input[type='file']").val();

                        $("#import-job-form-tabs-1 > form > #sparqlImportJob > #patterns > div").each(function() {
                            if ($(this).children("input").val() != '')
                                sparqlPatterns.push($(this).children("input").val());
                        });
                        $("#import-job-form-tabs-1 > form > #crawlImportJob > #seeds > div").each(function() {
                            if ($(this).children("input").val() != '')
                                seedUris.push($(this).children("input").val());
                        });
                        $("#import-job-form-tabs-1 > form > #crawlImportJob > #predicates > div").each(function() {
                            if ($(this).children("input").val() != '')
                                predicates.push($(this).children("input").val());
                        });

                        if (selectedTab == 1) {
                            if (!file) {
                                errors.push('No file chosen.<br/>'); fields.push('file');
                            }
                        } else {
                            if (internalId.search(/[^a-zA-Z0-9_.]+/) !== -1) {
                                errors.push('internalId may only contain the following characters (a - z, A - Z, 0 - 9, ., _).<br/>'); fields.push('internalId');
                            }
                            for (var d in workspaceObj.workspace.project[projectIndex].scheduler.importJobs) {
                                var importjob_name = workspaceObj.workspace.project[projectIndex].scheduler.importJobs[d].internalId;
                                if (importJobIndex !== d && internalId == importjob_name) {
                                    errors.push("A Import job with internalId '" + internalId + "' already exists in "+projectName+".<br/>"); fields.push('internalId');
                                }
                            }
                            if (type == 'quadImportJob' && !dumpLocationQuad) {
                                errors.push('Dump location is empty.<br/>'); fields.push('dumpLocation');
                            }
                            if (type == 'tripleImportJob' && !dumpLocationTriple) {
                                errors.push('Dump location is empty.<br/>'); fields.push('dumpLocation');
                            }
                            if (type == 'sparqlImportJob' && !endpointLocation) {
                                errors.push('Endpoint location is empty.<br/>'); fields.push('endpointLocation');
                            }
                            if (type == 'sparqlImportJob' && tripleLimit != '' && isNaN(tripleLimit)) {
                                errors.push('Triple limit must be a number.<br/>'); fields.push('tripleLimit');
                            }
                            if (type == 'crawlImportJob' && seedUris.length < 1) {
                                errors.push('At least one Seed URI must be set.<br/>'); fields.push('seed');
                            }
                            if (type == 'crawlImportJob' && levels != '' && isNaN(levels)) {
                                errors.push('Levels must be a number.<br/>'); fields.push('levels');
                            }
                            if (type == 'crawlImportJob' && resourceLimit != '' && isNaN(resourceLimit)) {
                                errors.push('Resource limit must be a number.<br/>'); fields.push('resourceLimit');
                            }
                        }

                        if (errors.length > 0) {
                            showErrors('import-job-form', errors, fields);
                        } else {
                            var xml = $('<importJob ></importJob >');
                            if (internalId)
                                xml.append($('<internalId>'+internalId+'</internalId>'));
                            if (dataSource != null) {
                                xml.append($('<dataSource>'+dataSource+'</dataSource>'));
                            }
                            xml.append($('<refreshSchedule>'+refreshSchedule+'</refreshSchedule>'));

                            var importType = $('<'+type+'></'+type+' >');

                            if (type == 'quadImportJob') {
                                importType.append($('<dumpLocation>'+dumpLocationQuad+'</dumpLocation>'));
                            }
                            if (type == 'tripleImportJob') {
                                importType.append($('<dumpLocation>'+dumpLocationTriple+'</dumpLocation>'));
                            }
                            if (type == 'sparqlImportJob') {
                                importType.append($('<endpointLocation>'+endpointLocation+'</endpointLocation>'));
                                if (tripleLimit != '') importType.append($('<tripleLimit>'+tripleLimit+'</tripleLimit>'));
                                var sPatterns = $('<sparqlPatterns></sparqlPatterns>');
                                for (var i in sparqlPatterns) {
                                    if (sparqlPatterns[i] != '') sPatterns.append($('<pattern>'+sparqlPatterns[i]+'</pattern>'));
                                }
                                importType.append(sPatterns);
                            }
                            if (type == 'crawlImportJob') {
                                if (seedUris.length > 0) {
                                    var sUris = $('<seedURIs></seedURIs>');
                                    for (var i in seedUris) {
                                        if (seedUris[i] != '') sUris.append($('<uri>'+seedUris[i]+'</uri>'));
                                    }
                                    importType.append(sUris);
                                }
                                if (predicates.length > 0) {
                                    var pr = $('<predicatesToFollow></predicatesToFollow>');
                                    for (var i in predicates) {
                                        if (predicates[i] != '') pr.append($('<uri>'+sparqlPatterns[i]+'</uri>'));
                                    }
                                    importType.append(pr);
                                }
                                if (levels != '') importType.append($('<levels>'+levels+'</levels>'));
                                if (resourceLimit != '') importType.append($('<resourceLimit>'+resourceLimit+'</resourceLimit>'));
                            }
                            xml.append(importType);

                            //if (importJobIndex != '')
                            //    importJobName = workspaceObj.workspace.project[projectIndex].scheduler.importJobs[importJobIndex].internalId;

                            if (selectedTab == 1) {
                                console.log("Use ImportImportJobDialog submit button instead.")
                                console.log("TODO: importImportJob("+projectName+","+file+");");
                                //importImportJob(projectName, file,'a');
                                return;
                            } else {
                                importJobName = internalId;
                                var xmlString = '<importJob>'+$(xml).html()+'</importJob>'
                                saveImportJob(importJobName, xmlString);
                            }
                            $(this).dialog("close");
                            loadingShow;
                        }
                    },
                    Cancel: function() {
                        $(this).dialog("close");
                    }
                }
    });
    $("#integration-job-form").dialog({
                title: '<span class="title-icon integrationjob-icon"></span>Integration job',
                autoOpen: false,
                height: 540,
                width: 630,
                modal: true,
                resizable: false,
                buttons: {
                    "Save": function() {
                        var selectedTab = $("#integration-job-form").tabs("option","selected");
                        resetForm('integration-job-form', false);
                        var errors = new Array();
                        var fields = new Array();
                        var projectName = $("#integration-job-form-tabs-1 > form > input[name='projectName']").val();

                        //var properties = $("#integration-job-form > form > input[name='properties']").val();
                        //var sources = $("#integration-job-form > form > input[name='sources']").val();

                        var linkSpecifications = $("#integration-job-form-tabs-1 > form > input[name='linkSpecifications']").val();
                        var mappings = $("#integration-job-form-tabs-1 > form > input[name='mappings']").val();
                        var sieve = $("#integration-job-form-tabs-1 > form > input[name='sieve']").val();
                        var output = $("#integration-job-form-tabs-1 > form > input[name='output']").val();
                        var runSchedule = $("#integration-job-form-tabs-1 > form > select[name='runSchedule']").val();

                        var file = $("#integration-job-form-tabs-2 > form > input[name='file']").val();

                        if (selectedTab == 1) {
                            if (!file) {
                                errors.push('No file chosen.<br/>'); fields.push('file');
                            }
                        } else {
                            //TODO update mandatory fields
                           /* if (!output) {
                                errors.push('Output value is empty.<br/>'); fields.push('output');
                            }
                            if (!linkSpecifications) {
                                errors.push('Link specifications value is empty.<br/>'); fields.push('linkSpecifications');
                            }
                            if (!mappings) {
                                errors.push('Mappings value is empty.<br/>'); fields.push('mappings');
                            }  */
                        }

                        if (errors.length > 0) {
                            showErrors('integration-job-form', errors, fields);
                        } else {
                            var propertiesString = "";
                            $("#integration-job-form-tabs-1 > form > .conf").each(function() {
                                var name = $(this).attr('name');
                                if (name == 'output2') name = 'output';
                                if ($(this).val())
                                    propertiesString += name + " = " + $(this).val() + "\n";
                            });
                            var predicates = "";
                            $("#integration-job-form-tabs-1 > form > #mintlabel-predicates > div").each(function() {
                                predicates += $(this).children("input").val() + " ";
                            });
                            if (predicates != '')
                                propertiesString += "uriMintLabelPredicate = " + predicates;
                            var xml = $('<integrationJob></integrationJob>');

                            /*
                            if (properties != '')
                                xml.append($('<properties>'+properties+'</properties>'));
                            xml.append($('<sources>'+sources+'</sources>'));
                             */

                            // TODO implement this properly
                            if(linkSpecifications != '')
                                xml.append($('<linkSpecifications>'+linkSpecifications+'</linkSpecifications>'));
                            else xml.append($('<linkSpecifications>linkspecs</linkSpecifications>'));
                            if(mappings != '')
                                xml.append($('<mappings>'+mappings+'</mappings>'));
                            else xml.append($('<mappings>mappings</mappings>'));
                            if(sieve != '')
                                xml.append($('<sieve>'+sieve+'</sieve>'));
                            else xml.append($('<sieve>sieve</sieve>'));
                            xml.append($('<source>dumps</source>'));
                            xml.append($('<output><file>output/integrated.nq</file></output>'));


                            if (runSchedule != '')
                                xml.append($('<runSchedule>'+runSchedule+'</runSchedule>'));

                            if (selectedTab == 1) {
                                importIntegrationJob(projectName, file);
                            } else {
                                var xmlString = '<integrationJob>'+$(xml).html()+'</integrationJob>'
                                saveIntegrationJob(xmlString, propertiesString);
                            }
                            $(this).dialog("close");
                            loadingShow;
                        }
                    },
                    Cancel: function() {
                        $(this).dialog("close");
                    }
                }
    });
    $("#scheduler-form").dialog({
                title: '<span class="title-icon scheduler-icon"></span>Scheduler',
                autoOpen: false,
                height: 400,
                width: 580,
                modal: true,
                resizable: false,
                buttons: {
                    "Save": function() {
                        resetForm('scheduler-form', false);
                        var errors = new Array();
                        var fields = new Array();
                        var projectName = $("#scheduler-form > form > input[name='projectName']").val();
                        //var properties = $("#scheduler-form > form > input[name='properties']").val();
                        //var dataSources = $("#scheduler-form > form > input[name='dataSources']").val();
                        //var importJobs = $("#scheduler-form > form > input[name='importJobs']").val();
                        //var integrationJobs = $("#scheduler-form > form > input[name='integrationJobs']").val();
                        var dumpLocation = $("#scheduler-form > form > input[name='dumpLocation']").val();
                        if (!dumpLocation) {
                            errors.push('Dump location is empty.<br/>'); fields.push('dumpLocation');
                        }
                        if (errors.length > 0) {
                            showErrors('scheduler-form', errors, fields);
                        } else {
                            var propertiesString = "";
                            $("#scheduler-form-tabs-1 > form > .conf").each(function() {
                                if ($(this).val()) propertiesString += $(this).attr('name') + " = " + $(this).val() + "\n";
                            });
                            var xml = $('<scheduler></scheduler>');

                            /*
                            if (properties)
                                xml.append($('<properties>'+properties+'</properties>'));
                            if (dataSources != '')
                                xml.append($('<dataSources>'+dataSources+'</dataSources>'));
                            xml.append($('<importJobs>'+importJobs+'</importJobs>'));
                            xml.append($('<integrationJobs>'+integrationJobs+'</integrationJobs>'));
                            */

                            xml.append($('<dumpLocation>'+dumpLocation+'</dumpLocation>'));

                            saveScheduler(xml, projectName, propertiesString);
                        }
                        $(this).dialog("close");
                        loadingShow;
                    },
                    Cancel: function() {
                        $(this).dialog("close");
                    }
                }
    });
});

// -- display functions
function addLeaf(leaf, parent, desc, iconClass) {
    if (leaf) {
        var leaf_ul = document.createElement("ul");
        $(parent).append(leaf_ul);
        var leaf_li = document.createElement("li");
        $(leaf_ul).append(leaf_li);
        var leaf_span = document.createElement("span");
        $(leaf_span).addClass('file')
                .text(desc + leaf);
        $(leaf_li).append(leaf_span);
        if (iconClass) $(leaf_span).addClass(iconClass);
    }
}

function addDataSource(jsonDataSource, projectNode, projectName, p, d) {
    var ds_ul = document.createElement("ul");
    $(projectNode).append(ds_ul);
    var ds_li = document.createElement("li");
    $(ds_li).attr("id", 'datasource_' + projectName + '_' + jsonDataSource.label)
            .addClass('closed');
    $(ds_ul).append(ds_li);
    var ds_span = document.createElement("span");
    $(ds_span).addClass('source');
    $(ds_li).append(ds_span);

    var ds_label = document.createElement("span");
    $(ds_label).addClass('label')
            .text(jsonDataSource.label);
    $(ds_span).append(ds_label);

    var ds_actions = document.createElement("div");
    $(ds_actions).addClass('actions');
    $(ds_span).append(ds_actions);
    addAction('edit', 'Edit', "Edit data source: " + jsonDataSource.label, "editDataSource('" + projectName + "'," + p + "," + d + ")", ds_actions, projectName, true);
    addAction('delete', 'Remove', "Remove data source: " + jsonDataSource.label, "confirmDelete('removeDataSource','" + jsonDataSource.label + "')", ds_actions, projectName, true);

    addLeaf(jsonDataSource.description, ds_li, 'Description: ', 'description-icon');
    addLeaf(jsonDataSource.homepage, ds_li, 'Homepage: ', 'homepage-icon');
}

function addImportJob(jsonImportJob, projectNode, projectName, p, d) {
    var ds_ul = document.createElement("ul");
    $(projectNode).append(ds_ul);
    var ds_li = document.createElement("li");
    $(ds_li).attr("id", 'importjob_' + projectName + '_' + jsonImportJob.internalId)
            .addClass('closed');
    $(ds_ul).append(ds_li);
    var ds_span = document.createElement("span");
    $(ds_span).addClass('importjob');
    $(ds_li).append(ds_span);

    var ds_label = document.createElement("span");
    $(ds_label).addClass('label')
            .text(jsonImportJob.internalId);
    $(ds_span).append(ds_label);

    var ds_actions = document.createElement("div");
    $(ds_actions).addClass('actions');
    $(ds_span).append(ds_actions);
    addAction('edit', 'Edit', "Edit import job: "+ jsonImportJob.internalId, "editImportJob('" + projectName + "'," + p + "," + d + ")", ds_actions, projectName, true);
    addAction('delete', 'Remove', "Remove import job: " + jsonImportJob.internalId, "confirmDelete('removeImportJob','" + jsonImportJob.internalId + "')", ds_actions, projectName, true);

    addLeaf(jsonImportJob.dataSource, ds_li, 'Data source: ', 'data-source-icon');
    addLeaf(jsonImportJob.refreshSchedule, ds_li, 'Refresh schedule: ', 'refresh-schedule-icon');

    if (jsonImportJob.quadImportJob) {
        addLeaf('Quad Import', ds_li, 'Type: ', 'type-icon');
        addLeaf(jsonImportJob.quadImportJob.dumpLocation, ds_li, 'Dump location: ', 'dump-location-icon');
    }
    if (jsonImportJob.tripleImportJob) {
        addLeaf('Triple Import', ds_li, 'Type: ', 'type-icon');
        addLeaf(jsonImportJob.tripleImportJob.dumpLocation, ds_li, 'Dump location: ', 'dump-location-icon');
    }
    if (jsonImportJob.sparqlImportJob) {
        addLeaf('SPARQL Import', ds_li, 'Type: ', 'type-icon');
        addLeaf(jsonImportJob.sparqlImportJob.endpointLocation, ds_li, 'Endpoint location: ', 'endpoint-icon');
        addLeaf(jsonImportJob.sparqlImportJob.tripleLimit, ds_li, 'Triple limit: ', 'limit-icon');

        var pat_ul = document.createElement("ul");
        $(ds_li).append(pat_ul);
        var pat_li = document.createElement("li");
        $(pat_li).attr("id", 'sparqlpatterns_' + projectName + '_' + jsonImportJob.internalId).addClass('closed');
        $(pat_ul).append(pat_li);
        var pat_span = document.createElement("span");
        $(pat_span).addClass('uri');
        $(pat_li).append(pat_span);
        var pat_label = document.createElement("span");
        $(pat_label).addClass('label').text('SPARQL patterns');
        $(pat_span).append(pat_label);

        for (var pat in jsonImportJob.sparqlImportJob.sparqlPatterns) {
            addLeaf(jsonImportJob.sparqlImportJob.sparqlPatterns[pat].pattern, pat_li, '');
        }

    }
    if (jsonImportJob.crawlImportJob) {
        addLeaf('Crawl Import', ds_li, 'Type: ', 'type-icon');
        addLeaf(jsonImportJob.crawlImportJob.levels, ds_li, 'Levels: ', 'levels-icon');
        addLeaf(jsonImportJob.crawlImportJob.resourceLimit, ds_li, 'Resource limit: ', 'limit-icon');

        var pat_ul = document.createElement("ul");
        $(ds_li).append(pat_ul);
        var pat_li = document.createElement("li");
        $(pat_li).attr("id", 'seeduris_' + projectName + '_' + jsonImportJob.internalId).addClass('closed');
        $(pat_ul).append(pat_li);
        var pat_span = document.createElement("span");
        $(pat_span).addClass('uri');
        $(pat_li).append(pat_span);
        var pat_label = document.createElement("span");
        $(pat_label).addClass('label').text('Seed URIs');
        $(pat_span).append(pat_label);

        for (var pat in jsonImportJob.crawlImportJob.seedURIs) {
            addLeaf(jsonImportJob.crawlImportJob.seedURIs[pat].uri, pat_li, '');
        }

        var pat_ul = document.createElement("ul");
        $(ds_li).append(pat_ul);
        var pat_li = document.createElement("li");
        $(pat_li).attr("id", 'predicates_' + projectName + '_' + jsonImportJob.internalId).addClass('closed');
        $(pat_ul).append(pat_li);
        var pat_span = document.createElement("span");
        $(pat_span).addClass('uri');
        $(pat_li).append(pat_span);
        var pat_label = document.createElement("span");
        $(pat_label).addClass('label').text('Predicates to follow');
        $(pat_span).append(pat_label);

        for (var pat in jsonImportJob.crawlImportJob.predicatesToFollow) {
            addLeaf(jsonImportJob.crawlImportJob.predicatesToFollow[pat].uri, pat_li, '');
        }
    }
}

// Display a mapping and the related actions
function addMapping(jsonMapping, integrationNode, projectName, p, d) {
    var ul = document.createElement("ul");
    $(integrationNode).append(ul);
    var li = document.createElement("li");
    $(li).attr("id", 'mapping_' + p + '_' + d)
    $(ul).append(li);

    var span = document.createElement("span");
    $(span).addClass('mapping')
        .text('Mapping_' + d );
    $(li).append(span);

    var actions = document.createElement("div");
    $(actions).addClass('actions');
    $(span).append(actions);
    addAction('edit', 'Edit', "Edit mapping", "editMapping('"+ jsonMapping +"'," + d + ")", actions, projectName, true);
}

// Display a linking specification and the related actions
function addLinkSpec(jsonLinkSpec, integrationNode, projectName, p, d) {
    var ul = document.createElement("ul");
    $(integrationNode).append(ul);
    var li = document.createElement("li");
    $(li).attr("id", 'linkspec_' + p + '_' + d)
    $(ul).append(li);

    var span = document.createElement("span");
    $(span).addClass('linkspec')
        .text('LinkingSpecification_' + d );
    $(li).append(span);

    var actions = document.createElement("div");
    $(actions).addClass('actions');
    $(span).append(actions);
    addAction('edit', 'Edit', "Edit linking specification", "editLinkSpec('"+ jsonLinkSpec +"'," + d + ")", actions, projectName, true);
}

function addIntegrationJob(jsonIntegrationJob, projectNode, projectName, p) {
    var ul = document.createElement("ul");
    $(projectNode).append(ul);
    var li = document.createElement("li");
    $(li).attr("id", 'importjob_' + projectName + '_integrationjob')
            .addClass('closed');
    $(ul).append(li);
    var span = document.createElement("span");
    $(span).addClass('integrationjob');
    $(li).append(span);

    var label = document.createElement("span");
    $(label).addClass('label')
            .text('Integration Job');
    $(span).append(label);

    var actions = document.createElement("div");
    $(actions).addClass('actions');
    $(span).append(actions);
    addAction('edit', 'Edit', "Edit integration job", "editIntegrationJob('" + projectName + "'," + p + ")", actions, projectName, true);
    addAction('delete', 'Remove', "Remove integration job ", "confirmDelete('removeIntegrationJob','IntegrationJob')", actions, projectName, true);
    addAction('run', 'Run', "Run integration job", "runIntegration('')", actions, projectName, true);

    addLeaf(jsonIntegrationJob.runSchedule, li, 'Run schedule: ', 'refresh-schedule-icon');
    //### addLeaf(jsonIntegrationJob.sources, li, 'Sources: ', 'folder-icon');

    // display Mappings
    //    var mappings = jsonIntegrationJob.mappings
    //    for (var d in mappings) {
    //        addMapping(mappings[d], li, projectName, p, d);
    //    }
    addMapping(jsonIntegrationJob.mappings, li, projectName, p, 0);
    addLinkSpec(jsonIntegrationJob.linkSpecifications, li, projectName, p, 0);

    addLeaf(jsonIntegrationJob.sieve, li, 'Sieve: ', 'folder-icon');
    addLeaf(jsonIntegrationJob.output, li, 'Output: ','properties-icon');
    //### addLeaf(jsonIntegrationJob.properties, li, 'Properties: ','properties-icon');

    var conf_ul = document.createElement("ul");
    $(li).append(conf_ul);
    var conf_li = document.createElement("li");
    $(conf_li).attr("id", 'config_' + projectName + '_integrationjob').addClass('closed');
    $(conf_ul).append(conf_li);
    var conf_span = document.createElement("span");
    $(conf_span).addClass('conf');
    $(conf_li).append(conf_span);
    var conf_label = document.createElement("span");
    $(conf_label).addClass('label').text('Configuration properties');
    $(conf_span).append(conf_label);

    //TODO to be updated, see http://ldif.wbsg.de/#configurationproperties
    addLeaf(jsonIntegrationJob.configurationProperties.output, conf_li, 'Output: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.rewriteURIs, conf_li, 'Rewrite URIs: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.provenanceGraphURI, conf_li, 'Provenance graph URI: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.validateSources, conf_li, 'Validate sources: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.discardFaultyQuads, conf_li, 'Discard faulty quads: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.useExternalSameAsLinks, conf_li, 'Use external sameAs-links: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.uriMinting, conf_li, 'URI minting: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.uriMintNamespace, conf_li, 'URI mint namespace: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.uriMintLanguageRestriction, conf_li, 'URI mint language restriction: ', 'foo-icon');
    addLeaf(jsonIntegrationJob.configurationProperties.outputQualityScores, conf_li, 'Output quality scores: ', 'foo-icon');

    addLeaf(jsonIntegrationJob.configurationProperties.uriMintLabelPredicate, conf_li, 'Mint label predicates: ', 'foo-icon');
    //TODO parse uriMintLabelPredicate
//    if (jsonIntegrationJob.configurationProperties.uriMintLabelPredicate) {
//        var pat_ul = document.createElement("ul");
//        $(conf_li).append(pat_ul);
//        var pat_li = document.createElement("li");
//        $(pat_li).attr("id", 'predicates_' + projectName + '_integrationjob').addClass('closed');
//        $(pat_ul).append(pat_li);
//        var pat_span = document.createElement("span");
//        $(pat_span).addClass('uri');
//        $(pat_li).append(pat_span);
//        var pat_label = document.createElement("span");
//        $(pat_label).addClass('label').text('Mint label predicates');
//        $(pat_span).append(pat_label);
//
//        for (var pat in jsonIntegrationJob.configurationProperties.uriMintLabelPredicate) {
//            addLeaf(jsonIntegrationJob.configurationProperties.uriMintLabelPredicate[pat].uri, pat_li, '');
//        }
//    }
}

function addScheduler(jsonScheduler, projectNode, projectName, p) {
 /* ###   var ds_ul = document.createElement("ul");
    $(projectNode).append(ds_ul);
    var ds_li = document.createElement("li");
    $(ds_li).attr("id", 'importjob_' + projectName + '_scheduler')
            .addClass('closed');
    $(ds_ul).append(ds_li);
    var ds_span = document.createElement("span");
    $(ds_span).addClass('scheduler');
    $(ds_li).append(ds_span);

    var ds_label = document.createElement("span");
    $(ds_label).addClass('label')
            .text('Scheduler');
    $(ds_span).append(ds_label);

    var ds_actions = document.createElement("div");
    $(ds_actions).addClass('actions');
    $(ds_span).append(ds_actions);
    addAction('edit', 'Edit', "Edit scheduler", "editScheduler('" + projectName + "'," + p + ")", ds_actions, projectName, true);

    addLeaf(jsonScheduler.scheduler.dataSources, ds_li, 'Data sources: ', 'folder-icon');
    addLeaf(jsonScheduler.scheduler.importJobs, ds_li, 'Import jobs: ', 'folder-icon');
    addLeaf(jsonScheduler.dumpLocation, ds_li, 'Dumps: ', 'folder-icon');
    addLeaf(jsonScheduler.integrationJob, ds_li, 'Integration job: ','properties-icon');
    addLeaf(jsonScheduler.properties, ds_li, 'Properties: ', 'properties-icon');
### */
    var conf_ul = document.createElement("ul");
    $(projectNode).append(conf_ul);   //###
    var conf_li = document.createElement("li");
    $(conf_li).attr("id", 'config_' + projectName + '_scheduler').addClass('closed');
    $(conf_ul).append(conf_li);
    var conf_span = document.createElement("span");
    $(conf_span).addClass('conf');
    $(conf_li).append(conf_span);
    var conf_label = document.createElement("span");
    $(conf_label).addClass('label').text('Configuration properties');
    $(conf_span).append(conf_label);

// ### start add
    var ds_actions = document.createElement("div");
    $(ds_actions).addClass('actions');
    $(conf_span).append(ds_actions);
    addAction('edit', 'Edit', "Edit scheduler", "editScheduler('" + projectName + "'," + p + ")", ds_actions, projectName, true);
    // ###  end add 

    addLeaf(jsonScheduler.configurationProperties.provenanceGraphURI, conf_li, 'Provenance graph URI: ', 'foo-icon');
    addLeaf(jsonScheduler.configurationProperties.oneTimeExecution, conf_li, 'One time execution: ', 'foo-icon');
}


// -- display the workspace as treeview
function updateWorkspace(obj) {

    removeNodeById("div_tree");

    // root folder
    if (!document.getElementById("root-folder")) {
        var rootFolder = document.createElement("div");
        $(rootFolder).attr("id", 'root-folder');
        $("#content").append(rootFolder);
    }

    var proj_actions = document.createElement("div");
    $(proj_actions).addClass('actions');
    $("#content").append(proj_actions);

    // new project button
    if (!document.getElementById("newproject")) {
        var newProj = document.createElement("div");
        $(newProj).attr("id", 'newproject');
        addAction('add', 'New scheduler', 'Create new scheduler', "createNewProject();", newProj, "", true);
        $(proj_actions).append(newProj);
    }

    // import project button
    /*
    if (!document.getElementById("import")) {
        var importProj = document.createElement("div");
        $(importProj).attr("id", 'import');
        addAction('import', 'Import', 'Import a project', "importNewProject();", importProj, "", true);
        $(proj_actions).append(importProj);
    }  */

    var tree = document.createElement("div");
    tree.id = "div_tree";

    var root = document.createElement("ul");
    $(root).attr("id", 'tree')
            .addClass('filetree');
    $(tree).append(root);

    if (obj) {

        console.log('updateWorkspace');

        // for each project
        for (var p in obj.workspace.project) {
            var project = obj.workspace.project[p];
            var proj = document.createElement("li");
            $(proj).addClass('closed')
                    .attr("id", 'project_' + project.name);
            //.attr("onclick", "updateActiveProject('" + project.name + "')");
            $(root).append(proj);
            var proj_span = document.createElement("span");
            $(proj_span).addClass('scheduler'); //###
            $(proj).append(proj_span);

            var proj_label = document.createElement("span");
            $(proj_label).addClass('label')
                    .text(project.name);
            $(proj_span).append(proj_label);

            var proj_actions = document.createElement("div");
            $(proj_actions).addClass('actions');
            $(proj_span).append(proj_actions);

            addAction('add_source', 'Source', 'Add data source', "createDataSource('" + project.name + "'," + p + ")", proj_actions, project.name, true);
            addAction('add_import_job', 'Import Job', 'Add import job', "createImportJob('" + project.name + "'," + p + ")", proj_actions, project.name, true);
            addAction('integration_job', 'Integration Job', 'Add integration job', "createIntegrationJob('" + project.name + "'," + p + ")", proj_actions, project.name, !integrationJobExists(p));
            //### addAction('schedule_job', 'Scheduler', 'Configure scheduler', "editScheduler('" + project.name + "'," + p + ")", proj_actions, project.name, true);
            addAction('export', 'Export','Export scheduler: '+project.name,"exportProject('"+project.name+"')",proj_actions, project.name, true);
            addAction('delete', 'Remove', 'Remove scheduler: '+project.name, "confirmDelete('removeProject','"+project.name+"')", proj_actions, project.name, true);
     	    addAction('run', 'Run', "Run scheduler: "+project.name, "runScheduler('')", proj_actions, project.name, integrationJobExists(p));

            // display dataSource
            var dataSources = obj.workspace.project[p].scheduler.dataSources
            for (var d in dataSources) {
                addDataSource(dataSources[d], proj, project.name, p, d);
            }

            // display importJob
            var importJobs = obj.workspace.project[p].scheduler.importJobs
            for (var d in importJobs) {
                addImportJob(importJobs[d], proj, project.name, p, d);
            }

            // display integrationJob
            if (project.scheduler.integrationJob[0] !== undefined)
                addIntegrationJob(project.scheduler.integrationJob[0], proj, project.name, p);

            // display scheduler
            if (project.scheduler !== undefined)
                addScheduler(project.scheduler, proj, project.name, p);
        }
    }

    $("#content").append(tree);
    var tree = $("#tree").treeview({animated:"fast"});

    // prevent toggeling tree on button click:
    $('button').click(function(e) {
        e.stopImmediatePropagation();
    });

    $(".ui-icon").removeClass('ui-icon');
    $(".ui-icon-closethick").addClass('ui-icon');
    workspaceObj = obj;
}


function addAction(type, label, desc, action, parent, activeProject, enabled) {
    var icon = getIcon(type);
    var action_button = document.createElement("button");
        $(action_button).click(function() {
                console.log("Evaluate "+action);
                //saveOpenNodes(activeProject);
                if (activeProject!=""){
                    setCurrentProject(activeProject);}
                $.globalEval(action);
            })
            .addClass("action")
            .button({
                icons: {
                    primary: icon
                },
                label: label
                });

    if (!enabled) {
        $(action_button).attr("disabled", "disabled").addClass('ui-state-disabled');
    }
    $(action_button).attr("title", desc);
    $(parent).append(action_button);

}

function getIcon(type) {
    var icon;
    switch (type) {
        case 'add' :
            icon = "new-project-icon";
            break;
        case 'add_source' :
            icon = "add-icon";
            break;
        case 'integration_job' :
            icon = "add-icon";
            break;
        case 'add_import_job' :
            icon = "add-icon";
            break;
        case 'delete' :
            icon = "remove-icon";
            break;
        case 'import':
            icon = "import-icon";
            break;
        case 'export':
            icon = "export-icon";
            break;
        case 'schedule_job' :
            icon = "scheduler-icon";
            break;
        case 'edit' :
            icon = "edit-icon";
            break; 
        case 'run' :
            icon = "run-icon";
            break;        
    }
    return icon;
}

function callAction(action,res){
    // work-around : passing the action as string parameter -> the action would be invoked anyway
    switch (action)
        {
        case 'removeProject' :  removeProject();  break;
        case 'removeDataSource' :  removeDataSource(res);  break;
        case 'removeImportJob' :  removeImportJob(res);  break;
        case 'removeIntegrationJob' : removeIntegrationJob(); break;
        default : alert("Error: Action \'"+action+"\' not defined!");
        }
}

// -- Form functions
function createNewProject() {
    resetForm('new-project-form', true);
    $('#new-project-form').tabs();
    $("#new-project-form-tabs > ul, #new-project-form-tabs-2").show();
    $('#new-project-form').dialog('open');
}

function createDataSource(projectName, p) {
    resetForm('data-source-form', true);
    $("#data-source-form").tabs();
    $("#data-source-form-tabs > ul, #data-source-form-tabs-2").show();
    $("#data-source-form-tabs-1 > form > input[name='projectName']").attr("value", projectName);
    $("#data-source-form-tabs-1 > form > input[name='projectIndex']").attr("value", p);
    $("#data-source-form-tabs-1 > form > input[name='dataSourceIndex']").attr("value", "");
    $('#data-source-form').dialog('open').dialog("option", "title", '<span class="title-icon add-icon"></span>Add data source');
}
function editDataSource(projectName, p, d) {
    $("#data-source-form").tabs("select", 0);
    resetForm('data-source-form', false);
    $("#data-source-form-tabs > ul, #data-source-form-tabs-2").hide();
    $("#data-source-form-tabs-1 > form > input[name='projectName']").attr("value", projectName);
    $("#data-source-form-tabs-1 > form > input[name='projectIndex']").attr("value", p);
    $("#data-source-form-tabs-1 > form > input[name='dataSourceIndex']").attr("value", d);
    var dataSource = workspaceObj.workspace.project[p].scheduler.dataSources[d];
    $("#data-source-form-tabs-1 > form > input[name='label']").attr("value", dataSource.label);
    $("#data-source-form-tabs-1 > form > input[name='description']").attr("value", dataSource.description);
    $("#data-source-form-tabs-1 > form > input[name='homepage']").attr("value", dataSource.homepage);
    $('#data-source-form').dialog('open').dialog("option", "title", '<span class="title-icon edit-form-icon"></span>Edit data source');
}

function createImportJob(projectName, projectIndex) {
    resetImportJobForm(projectIndex);
    $("#import-job-form-tabs-1 > form > select").each(function() {
            $(this).children("option:first").attr('selected', 'selected');
    });
    $("#import-job-form").tabs();
    $("#import-job-form-tabs > ul, #import-job-form-tabs-2").show();
    $("#import-job-form-tabs-1 > form > input[name='projectName']").attr("value", projectName);
    $("#import-job-form-tabs-1 > form > input[name='projectIndex']").attr("value", projectIndex);
    $("#data-source-form-tabs-1 > form > input[name='importJobIndex']").attr("value", "");
    $("div#quadImportJob").show();
    $('#import-job-form').dialog('open').dialog("option", "title", '<span class="title-icon add-icon"></span>Add import job');
}
function editImportJob(projectName, projectIndex, d) {
    $("#import-job-form").tabs("select", 0);
    resetImportJobForm(projectIndex);
    $("#import-job-form-tabs > ul, #import-job-form-tabs-2").hide();
    var importJob = workspaceObj.workspace.project[projectIndex].scheduler.importJobs[d];
    if (importJob.quadImportJob) {
        $("#import-job-form-tabs-1 > form > #quadImportJob > input[name='dumpLocation']").attr("value", importJob.quadImportJob.dumpLocation);
        $("#import-job-form-tabs-1 > form > select[name='type'] > option[value='quadImportJob']").attr('selected','selected');
        $("div#quadImportJob").show();
    }
    if (importJob.tripleImportJob) {
        $("#import-job-form-tabs-1 > form > #tripleImportJob > input[name='dumpLocation']").attr("value", importJob.tripleImportJob.dumpLocation);
        $("#import-job-form-tabs-1 > form > select[name='type'] > option[value='tripleImportJob']").attr('selected','selected');
        $("div#tripleImportJob").show();
    }
    if (importJob.sparqlImportJob) {
        $("#patterns").html('');
        $("#import-job-form-tabs-1 > form > #sparqlImportJob > input[name='endpointLocation']").attr("value", importJob.sparqlImportJob.endpointLocation);
        $("#import-job-form-tabs-1 > form > #sparqlImportJob > input[name='tripleLimit']").attr("value", importJob.sparqlImportJob.tripleLimit);
        $("#import-job-form-tabs-1 > form > select[name='type'] > option[value='sparqlImportJob']").attr('selected','selected');
        for (var i in importJob.sparqlImportJob.sparqlPatterns) {
            $("#patterns").append('<div id="pattern'+i+'">' +
                    '<div class="input-label" style="width: 60px;">Pattern</div>' +
                    '<input type="text" name="pattern'+i+'" value="'+importJob.sparqlImportJob.sparqlPatterns[i].pattern+'" class="text ui-widget-content ui-corner-all"/>' +
                    ' <button type="button" style="display: inline;" onclick="removePattern($(this).parent().attr(\'id\'));">Remove</button>' +
                    '</div>');
        }
        $("div#sparqlImportJob").show();
    }
    if (importJob.crawlImportJob) {
        $("#seeds, #predicates").html('');
        $("#import-job-form-tabs-1 > form > #crawlImportJob > input[name='levels']").attr("value", importJob.crawlImportJob.levels);
        $("#import-job-form-tabs-1 > form > #crawlImportJob > input[name='resourceLimit']").attr("value", importJob.crawlImportJob.resourceLimit);
        $("#import-job-form-tabs-1 > form > select[name='type'] > option[value='crawlImportJob']").attr('selected','selected');
        for (var i in importJob.crawlImportJob.seedURIs) {
            $("#seeds").append('<div id="seed'+i+'">' +
                    '<div class="input-label" style="width: 35px;">URI</div>' +
                    '<input type="text" name="seed'+i+'" value="'+importJob.crawlImportJob.seedURIs[i].uri+'" class="text ui-widget-content ui-corner-all"/>' +
                    ' <button type="button" style="display: inline;" onclick="removeSeed($(this).parent().attr(\'id\'));">Remove</button>' +
                    '</div>');
        }
        for (var i in importJob.crawlImportJob.predicatesToFollow) {
            $("#predicates").append('<div id="predicate'+i+'">' +
                    '<div class="input-label" style="width: 35px;">URI</div>' +
                    '<input type="text" name="predicate'+i+'" value="'+importJob.crawlImportJob.predicatesToFollow[i].uri+'" class="text ui-widget-content ui-corner-all"/>' +
                    ' <button type="button" style="display: inline;" onclick="removePredicate($(this).parent().attr(\'id\'));">Remove</button>' +
                    '</div>');
        }
        $("div#crawlImportJob").show();
    }
    $("#import-job-form-tabs-1 > form > input").each(function() {
       var name = $(this).attr('name');
       $(this).val(eval('importJob.'+name));
    });
    $("#import-job-form-tabs-1 > form > select").each(function() {
        var name = $(this).attr('name');
        $(this).children().each(function() {
        if ($(this).val() == eval('importJob.'+name)) $(this).attr('selected','selected');
        });
    });
    $("#import-job-form-tabs-1 > form > input[name='projectName']").attr("value", projectName);
    $("#import-job-form-tabs-1 > form > input[name='projectIndex']").attr("value", projectIndex);
    $("#import-job-form-tabs-1 > form > input[name='importJobIndex']").attr("value", d);
    $('#import-job-form').dialog('open').dialog("option", "title", '<span class="title-icon edit-form-icon"></span>Edit import job');
}

function resetImportJobForm(projectIndex) {
    resetForm('import-job-form', true);
    $('#quadImportJob, #tripleImportJob, #sparqlImportJob, #crawlImportJob').hide();
    $("div#quadImportJob").show();
    $("#import-job-form-tabs-1 > form > input").val('');
    $("#import-job-form-tabs-1 > form > #quadImportJob > input").val('');
    $("#import-job-form-tabs-1 > form > #tripleImportJob > input").val('');
    $("#import-job-form-tabs-1 > form > #sparqlImportJob > input").val('');
    $("#import-job-form-tabs-1 > form > #crawlImportJob > input").val('');
    $("#import-job-form-tabs-1 > form > select > option").removeAttr('selected');
    $("#patterns").html('<div id="pattern0"><div class="input-label" style="width: 60px;">Pattern</div><input type="text" name="pattern0" class="text ui-widget-content ui-corner-all"/> <button type="button" style="display: inline;" onclick="removePattern($(this).parent().attr(\'id\'));">Remove</button></div>');
    $("#seeds").html('<div id="seed0"><div class="input-label" style="width: 35px;">URI</div><input type="text" name="seed0" class="text ui-widget-content ui-corner-all"/> <button type="button" style="display: inline;" onclick="removeSeed($(this).parent().attr(\'id\'));">Remove</button></div>');
    $("#predicates").html('<div id="predicate0"><div class="input-label" style="width: 35px;">URI</div><input type="text" name="predicate0" class="text ui-widget-content ui-corner-all"/> <button type="button" style="display: inline;" onclick="removePredicate($(this).parent().attr(\'id\'));">Remove</button></div>');
    $('#quadImportJob, #tripleImportJob, #sparqlImportJob, #crawlImportJob').hide();
    // load data sources:
    $("#import-job-form-tabs-1 > form > select[name='dataSource']").html('');

    for (var i in workspaceObj.workspace.project[projectIndex].scheduler.dataSources) {
        var label = workspaceObj.workspace.project[projectIndex].scheduler.dataSources[i].label;
        $("#import-job-form-tabs-1 > form > select[name='dataSource']").append('<option value="'+label+'">'+label+'</option>');
    }
}


// -- Import Job form functions:
function addPattern() {
    var patID = $("#patterns > div").last().attr('id');
    var counter = parseInt(patID.substr(7, 1)) + 1;
    var pat = '<div id="pattern' + counter + '"><div class="input-label" style="width: 60px;">Pattern</div><input type="text" name="pattern' + counter + '" class="text ui-widget-content ui-corner-all"/> <button type="button" style="display: inline;" onclick="removePattern($(this).parent().attr(\'id\'));">Remove</button></div>';
    $("#patterns").append(pat);
}
function removePattern(id) {
    var counter = $("#patterns > div").size();
    if (counter > 1) $("#" + id).remove();
}
function addSeed() {
    var patID = $("#seeds > div").last().attr('id');
    var counter = parseInt(patID.substr(5, 1)) + 1;
    var pat = '<div id="seed' + counter + '"><div class="input-label" style="width: 35px;">URI</div><input type="text" name="seed' + counter + '" class="text ui-widget-content ui-corner-all"/> <button type="button" style="display: inline;" onclick="removeSeed($(this).parent().attr(\'id\'));">Remove</button></div>';
    $("#seeds").append(pat);
}
function removeSeed(id) {
    var counter = $("#seeds > div").size();
    if (counter > 1) $("#" + id).remove();
}
function addPredicate() {
    var patID = $("#predicates > div").last().attr('id');
    var counter = parseInt(patID.substr(9, 1)) + 1;
    var pat = '<div id="predicate' + counter + '"><div class="input-label" style="width: 35px;">URI</div><input type="text" name="predicate' + counter + '" class="text ui-widget-content ui-corner-all"/> <button type="button" style="display: inline;" onclick="removePredicate($(this).parent().attr(\'id\'));">Remove</button></div>';
    $("#predicates").append(pat);
}
function removePredicate(id) {
    var counter = $("#predicates > div").size();
    if (counter > 1) $("#" + id).remove();
}

function createIntegrationJob(projectName, p) {
    resetForm('integration-job-form', true);
    $("#integration-job-form-tabs-1 > form > select").each(function() {
            $(this).children("option:first").attr('selected', 'selected');
    });
    $("#integration-job-form").tabs();
    $("#integration-job-form-tabs > ul, #integration-job-form-tabs-2").show();
    $("#integration-job-form-tabs-1 > form > input[name='projectName']").attr("value", projectName);
    $("#integration-job-form-tabs-1 > form > input[name='projectIndex']").attr("value", p);
    $("#integration-job-form-tabs-1 > form > input[name='importJobIndex']").attr("value", "");
    $('#integration-job-form').dialog('open').dialog("option", "title", '<span class="title-icon add-icon"></span>Add integration job');
}

function editIntegrationJob(projectName, p) {
    $("#integration-job-form").tabs("select", 0);
    resetForm('integration-job-form', false);
    $("#integration-job-form-tabs > ul, #integration-job-form-tabs-2").hide();
    var integrationJob = workspaceObj.workspace.project[p].scheduler.integrationJob[0];
    $("#integration-job-form-tabs-1 > form > input").each(function() {
       var name = $(this).attr('name');
        if ($(this).hasClass('conf')) {
            $(this).val(eval('integrationJob.configurationProperties.'+name));
        } else {
            $(this).val(eval('integrationJob.'+name));
        }
    });
    $("#integration-job-form-tabs-1 > form > select").each(function() {
        var name = $(this).attr('name');
        if (name == 'output2') name = 'output';
        var path = 'integrationJob.';
        if ($(this).hasClass('conf')) path = 'integrationJob.configurationProperties.';
        $(this).children().each(function() {
            if ($(this).val() == eval(path+name)) $(this).attr('selected','selected');
        });
    });

    $("#mintlabel-predicates").html('');
    for (var i in integrationJob.configurationProperties.uriMintLabelPredicate) {
            $("#mintlabel-predicates").append('<div id="mintlabel-predicate'+i+'">' +
                    '<div class="input-label" style="width: 35px;">URI</div>' +
                    '<input type="text" name="predicate'+i+'" value="'+integrationJob.configurationProperties.uriMintLabelPredicate[i].uri+'" class="text ui-widget-content ui-corner-all"/>' +
                    ' <button type="button" style="display: inline;" onclick="removeMintLabelPredicate($(this).parent().attr(\'id\'));">Remove</button>' +
                    '</div>');
    }

    $("#integration-job-form-tabs-1 > form > input[name='projectName']").attr("value", projectName);
    $("#integration-job-form-tabs-1 > form > input[name='projectIndex']").attr("value", p);
    $('#integration-job-form').dialog('open').dialog("option", "title", '<span class="title-icon edit-form-icon"></span>Edit integration job');
}

function addMintLabelPredicate() {
    var patID = $("#mintlabel-predicates > div").last().attr('id');
    var counter = parseInt(patID.substr(9, 1)) + 1;
    var pat = '<div id="predicate' + counter + '"><div class="input-label" style="width: 35px;">URI</div><input type="text" name="predicate' + counter + '" class="text ui-widget-content ui-corner-all"/> <button type="button" style="display: inline;" onclick="removeMintLabelPredicate($(this).parent().attr(\'id\'));">Remove</button></div>';
    $("#mintlabel-predicates").append(pat);
}
function removeMintLabelPredicate(id) {
    var counter = $("#mintlabel-predicates > div").size();
    if (counter > 1) $("#" + id).remove();
}

function editScheduler(projectName, p) {
    var scheduler = workspaceObj.workspace.project[p].scheduler;
    $("#scheduler-form > form > input").each(function() {
       var name = $(this).attr('name');
        if ($(this).hasClass('conf')) {
            $(this).val(eval('scheduler.configurationProperties.'+name));
        } else {
            $(this).val(eval('scheduler.'+name));
        }
    });
    $("#scheduler-form > form > select").each(function() {
        var name = $(this).attr('name');
        var path = 'scheduler.';
        if ($(this).hasClass('conf')) path = 'scheduler.configurationProperties.';
        $(this).children().each(function() {
            if ($(this).val() == eval(path+name)) $(this).attr('selected','selected');
        });
    });
    $("#scheduler-form > form > input[name='projectName']").attr("value", projectName);
    $("#scheduler-form > form > input[name='projectIndex']").attr("value", p);
    $('#scheduler-form').dialog('open');
}

// init and display the proper delete confirm dialog
function confirmDelete(action,res){
    $("#remove-form > span.resource").html(res);
    $("#remove-form").dialog({
                title: 'Remove',
                height: 180,
                width: 440,
                modal: true,
                resizable: false,
                buttons: {
                    "Yes, delete it": function() {
                        $(this).dialog("close");
                        callAction(action,res);
                    },
                    Cancel: function() {
                        $(this).dialog("close");
                    }
                }
    });
}

function resetForm(id, clearInput) {
    if (clearInput) {
        $("#" + id + "-tabs-1 > form > input, #" + id + "-tabs-2 > form > input").val('');
        $("#" + id + "-tabs-1 > form > select > option, #" + id + "-tabs-2 > form > select > option").removeAttr('selected');
    }
    $("#" + id + " > .error").hide();
    $("#" + id + " > .error > .message").html('');
    $("input, select").removeClass('ui-state-error');

}

function showErrors(id, errors, fields) {
    for (var i in errors)
        $("#" + id + " > .error > .message").append(errors[i]);
    for (var j in fields)
        $("#" + id + "-tabs-1 > form > input[name='" + fields[j] + "'], #" + id + "-tabs-2 > form > input[name='" + fields[j] + "']").addClass('ui-state-error');
    if (jQuery.inArray('dumpLocation', fields) != -1)
        $("input[name='dumpLocation']").addClass('ui-state-error');
    if (jQuery.inArray('endpointLocation', fields) != -1)
        $("input[name='endpointLocation']").addClass('ui-state-error');
    if (jQuery.inArray('levels', fields) != -1)
            $("input[name='levels']").addClass('ui-state-error');
    if (jQuery.inArray('resourceLimit', fields) != -1)
            $("input[name='resourceLimit']").addClass('ui-state-error');
    if (jQuery.inArray('tripleLimit', fields) != -1)
            $("input[name='tripleLimit']").addClass('ui-state-error');
    if (jQuery.inArray('seed', fields) != -1)
        $("input[name='seed0']").addClass('ui-state-error');

    $("#" + id + " > .error").show();
}

function integrationJobExists(p) {
    return (workspaceVar.workspace.project[p].scheduler.integrationJob[0] !== undefined);
}

// display loading bar
function loadingShow(){
    $("#loading-dialog").dialog("open");
}

function loadingHide(){
    $("#loading-dialog").dialog("close");
}

// init loading dialog
function initLoadingDialog(){
    $("#loading-dialog").dialog({
        title: 'Loading...',
        width: 330, height: 85,
        modal: true,
        resizable: false,
        closable: false,
        autoOpen: false,
        closeOnEscape: false,
        open: function(event, ui) { $(".ui-dialog-titlebar-close", $(this).parent()).hide();}
    });
    $("#loading-progressbar").progressbar({value: 100});
}

function removeNodeById(nodeId)
{
    var node = document.getElementById(nodeId);
    if (node) node.parentNode.removeChild(node);
}

