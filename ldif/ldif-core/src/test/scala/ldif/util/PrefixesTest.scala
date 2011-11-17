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

package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12.05.11
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class PrefixesTest extends FlatSpec {
  behavior of "the standard Prefixes object"

  it should "resolve xsd:Int" in {
    assert(Prefixes.resolveStandardQName("xsd:Int")=="http://www.w3.org/2001/XMLSchema#Int")
  }

  it should "pass through http://www.w3.org/2001/XMLSchema#Int" in {
    assert(Prefixes.resolveStandardQName("http://www.w3.org/2001/XMLSchema#Int")=="http://www.w3.org/2001/XMLSchema#Int")
  }

  it should "fail to resolve unknown prefix" in {
    intercept[IllegalArgumentException] {
      Prefixes.resolveStandardQName("unknownprefix:name")
    }
  }

  it should "signal if no qName was given as parameter" in {
    intercept[IllegalArgumentException] {
      Prefixes.resolveStandardQName("noQname")
    }
  }
}