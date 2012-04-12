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

package ldif.local.rest

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */

import com.sun.jersey.api.container.httpserver.HttpServerFactory
import com.sun.net.httpserver.HttpServer
import ldif.local.IntegrationJobStatusMonitor
import javax.ws.rs._
import core.Response
import ldif.util._

@Path("/")
class MonitorServer {
  @GET @Produces(Array[String]("text/plain"))
  def getJobsText(): String = {
    MonitorServer.generalStatusMonitor.getText
  }

  @GET @Produces(Array[String]("text/html"))
  def getJobsHtml(
    @DefaultValue("0") @QueryParam("refresh") refreshTimeInSeconds: Int): String = {
    MonitorServer.generalStatusMonitor.getHtml(Map("refresh" -> refreshTimeInSeconds.toString ))
  }
}

@Path("/{jobtype}")
class IntegrationJobMonitorServer {
  @GET @Produces(Array[String]("text/html"))
  @Path("/{index}")
  def getIntegrationJobHtml(
         @PathParam("index") index: Int,
         @PathParam("jobtype") jobtype: String,
         @DefaultValue("0") @QueryParam("refresh") refreshTimeInSeconds: Int): String = {
    val jobPublisher = MonitorServer.generalStatusMonitor.getPublisher(index)

    if(jobPublisher!=None && jobPublisher.get.isInstanceOf[StatusMonitor] && jobPublisher.get.getLink!=None && jobPublisher.get.getLink.get==jobtype) {
      val statusMonitor = jobPublisher.get.asInstanceOf[StatusMonitor]
      return statusMonitor.getHtml(Map("refresh" -> refreshTimeInSeconds.toString ))
    } else
      throw new WebApplicationException(Response.Status.NOT_FOUND)
  }

  @GET @Produces(Array[String]("text/plain"))
  @Path("/{index}")
  def getIntegrationJobHtml(
         @PathParam("index") index: Int): String = {
    val integrationJobMonitor = MonitorServer.generalStatusMonitor.getPublisher(index)
    if(integrationJobMonitor.isInstanceOf[IntegrationJobStatusMonitor])
      return integrationJobMonitor.asInstanceOf[IntegrationJobStatusMonitor].getText
    else
      throw new WebApplicationException(Response.Status.NOT_FOUND)
  }
}



object  MonitorServer {
  val generalStatusMonitor: StatusMonitor with ReportRegister = JobMonitor
  private var server: HttpServer = null

  def stop() {
    server.stop(0)
  }

  def start(uri: String) {
    if(server!=null)
      stop()
    server = HttpServerFactory.create(uri)
    server.start()
  }

}

object dummyStatusMonitor extends StatusMonitor {
  def getHtml(params: Map[String, String]) = "<html><head><title>No status monitor found</title></head><body><b>If you see this message then there is no status monitor implemented, yet, for this URI!</b></body></html>"

  def getText = "If you see this message then there is no status monitor implemented, yet, for this URI."
}