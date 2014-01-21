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

package ldif.workbench.snippet

import xml.{XML, NodeSeq}
import net.liftweb.util.Helpers._
import ldif.local.scheduler.ImportJob
import ldif.workbench.model.User
import ldif.workbench.model.modules.ImportTask
import net.liftweb.http.{S, SHtml, FileParamHolder}

/**
 * Dialog to import importJobs into the workspace.
 */

class ImportImportJobDialog {

  def render(xhtml: NodeSeq): NodeSeq = {
    var fileHolder: FileParamHolder = null

    bind("entry", xhtml,
      "file" -> SHtml.fileUpload(fileHolder = _),
      "submit" -> SHtml.submit("Import", () => submit(fileHolder), "style" -> "float:right;"))
  }

  def submit(fileHolder: FileParamHolder) =
    try {
      fileHolder match {
        case FileParamHolder(_, mime, _, data) => {
          val dataString = new String(data)
          val job = ImportJob.fromXML(XML.loadString(dataString))
          User().project.importModule.update(ImportTask(job))
        }
        case _ =>
      }
    }
    catch {
      case ex: Exception => S.warning("Error importing ImportJob: " + ex.getMessage)
    }

}
