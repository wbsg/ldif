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

package ldif.util

import java.util.logging.LogManager
//import org.slf4j.bridge.SLF4JBridgeHandler

object LogUtil {

  /* Install an instance of SLF4JBridgeHandler
   * as the sole JUL handler in the system.
   * The SLF4JBridgeHandler instance will redirect
   * all JUL log records are redirected to the SLF4J API
   */
  def init { val julLogger = LogManager.getLogManager().getLogger("")
//    val handlers = julLogger.getHandlers
//    for (handler <- handlers) {
//      // Remove JUL's default Handler,
//      // so to avoid having everything logged twice
//      julLogger.removeHandler(handler)
//    }
//
//    SLF4JBridgeHandler.install
  }
}