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

package ldif.local.runtime.impl

import ldif.runtime.QuadReader
import ldif.runtime.QuadWriter

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This QuadReader writes all quads it returns also to a QuadWriter. The QuadWriter is NOT closed in the end.
 */
class CopyQuadReader(reader: QuadReader, writeTo: QuadWriter) extends QuadReader {
  def size = reader.size

  def read() = {
    val value = reader.read()
    writeTo.write(value)
    value
  }

  def hasNext = reader.hasNext
}