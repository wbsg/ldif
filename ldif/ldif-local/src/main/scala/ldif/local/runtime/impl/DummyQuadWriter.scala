package ldif.local.runtime.impl

import ldif.local.runtime.QuadWriter
import ldif.runtime.Quad

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 23.08.11
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */

class DummyQuadWriter extends QuadWriter {
  def write(quad: Quad) = null

  def finish = {}
}