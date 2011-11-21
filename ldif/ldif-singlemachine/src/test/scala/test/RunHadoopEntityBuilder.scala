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

package test

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/4/11
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */

object RunHadoopEntityBuilder {
  def main(args: Array[String]) {
    if(args.length > 1) {
      RunPhase2.runPhase((args(0)::List(args(1)+"_2")).toArray)
      RunPhase3.runPhase((args(1)+"_2"::List(args(1)+"_3")).toArray)
      RunPhase4.runPhase((args(1)+"_3"::List(args(1)+"_4")).toArray)
    } else {
      sys.error("Not enough arguments! Arguments: <in> <out>")
    }
  }
}