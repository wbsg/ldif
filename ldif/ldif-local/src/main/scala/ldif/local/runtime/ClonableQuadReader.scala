package ldif.local.runtime

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 31.08.11
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */

trait ClonableQuadReader extends QuadReader {
  def cloneReader: QuadReader
}