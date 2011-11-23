
// Functions to be defined in the backend:

function createProject(projectName) {}
function importProject(projectName, file) {}
function exportProject(projectName) {}
function removeProject(projectName) {}
function removeDataSource(projectName,ressourceName) {}
function removeImportJob(projectName,ressourceName) {}
function removeIntegrationJob(projectName) {}

function saveDataSource(xml, projectName, dataSourceName) {}
/*
 if dataSourceName is set: update existing data source (dataSourceName is the current 'label')
    else: create new Data source
*/
function importDataSource(projectName, file) {}
function saveImportJob(xml, projectName, importJobName) {}
/*
 if importJobName is set: update existing Import job (importJobName is the current 'internalId')
    else: create new Import job
*/
function importImportJob(projectName, file) {}
function saveIntegrationJob(xml, projectName, propertiesString) {}
function importIntegrationJob(projectName, file) {}
function saveScheduler(xml, projectName, propertiesString) {}




// updateWorkspace(workspaceVar) should be called at the end of each functions above (analogous to the Silk workspace)


$(function () {
	updateWorkspace(workspaceVar);
});

var workspaceVar = {
  "workspace":{
    "project":[{
      "name":"MyScheduler",
      "dataSource":[{
        "label":"DBpedia",
        "description":"DBpedia ist an RDF version of Wikipedia",
        "homepage":"http://dbpedia.org"
      },{
        "label":"mySecondSource",
        "description":"some description...",
        "homepage":""
      }],
      "importJob":[{
        "internalId":"dBpedia.0",
        "dataSource":"mySecondSource",
        "refreshSchedule":"daily",
        "tripleImportJob":{
            "dumpLocation":"http://dbpedia.org/dump.nt"
        }
      },{
        "internalId":"musicbrainz.3",
        "dataSource":"MusicBrainz_Talis",
        "refreshSchedule":"monthly",
        "sparqlImportJob":{
            "endpointLocation":"http://api.talis.com/stores/musicbrainz/services/sparql",
            "tripleLimit":"100000",
            "sparqlPatterns":[
                { "pattern":"?s a &lt;http://purl.org/ontology/mo/MusicArtist&gt;" }
            ]
        }
      },{
        "internalId":"freebase.0",
        "dataSource":"Freebase",
        "refreshSchedule":"onStartup",
        "crawlImportJob":{
            "seedURIs":[
                { "uri":"http://rdf.freebase.com/ns/en.dance-pop" },
                { "uri":"http://rdf.freebase.com/ns/en.radiohead" },
                { "uri":"http://rdf.freebase.com/ns/en.art_rock" }
            ],
            "predicatesToFollow":[
                { "uri":"http://rdf.freebase.com/ns/music.artist.genre" },
                { "uri":"http://rdf.freebase.com/ns/music.genre.albums" }
            ],
            "levels":"5",
            "resourceLimit":"50000"
        }
      }],
      "integrationJob":{
        "properties":"test.properties",
        "sources":"sources",
        "linkSpecifications":"linkSpecs",
        "mappings":"mappings",
        "output":"output.nq",
        "runSchedule":"daily",
        "configurationProperties":{
            "output":"all",
            "rewriteURIs":"true",
            "provenanceGraphURI":"http://www4.wiwiss.fu-berlin.de/ldif/provenance",
            "validateSources":"true",
            "discardFaultyQuads":"false",
            "useExternalSameAsLinks ":"true",
            "outputFormat":"nt",
            "uriMinting":"true",
            "uriMintNamespace":"http://www4.wiwiss.fu-berlin.de/ldif/resource/",
            "uriMintLabelPredicate":[
                { "uri":"http://www.w3.org/2000/01/rdf-schema#label" },
                { "uri":"http://www4.wiwiss.fu-berlin.de/ldif/property-example/id" }
            ]
        }
      },
      "scheduler":{
        "properties":"scheduler.properties",
        "dataSources":"datasources",
        "importJobs":"importJobs",
        "integrationJob":"integration-config.xml",
        "dumpLocation":"dumps",
        "configurationProperties":{
            "provenanceGraphURI":"http://www4.wiwiss.fu-berlin.de/ldif/provenance",
            "oneTimeExecution":"true"
        }
      }
    }]
  }
};



