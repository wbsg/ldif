/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

package ldif.local.util

//import org.apache.http.conn.params.ConnManagerParams
//import org.apache.http.conn.scheme.{PlainSocketFactory,Scheme,SchemeRegistry}
//import org.apache.http.impl.client.DefaultHttpClient
//import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
//import org.apache.http.params.{BasicHttpParams, CoreProtocolPNames, HttpProtocolParams }
//import org.apache.http.HttpVersion

object HttpClientFactory  {
  val MAX_CONNECTIONS = 100;
  val HTTP_USER_AGENT = "ldif/0.1 (http://www.wiwiss.fu-berlin.de/en/institute/pwo/bizer/index.html)";
  val HTTP_SOCKET_TIMEOUT = 30 * 1000;

	def createHttpClient = {
    //	    /* Create and initialize HTTP parameters */
    //	    val params = new BasicHttpParams
    //	    ConnManagerParams.setMaxTotalConnections(params, MAX_CONNECTIONS)
    //	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1)
    //		  params.setIntParameter("http.connection.timeout", HTTP_SOCKET_TIMEOUT)
    //		  params.setParameter(CoreProtocolPNames.USER_AGENT, HTTP_USER_AGENT)
    //
    //	    /* Create and initialize scheme registry */
    //	    val schemeRegistry = new SchemeRegistry
    //	    schemeRegistry.register( new Scheme("http", PlainSocketFactory.getSocketFactory, 80))
    //
    //	    /*
    //	     * Create an HttpClient with the ThreadSafeClientConnManager.
    //	     * This connection manager must be used if more than one thread will
    //	     * be using the HttpClient.
    //	     */
    //	    val cm = new ThreadSafeClientConnManager(params, schemeRegistry)
    //
    //      new DefaultHttpClient(cm, params)
	}
}