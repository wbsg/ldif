/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

$(document).ready(function() {
    var $dialog = $('<div id="about_dialog"></div>')
        .html(
            '<p style="font-weight: bold;">LDIF Workbench</p>' +
            '<p style="margin:5px;">Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH &amp; Co. KG</p>' +
            '<p style="margin:5px;">Licensed under the <a target="_blank" href="http://www.apache.org/licenses/LICENSE-2.0">Apache License, Version 2.0</a></p>' +
            '<p style="font-weight: bold;">Website</p>' +
            '<p style="margin:5px;"><a target="_blank" href="http://ldif.wbsg.de">http://ldif.wbsg.de</a></p>' +
            '<p style="font-weight: bold;">Acknowledgements</p>' +
            '<p style="margin:5px;">This work was supported in part by Vulcan Inc. as part of its <a target="_blank" href="http://www.projecthalo.com">Project Halo</a> and by the EU FP7 project <a target="_blank" href="http://lod2.eu/">LOD2 - Creating Knowledge out of Interlinked Data</a> (Grant No. 257943).</p>' +
            '<p style="margin:5px;">The icons were created by <a target="_blank" href="http://p.yusukekamiyamane.com/">Yusuke Kamiyamane</a> and are licensed under the <a target="_blank" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 license</a>.</p>')
        .dialog({
            autoOpen: false,
            title: 'About',
            width: 700,
            modal: true,
            position: ['center', 150]
        });
    $('#about').click(function() {
        $dialog.dialog('open');
        return false;
    });
});
