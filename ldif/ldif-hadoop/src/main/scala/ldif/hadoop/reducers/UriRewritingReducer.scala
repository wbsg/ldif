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

package ldif.hadoop.reducers

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.lib.MultipleOutputs
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.mapred._
import ldif.entity.entityComparator.entityComparator
import ldif.hadoop.types.QuadWritable
import java.util.Iterator
import collection.mutable.HashSet
import ldif.util.Consts
import ldif.entity.{Node, NodeWritable}
import ldif.hadoop.runtime.RewriteObjectUris
import java.io.File
import ldif.runtime.Quad
import org.apache.hadoop.io.{Text, WritableUtils, NullWritable}
import ldif.runtime.impl.{FileObjectReader, FileObjectWriter}
import ldif.util.{MemoryUsage, Consts}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */

class UriRewritingReducer extends MapReduceBase with Reducer[NodeWritable, QuadWritable, NullWritable, QuadWritable] {
  private var mos: MultipleOutputs = null
  private var config: Configuration = null
  private var rewriteObjectNode = false

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
    config = conf
    rewriteObjectNode = HadoopHelper.getDistributedObject(conf,"rewriteObjectUris").asInstanceOf[RewriteObjectUris].value
  }

  /**
   * This reducer rewrites either the subject or object part of all quads. The rewrite node is picked
   * from sameAs links (interpreted as directed links from left to right). These sameAs links are not
   * rewritten and also not output. The reducer also removes quad duplicates.
   */
  def reduce(entity: NodeWritable, quads: Iterator[QuadWritable], output: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    val quadCollection = new QuadCollection
    var reWriteNode: NodeWritable = null
    while(quads.hasNext) {
      val quad: QuadWritable = quads.next()
      // Find the "largest" rewrite node in all (should usually be at most one) sameAs links
      // blank nodes are always smaller than nodes of other types
      if(quad.property.toString==Consts.SAMEAS_URI) {
        if(reWriteNode==null || reWriteNode.compare(quad.obj) < 0)
          reWriteNode = WritableUtils.clone[NodeWritable](quad.obj, config) }
      else
        quadCollection.add(WritableUtils.clone[QuadWritable](quad, config))
    }
    quadCollection.finish()

    for(quad <- quadCollection) {
      // Don't rewrite quad if rewrite node is a blank node from another graph than the quad itself
      if(reWriteNode!=null && (reWriteNode.nodeType!=Node.BlankNode || reWriteNode.graph==quad.graph)) {
        if(rewriteObjectNode)
          quad.obj = reWriteNode
        else
          quad.subject = reWriteNode
      }
      output.collect(NullWritable.get(), quad)
    }
  }

  override def close() {
    mos.close()
  }
}

class QuadCollection {
  var quadSet = new HashSet[QuadWritable]
  var quadFileWriter: FileQuadWritableWriter = null
  private val spillToDiskThreshold = 10000

  def add(quad: QuadWritable) {
    if(quadFileWriter==null && (MemoryUsage.getFreeMemoryInBytes() < 16777216 || quadSet.size > spillToDiskThreshold))
      spillToDisk

    if(quadFileWriter==null)
      quadSet.add(quad)
    else
      quadFileWriter.write(quad.asQuad)
  }

  private def spillToDisk {
    val tempFile = File.createTempFile("ldif-urirewriting-spill", ".dat")
    tempFile.deleteOnExit()
    quadFileWriter = new FileQuadWritableWriter(tempFile)
    for(quad <- quadSet)
      quadFileWriter.write(quad.asQuad)
    quadSet = null
  }

  def finish() {
    if(quadFileWriter!=null)
      quadFileWriter.finish
  }

  def foreach(f: QuadWritable => Unit) {
    if(quadFileWriter==null)
     for(quad <- quadSet)
        f(quad)
    else {
      val quadFileReader = new FileQuadWritableReader(quadFileWriter.outputFile)
      while(quadFileReader.hasNext)
        f(new QuadWritable(quadFileReader.read()))
    }
  }
}

class FileQuadWritableWriter(outputFile: File) extends FileObjectWriter[Quad](outputFile, NoQuadLeft)

class FileQuadWritableReader(inputFile: File) extends FileObjectReader[Quad](inputFile, NoQuadLeft)

case object NoQuadLeft extends Quad(null, "", null, "")


