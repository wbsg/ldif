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
import scala.actors.Actor
import scala.actors.Actor._
import java.util.concurrent.atomic.AtomicInteger

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
  val counter = new AtomicInteger

  val parserActor = actor {
    loop {
      receive {
          case line: String => parseNTLine(line) match {
            case quad: Quad => writerActor ! quad
            case null => println("Found non-quad line")
          }
          case null => println("End..."); writerActor ! null; exit()
      }
    }
  }

  parserActor.start()

  var writerActor: Actor = null

  def parseNTLine(line: String): Quad = {
    p.parseAll(p.line, line) match {
      case p.Success(quad, _) => quad
      case _ => null
    }
  }

  def readQuads(input: BufferedReader, quadQueue: QuadWriter) {
    writerActor = new WriterActor(quadQueue)
    writerActor.start()
    var line: String = null

    var loop = true

    while(loop) {
      val line = input.readLine()

      if(line==null)
        loop = false
      else {
        globalCounter.inc;
        parserActor ! line

//        val quad = parseNTLine(line)
//        if(quad!=null) {
//          quadQueue.write(quad) }
      }
    }
    parserActor ! null
  }

  def main(args: Array[String]) {
//    val reader = new BufferedReader(new FileReader("/home/andreas/test2.nt"))
    val reader = new BufferedReader(new FileReader("/home/andreas/test.nt"))
    val queue = new QuadQueue

    stopWatch.getTimeSpanInSeconds

    readQuads(reader, queue)

    reader.close
    Thread.sleep(40000)
    println(stopWatch.getTimeSpanInSeconds + "s")
    println("Counter: " + globalCounter.get)
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

class WriterActor(quadQueue: QuadWriter) extends Actor {
  def act() = {
    loop {
      receive {
        case quad: Quad =>  quadQueue.write(quad)
        case null => exit()
      }
    }
  }
}

object globalCounter {
  val counter = new AtomicInteger
  var finished = false

  def inc() = counter.incrementAndGet

  def get = counter.get

  def setFinished = true

  def getFinished = finished
}