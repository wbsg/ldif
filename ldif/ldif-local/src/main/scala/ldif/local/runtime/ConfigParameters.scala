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

package ldif.local.runtime

import impl.FileQuadWriter
import java.util.Properties

//TODO This class is used to configure both Entity Builders and DumpQuadReader, maybe we should split that in two different classes
case class ConfigParameters(configProperties: Properties, otherQuadsWriter: FileQuadWriter = null, sameAsWriter: FileQuadWriter = null, provenanceQuadsWriter: FileQuadWriter = null, passOnToSieveWriter: FileQuadWriter = null, collectNotUsedQuads : Boolean = false)


