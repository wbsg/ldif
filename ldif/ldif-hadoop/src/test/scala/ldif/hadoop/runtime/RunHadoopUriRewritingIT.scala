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

package ldif.hadoop.runtime

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 1/3/12
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class RunHadoopUriRewritingIT extends FlatSpec with ShouldMatchers{
  it should "rewrite all URIs correctly" in {
    val inputFile = getClass.getClassLoader.getResource("test/input.nt").getPath
    val sameAsFile = getClass.getClassLoader.getResource("test/sameAs.nt").getPath
    RunHadoopQuadConverter.execute(inputFile, "t/input")
    RunHadoopQuadConverter.execute(sameAsFile, "t/sameas")
    RunHadoopUriRewriting.execute("t/input", "t/sameas", "t/output_seq")
    HadoopQuadToTextConverter.execute("t/output_seq", "t/output")
  }
}