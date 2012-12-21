/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import net.liftweb.util.Helpers._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST
import net.liftweb.http.js.JsCmds.Script
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.SHtml
import net.liftweb.json.Printer.pretty
import ldif.workbench.model.util.JSUtils
import ldif.workbench.model.User
import xml.NodeSeq
import scala.collection.JavaConversions.propertiesAsScalaMap
import net.liftweb.json.JsonAST.{JField, JObject, JArray, JValue}
import ldif.local.Scheduler
import ldif.local.scheduler.{CrawlImportJob, QuadImportJob, SparqlImportJob, TripleImportJob}
import ldif.util.Identifier._
import ldif.local.rest.MonitorServer
import ldif.util.Consts

/**
 * Workspace snippet.
 */
class Workspace {
  def content(xhtml: NodeSeq): NodeSeq = {
    bind("entry", xhtml,
      "injectedJavascript" -> (Script(Workspace.javasScriptFunctions)))
  }
}

object Workspace {

  // To avoid more integration jobs running at same time
  var  runningIntegration = false
  // To avoid more scheduler jobs running at same time
  var  runningScheduler = false

  def javasScriptFunctions = {
    updateCmd &
      hideLoadingDialogCmd &
      injectFunction("createProject", createProject _ ,1) &
//      importProjectFunction &
//      exportProjectFunction &
      injectFunction("setCurrentProject", setCurrentProject _, 1) &
      injectFunction("removeProject", removeProject _ , 0) &
      injectFunction("removeIntegrationJob", removeIntegrationJob _ , 0) &
      injectFunction("removeDataSource", removeDataSource _ , 1) &
      injectFunction("removeImportJob", removeImportJob _ , 1) &
      injectFunction("saveDataSource", saveDataSource _ , 2) &
      injectFunction("saveImportJob", saveImportJob _ , 2) &
      injectFunction("saveIntegrationJob", saveIntegrationJob _ , 2) &
      injectFunction("editMapping", editMapping _ , 2) &
      injectFunction("editLinkSpec", editLinkSpec _ , 2) &
      injectFunction("runScheduler", runScheduler _ , 0) &
      injectFunction("runIntegration", runIntegration _ , 0)
  }

  //TODO Fix - ajax calls are executed more times
  // (see http://groups.google.com/group/liftweb/browse_thread/thread/9ccd5abd81c55449)

  // Injects a Javascript function
  private def injectFunction(name: String, func: (Array[String]) => JsCmd, numberOfParams : Int): JsCmd = {

    //Callback which executes the provided function
    def callback(args: String): JsCmd = {
      try {
        val params =
          if (numberOfParams>0)
            args.split(',')
          else Array.empty[String]
        // check number of parameters
        if (params.length != numberOfParams)
          throw new Exception("Wrong number of parameters for function "+name+
            " [expected: "+numberOfParams+", actual: "+params.size+"]")
        func(params) &
          JSUtils.Log("Ok - " + name + "("+args+")")
      } catch {
        case ex: Exception =>
          Workspace.hideLoadingDialogCmd &
            JSUtils.Log("Error - " + name + "("+args+") > "+ex.getMessage.encJs) &
            JSUtils.Message(ex.getMessage.encJs)
      }
    }

    //Build parameter list
    val params = for (i <- 1 to numberOfParams) yield "p"+i

    //Ajax Call which executes the callback
    val ajaxCall = SHtml.ajaxCall(JsRaw(params.mkString("+','+")), callback _)._2.cmd

    //JavaScript function definition
    JsCmds.Function(name, params.toList, ajaxCall)
  }

  // JS Command which defines the setCurrentProject function
  private def setCurrentProject(args : Array[String]) : JsCmd = {
    User().project = User().workspace.project(args(0))
    JSUtils.Empty
  }

  // JS Command which defines the removeDataSource function
  private def removeDataSource(args : Array[String]) : JsCmd = {
    User().project.dataSourceModule.remove(args(0))
    updateCmd
  }

  // JS Command which defines the removeImportJob function
  private def removeImportJob(args : Array[String]) : JsCmd = {
    User().project.importModule.remove(args(0))
    updateCmd
  }

  // JS Command which defines the removeProject function
  private def removeProject(args : Array[String]) : JsCmd = {
    User().workspace.removeProject(User().project.name)
    User().resetCurrentProject()
    updateCmd
  }

