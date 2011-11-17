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

package ldif.datasources.crawl

import ldif.module.ModuleTask
import ldif.util.Identifier
import java.net.URI

 /**
 * Crawl data access task
 *
 * @param levels Max number of levels to crawl (how deep the search is). Default value is 0
 * @param limit Max number of URI to crawl for each round/level for each PLD. Default value is -1
 */

class CrawlTask(override val name : Identifier, val seed : URI, val levels : Int = 0, val limit : Int  = -1) extends ModuleTask