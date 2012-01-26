//package test
//
//import ldif.modules.silk.SilkModule
//import ldif.modules.silk.hadoop.SilkHadoopExecutor
//import ldif.hadoop.runtime.ConfigParameters._
//import org.apache.hadoop.fs.Path
//import ldif.hadoop.io.EntityMultipleSequenceFileOutput
//import ldif.hadoop.runtime.{ConfigParameters, StaticEntityFormat}
//import de.fuberlin.wiwiss.silk.util.DPair
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//import org.scalatest.FlatSpec
//import org.scalatest.matchers.ShouldMatchers
//import de.fuberlin.wiwiss.silk.plugins.Plugins
//import java.io.File
//
///*
// * LDIF
// *
// * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//@RunWith(classOf[JUnitRunner])
//class SilkTest extends FlatSpec with ShouldMatchers {
//
//  Plugins.register()
//
//
//  "Silk-test" should "works" in {
//
//
//    val linkSpecDir = "/home/andrea/job/ldif/code/ldif/ldif/examples/life-science/linkSpecs/"
//    val entitiesDirectory = "/home/andrea/Desktop/eb-silk"
//    val outputDirectory =  "silk-out"
//
//    val silkModule = SilkModule.load(new File(linkSpecDir))
//    val silkExecutor = new SilkHadoopExecutor
//    val tasks = silkModule.tasks.toIndexedSeq
//
//    for((silkTask, i) <- tasks.zipWithIndex) yield {
//      val sourcePath = new Path(entitiesDirectory, EntityMultipleSequenceFileOutput.generateDirectoryName(i * 2))
//      val targetPath = new Path(entitiesDirectory, EntityMultipleSequenceFileOutput.generateDirectoryName(i * 2 + 1))
//      val outputPath = new Path(outputDirectory, EntityMultipleSequenceFileOutput.generateDirectoryName(i))
//
//      silkExecutor.execute(silkTask, DPair(sourcePath, targetPath), outputPath)
//
//      outputPath
//    }
//
//  }
//
//}