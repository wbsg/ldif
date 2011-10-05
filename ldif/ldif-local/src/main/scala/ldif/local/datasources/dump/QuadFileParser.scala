package ldif.local.datasources.dump

import scala.util.parsing.combinator._
import ldif.local.runtime._
import ldif.local.runtime.impl.QuadQueue
import ldif.entity._
import java.util.concurrent.atomic.AtomicInteger
import java.io.{FileReader, BufferedReader}
import ldif.runtime.Quad
import ldif.util.{StopWatch, NTriplesStringConverter}


/**
 * A Quad file parser that currently only allows URIs as context
 */
class QuadFileParser(val graphURI: String) extends JavaTokenParsers {

  def this() {
    this("default")
  }

  override val skipWhitespace = false

  def line = rep(ws) ~ opt(comment | triple | quad) ^^ {
    case _~Some(quad) => quad
    case _ => null
  }

  def comment = COMMENT ^^ { case _ => null }

  val COMMENT = "#[\u0020-\u007E]*".r  ^^ { case _ => null }

  def quad = subject ~ rep1(ws) ~ predicate ~ rep1(ws) ~ obj ~ rep1(ws) ~ graph ~ rep1(ws) ~ "." ~ rep(ws) ^^ {
    case subject~_~predicate~_~obj~_~graph~_~"."~_ => Quad(subject.modifyGraph(graph.value), predicate.value, obj.modifyGraph(graph.value), graph.value)
  }

  def triple = subject ~ rep1(ws) ~ predicate ~ rep1(ws) ~ obj ~ rep1(ws) ~ "." ~ rep(ws) ^^ {
    case subject~_~predicate~_~obj~_~"."~_ => Quad(subject.modifyGraph(graphURI), predicate.value, obj.modifyGraph(graphURI), graphURI)
  }

  def subject = uriref | namedNode

  def predicate = uriref

  def obj = uriref | namedNode | lit

  def graph = uriref

  def uriref = "<" ~ absoluteUri ~ ">" ^^ { case "<"~uri~">" => Node.createUriNode(uri, null)}

  def namedNode = "_:" ~ name ^^ { case "_:"~name => Node.createBlankNode(name, null)

  }

  def lit = langString | datatypeString

  def langString = "\"" ~ characterString ~ "\"" ~ opt("@" ~ language) ^^ {
    case "\"" ~ characters ~ "\"" ~ Some("@" ~ language) => Node.createLanguageLiteral(characters, language, null)
    case "\"" ~ characters ~ "\"" ~ None => Node.createLiteral(characters, null)
  }

  def datatypeString = "\"" ~ characterString ~ "\"^^" ~ uriref ^^ {
    case "\"" ~ characters ~ "\"^^" ~ uri => Node.createTypedLiteral(characters, uri.value, null)
  }

  val language = "[a-z]+(-[a-z0-9]+)*".r

  def ws = space | tab

  def eoln = cr | lf | cr ~ lf

  val space = "\u0020"

  val cr = "\u000D"

  val lf = "\u000A"

  val characterString = """(?:[\u0020\u0021\u0023-\[\]-\u007E]|\\(["nt\\r]|u[0-9a-fA-F]{4}|U[0-9a-fA-F]{8}))*""".r ^^
                        { case str => NTriplesStringConverter.convertFromEscapedString(str) }

  val tab = "\u0009"

  val name = """[A-Za-z][A-Za-z0-9]*""".r

  val absoluteUri = """(?:[\u0021-\u003B=\u003F-\[\]-\u007E]|\\(["nt\\r]|u[0-9a-fA-F]{4}|U[0-9a-fA-F]{8}))+""".r ^^
                    { case str => NTriplesStringConverter.convertFromEscapedString(str) }
}

object QuadFileParser {
  val p = new QuadFileParser
  val counter = new AtomicInteger

  def parseNTLine(line: String): Quad = {
    p.parseAll(p.line, line) match {
      case p.Success(quad, _) => quad
      case _ => null
    }
  }

  def parseQuadLine(line: String): Quad = {
    try
    p.parseAll(p.line, line) match {
      case p.Success(quad, _) => quad
      case _ => null
    }
  }

  def readQuads(input: BufferedReader, quadQueue: QuadWriter) {
    var line: String = null

    var loop = true

    while(loop) {
      val line = input.readLine()

      if(line==null)
        loop = false
      else {
        val quad = parseQuadLine(line)
        if(quad!=null) {
          quadQueue.write(quad) }
      }
    }
  }

  def main(args: Array[String]) {
//    val reader = new BufferedReader(new FileReader("/home/andreas/minimaltest.nt"))
    val reader = new BufferedReader(new FileReader("/home/andreas/test.nt"))
    val queue = new QuadQueue

    println("Starting to read file...")
    val watch = new StopWatch
    watch.getTimeSpanInSeconds

    readQuads(reader, queue)

    reader.close

    println(watch.getTimeSpanInSeconds + "s")
    println("Queue size: " + queue.size)
  }
}