  // JS Command which defines the removeIntegrationJob function
  private def removeIntegrationJob(args : Array[String]) : JsCmd = {
    User().project.integrationModule.remove("integrationJob")
    updateCmd
  }

  // JS Command which defines the saveDataSource function
  private def saveDataSource(args : Array[String]) : JsCmd = {
    User().workspace.saveDataSource(args(0), args(1))
    updateCmd
  }

  // JS Command which defines the saveImportJob function
  private def saveImportJob(args : Array[String]) : JsCmd = {
    User().workspace.saveImportJob(args(0), args(1))
    updateCmd
  }

  // JS Command which defines the saveIntegrationJob function
  private def saveIntegrationJob(args : Array[String]) : JsCmd = {
    User().workspace.saveIntegrationJob("integrationJob", args(0), args(1))
    updateCmd
  }

  // JS Command which defines the editMapping function
  private def editMapping(args : Array[String]) : JsCmd = {
    // args(0) : path to the mapping
    // args(1) : mapping index (for the active project)

    // TODO open R2R editor with args(0)

    JSUtils.Message("R2R Editor has not been integrated yet")
    //updateCmd
  }

  // JS Command which defines the editLinkSpec function
  private def editLinkSpec(args : Array[String]) : JsCmd = {
    // args(0) : the path to the linkSpec
    // args(1) : linkSpec index (for the active project)

    // TODO open Silk Workbench with args(0)

    JSUtils.Message("Silk Workbench has not been integrated yet")
    //updateCmd
  }


  // JS Command which defines the runIntegration function
  private def runIntegration(args : Array[String]) : JsCmd = {
      if (!runningIntegration) {
        runningIntegration =true
        try {
          User().project.integrationModule.tasks.head.job.runIntegration
          runningIntegration = false
          JSUtils.Message("IntegrationJob completed")
        } catch {
          case ex: Exception => {
            runningIntegration = false
            Workspace.hideLoadingDialogCmd & JSUtils.Message(ex.getMessage)
          }
        }
      }
      else JSUtils.Log("An other IntegrationJob is already running, please wait.")
  }

  // JS Command which defines the runScheduler function
  private def runScheduler(args : Array[String])  : JsCmd = {
      if (!runningScheduler)  {
        runningScheduler =true
        try {
          Scheduler(User().project.config).run(runStatusMonitor = true)
          runningScheduler = false
          JSUtils.Message("Scheduler execution completed")
        } catch {
          case ex: Exception => {
            runningScheduler = false
            Workspace.hideLoadingDialogCmd & JSUtils.Message(ex.getMessage)
          }
        }
      }
      else JSUtils.Log("An other Scheduler is already running, please wait. ")
  }

  /**
   * JS Command which updates the workspace view.
   */
  private def updateCmd: JsCmd = {
//    update/align projects and workspace
    //    User().workspace.projects.foreach(_.config)
    if (User().projectOpen) User().project.config
    JsRaw("var workspaceVar = " + pretty(JsonAST.render(workspaceJson)) + "; updateWorkspace(workspaceVar);").cmd
  }

  /**
   * JS Command which defines the createProject function
   */
  private def createProject (args : Array[String]): JsCmd = {
      User().workspace.createProject(args(0))
      updateCmd
  }

  /**
   * JS Command which defines the exportProject function
   */
  private def exportProjectFunction: JsCmd = {
    def callback(projectName: String): JsCmd = {
      User().project = User().workspace.project(projectName)
      JSUtils.Redirect("project.xml")
    }

    val ajaxCall = SHtml.ajaxCall(JsRaw("projectName"), callback _)._2.cmd
    JsCmds.Function("exportProject", "projectName" :: Nil, ajaxCall)
  }

  /**
   * JS Command which hides the loading dialog.
   */
  def hideLoadingDialogCmd: JsCmd = JsRaw("loadingHide();").cmd



