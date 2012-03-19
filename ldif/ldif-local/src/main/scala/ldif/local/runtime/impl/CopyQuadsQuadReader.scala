package ldif.local.runtime.impl

import ldif.local.runtime.QuadReader
import ldif.runtime.{Quad, QuadWriter}
import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/19/12
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
class CopyQuadsQuadReader(inputQuadReader: QuadReader, filterTerms: Set[String], filteredQuads: QuadWriter) extends QuadReader{
  def size = inputQuadReader.size

  /**
   * If the class or the property of a quad is found in filterTerms then output to quad writer
   */
  def read(): Quad = {
    val quad = inputQuadReader.read()
    if(quad.predicate==Consts.RDFTYPE_URI && filterTerms.contains(quad.value.value))
      filteredQuads.write(quad)
    else if(filterTerms.contains(quad.predicate))
      filteredQuads.write(quad)

    return quad
  }

  def hasNext = inputQuadReader.hasNext
}