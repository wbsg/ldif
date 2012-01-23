/* 
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

package test

import java.io._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */

object SameAsChecker {
  def main(args: Array[String]) {
    if(args.length<4) {
      println("required parameters: <inputfile1> <inputfile2> <outputfile1> <outputfile2>\nThe output files contain the missing lines for the corresponding input file.")
      sys.exit()
    }
    val input1 = getInputReader(args(0))
    val input2 = getInputReader(args(1))
    val output1 = getOutputWrite(args(2))
    val output2 = getOutputWrite(args(3))
    val debugOutput = getOutputWrite("debugOut")
    var line1 = input1.readLine()
    var line2 = input2.readLine()
    while(line1!=null || line2!=null) {
      if(line1==null || line2==null)
        if(line1!=null) {
          output2.append(line1)
          line1 = input1.readLine()
        } else {
          output1.append(line2)
          line2 = input2.readLine()
        }
      else { // none of the lines are null
        if(line1==line2) { // Both files have the same line, don't output anything
          line1 = input1.readLine()
          line2 = input2.readLine()
        }
        else {
          if(line1<line2) {
            output2.append(line1).append("\n")
            debugOutput.append(line1 + " " + line2).append("\n")
            line1 = input1.readLine()
          } else {
            output1.append(line2).append("\n")
            debugOutput.append(line1 + " " + line2).append("\n")
            line2 = input2.readLine()

          }
        }
      }
    }
    closeWriter(output1)
    closeWriter(output2)
    closeWriter(debugOutput)
  }

  private def getInputReader(file: String) = {
    new BufferedReader(new FileReader(file))
  }

  private def getOutputWrite(file: String) = {
    new BufferedWriter(new FileWriter(new File(file)))
  }

  private def closeWriter(writer: BufferedWriter) {
    writer.flush()
    writer.close()
  }
}