  /**
   * Generates a JSON which contains the workspace contents.
   */
  private def workspaceJson: JValue = {

    var projectList: List[JValue] = List()

    for (project <- User().workspace.projects.toSeq.sortBy(n => (n.name.toString.toLowerCase))) {

      val importTasks: JArray = for (job <- project.importModule.tasks.toSeq.sortBy(n => (n.name.toString.toLowerCase)).map(_.job)) yield {
        val common = {
          ("internalId" -> job.id.toString) ~
            ("datasource" -> job.dataSource) ~
            ("refreshSchedule" -> job.refreshSchedule)
        }

        job match {
          case j:TripleImportJob => {
            common ~ ("tripleImportJob" -> {("dumpLocation" -> j.dumpLocation)})
          }

          case j:QuadImportJob => {
            var core = List(JField("dumpLocation", j.dumpLocation))
            if (j.isRenameGraphEnabled) core ::= JField("graphName", j.renameGraphs.toString)
            common ~ ("quadImportJob" -> core)
          }

          case j:CrawlImportJob => {
            val seeds = for(uri <- j.conf.seedUris.toList) yield JField("uri", uri.toString)
            var core = List(JField("seedUris", seeds))
            if (j.conf.isAnyPredicateDefined) {
              val predicates = for(uri <- j.conf.predicatesToFollow.toList) yield JField("uri", uri.toString)
              core ::= JField("predicatesToFollow", predicates)
            }
            if (j.conf.isLevelsDefined) core ::= JField("levels", j.conf.levels.toString)
            if (j.conf.isResourceLimitDefined) core ::= JField("resourceLimit", j.conf.resourceLimit.toString)
            if (j.conf.isRenameGraphEnabled) core ::= JField("graphName", j.conf.renameGraphs.toString)
            common ~ ("crawlImportJob" -> core)
          }

          case j:SparqlImportJob => {
            var core = List(JField("endpointLocation", j.conf.endpointLocation.toString))
            if (j.conf.isTripleLimitDefined) core ::= JField("tripleLimit", j.conf.tripleLimit.toString)
            if (j.conf.isGraphDefined) core ::= JField("graphName", j.conf.graphName.toString)
            if (j.conf.isAnyPatternDefined) {
              val patterns = for(pattern <- j.conf.sparqlPatterns.toList) yield JField("pattern", pattern)
              core ::= JField("sparqlPatterns", patterns)
            }
            common ~ ("sparqlImportJob" -> core)
          }
        }
      }

      val dataSourceTasks: JArray = for (dataSource <- project.dataSourceModule.tasks.toSeq.sortBy(n => (n.name.toString.toLowerCase)).map(_.datasource)) yield {
            ("label" -> dataSource.label) ~
            ("description" -> dataSource.description)
        }

      val integrationTasks: JArray = for (job <- project.integrationModule.tasks.toSeq.sortBy(n => (n.name.toString.toLowerCase)).map(_.job)) yield {
        val jobConfig = job.config

        val integrationProperties : JValue  =
          for ((k, v) <- propertiesAsScalaMap(jobConfig.properties).toList) yield {
            JField(k, v)
          }

        ("sources" -> jobConfig.sources.head) ~
          ("linkSpecifications" -> jobConfig.linkSpecDir.getCanonicalPath) ~
          ("mappings" -> jobConfig.mappingDir.getCanonicalPath) ~
          ("sieve" -> jobConfig.sieveSpecDir.getCanonicalPath) ~
          ("runScheduler" -> jobConfig.runSchedule) ~
          ("configurationProperties" ->  integrationProperties)
      }

      val scheduler: JValue = {
        val schedulerProperties : JValue  =
          for ((k, v) <- propertiesAsScalaMap(project.config.properties).toList) yield {
            JField(k, v)
          }

//        // get importJobs the paths
//        val importJobs : JArray  =
//          for (v <- project.config.importJobsFiles.toList) yield {
//            ("importJob", v.getCanonicalPath)
//          }

        ("dataSources" -> dataSourceTasks) ~
          ("importJobs" -> importTasks) ~
          ("integrationJob" -> integrationTasks) ~
          ("dumpLocation" -> project.config.dumpLocationDir) ~
          ("properties" -> project.config.properties.getProperty("propertiesFile")) ~
          ("configurationProperties" ->  schedulerProperties)
      }

      val proj: JObject = {
        ("name" -> project.name.toString)  ~
          ("scheduler" -> scheduler)
      }

      projectList :+= proj
    }

    val projects = ("project" -> JArray(projectList))
    val activeProject = ("activeProject" -> (if (User().projectOpen) User().project.name.toString else ""))
    //    val activeTask = ("activeTask" -> (if (User().taskOpen) User().task.name.toString else ""))
    //    val activeTaskType = ("activeTaskType" -> (if (User().taskOpen) User().task.getClass.getSimpleName else ""))
    //    ("workspace" -> projects ~ activeProject ~ activeTask ~ activeTaskType)
    ("workspace" -> projects ~ activeProject )
  }
}
