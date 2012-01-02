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

package ldif.local

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import util.JenaResultSetEntityBuilderHelper.Row
import ldif.entity.Node

@RunWith(classOf[JUnitRunner])
class RowTest extends FlatSpec with ShouldMatchers {

  it should "compare rows correctly" in {
    val row1 = new Row
    val row2 = new Row

    val node1 = Node.createUriNode("http://aaa")
    val node2 = Node.createUriNode("http://bbb")
    val node3 = Node.createUriNode("http://ccc")

    row1.append(node1)

    row1.equals(row2) should equal (false)

    row2.append(node1)

    row1.equals(row2) should equal (true)
    row1.hashCode.equals(row2.hashCode) should equal (true)

    row1.append(node2)
    row1.append(node3)

    row2.append(node2)
    row2.append(node3)

    row1.equals(row2) should equal (true)
    row1.hashCode.equals(row2.hashCode) should equal (true)

    row1.append(node2)
    row2.append(node3)

    row1.equals(row2) should equal (false)

  }

}