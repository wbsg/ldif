package ldif.local.runtime.impl

import ldif.runtime.{Quad, QuadWriter}
import java.io.{FileWriter, BufferedWriter, File}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 1/31/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

class SerializingQuadWriter(file: File, val syntax: RDFSyntax) extends QuadWriter {
  var writer:BufferedWriter = new BufferedWriter(new FileWriter(file.getAbsolutePath))

  def write(quad: Quad) = {
    syntax match {
      case NTRIPLES => writer.write(quad.toNTripleFormat)
      case NQUADS => writer.write(quad.toNQuadFormat)
    }
    writer.write(" .\n")
  }

  def finish() = {writer.flush(); writer.close()}
}

sealed trait RDFSyntax {val name: String}

case object NTRIPLES extends RDFSyntax {val name = "N-Triples" }

case object NQUADS extends RDFSyntax { val name = "N-Quads"}

