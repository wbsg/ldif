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

package ldif.util

import java.io._
import org.xml.sax.InputSource

/**
 * Parses an XML input source and validates it against the schema.
 */
class ValidatingXMLReader[T](deserializer: File => T, schemaPath: String) {

  def apply(file: File): T = {
    val inputStream = new FileInputStream(file)
    try {
      new XMLReader().read(new InputSource(inputStream), schemaPath)
    }
    finally {
      inputStream.close()
    }
    deserializer(file)
  }
}

/**
 * Parses an XML input source (including a boolean parameter) and validates it against the schema.
 */
class ValidatingXMLReaderWithBool[T](deserializer: (File, Boolean) => T, schemaPath: String) {

  def apply(file: File, bool: Boolean = false): T = {
    val inputStream = new FileInputStream(file)
    try {
      new XMLReader().read(new InputSource(inputStream), schemaPath)
    }
    finally {
      inputStream.close()
    }
    deserializer(file, bool)
  }
}
