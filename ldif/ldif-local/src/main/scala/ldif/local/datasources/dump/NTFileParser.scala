package ldif.local.datasources.dump

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.05.11
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */

import scala.util.parsing.combinator._
import ldif.local.runtime._
import ldif.local.runtime.impl.QuadQueue
import ldif.entity._
import java.io.{FileReader, BufferedReader}

class NTFileParser extends JavaTokenParsers {
   override val skipWhitespace = false

  def line = rep(ws) ~ opt(comment | triple) ^^ {
    case _~Some(quad) => quad
    case _ => null
  }

  def comment = COMMENT ^^ { case _ => null }

  val COMMENT = "#[\u0020-\u007E]*".r  ^^ { case _ => null }

  def triple = subject ~ rep1(ws) ~ predicate ~ rep1(ws) ~ obj ~ rep1(ws) ~ "." ~ rep(ws) ^^ {
    case subject~_~predicate~_~obj~_~"."~_ => Quad(subject, predicate.value, obj, "default")
  }

  def subject = uriref | namedNode

  def predicate = uriref

  def obj = uriref | namedNode | lit

  def uriref = "<" ~ absoluteUri ~ ">" ^^ { case "<"~uri~">" => Node.createUriNode(uri, "default")}

  def namedNode = "_:" ~ name ^^ { case "_:"~name => Node.createBlankNode(name, "default")

  }

  def lit = langString | datatypeString

  def langString = "\"" ~ characterString ~ "\"" ~ opt("@" ~ language) ^^ {
    case "\"" ~ characters ~ "\"" ~ Some("@" ~ language) => Node.createLanguageLiteral(characters, language, "default")
    case "\"" ~ characters ~ "\"" ~ None => Node.createLiteral(characters, "default")
  }

  def datatypeString = "\"" ~ characterString ~ "\"^^" ~ uriref ^^ {
    case "\"" ~ characters ~ "\"^^" ~ uri => Node.createTypedLiteral(characters, uri.value, "default")
  }

  val language = "[a-z]+(-[a-z0-9]+)*".r

  def ws = space | tab

  def eoln = cr | lf | cr ~ lf

  val space = "\u0020"

  val cr = "\u000D"

  val lf = "\u000A"

  val characterString = "[\u0020-\u0021\u0023-\u007E]*".r

  val tab = "\u0009"

  val name = """[A-Za-z][A-Za-z0-9]*""".r

  val absoluteUri = "[\u0021-\u003B=\u003F-\u007E]+".r
}

object NTFileParser {
  val p = new NTFileParser

  def parseNTLine(line: String): Quad = {
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
        val quad = parseNTLine(line)
        if(quad!=null) {
          quadQueue.write(quad) }
      }
    }
  }

  def main(args: Array[String]) {
//    val reader = new BufferedReader(new FileReader("/home/andreas/test2.nt"))
    val reader = new BufferedReader(new FileReader("/home/andreas/test.nt"))
    val queue = new QuadQueue

    stopWatch.getTimeSpanInSeconds

    readQuads(reader, queue)

    reader.close
    println(stopWatch.getTimeSpanInSeconds + "s")
    println("Queue size: " + queue.size)
  }
}

object stopWatch {
  private var lastTime = System.currentTimeMillis

  def getTimeSpanInSeconds(): Double = {
    val newTime = System.currentTimeMillis
    val span = newTime - lastTime
    lastTime = newTime
    span / 1000.0
  }
}