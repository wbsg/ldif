/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.hadoop

import org.specs.mock.Mockito
import org.mockito.Mockito._
import org.apache.hadoop.io._
import org.apache.hadoop.mapred.OutputCollector
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import ldif.entity.Node


/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 21.07.11
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class TestTest extends FlatSpec with ShouldMatchers {


    it should "A simple test should work" in {
//      val mapper = new MapTest
      val value = new Text("0001950000-001100000")
//      val mocker = mock[OutputCollector[Text, IntWritable]]
//      mapper.map(null, value, mocker, null)
      (true) should equal (true)

//      there was one(mocker).collect(new Text("1950"), new IntWritable(-11))
    }
}
