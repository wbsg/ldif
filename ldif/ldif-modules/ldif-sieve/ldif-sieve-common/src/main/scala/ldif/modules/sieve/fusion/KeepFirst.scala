package ldif.modules.sieve.fusion

import ldif.runtime.Quad
import org.slf4j.LoggerFactory
import ldif.entity.{NodeTrait, Node}

/**
 * example fusion function that keeps the first value
 * @author pablomendes
 */

class KeepFirst extends FusionFunction {

  private val log = LoggerFactory.getLogger(getClass.getName)

  override def fuse(values: Traversable[IndexedSeq[NodeTrait]]) : Traversable[IndexedSeq[NodeTrait]] = {
    if (values.nonEmpty) Seq(values.head) else Seq[IndexedSeq[NodeTrait]]()
  }

}

/**
 * Example fusion function that prefers values from certain Graphs given as input.
 * TODO expand to a list of graphs
 * @author pablomendes
 */

class TrustYourFriends(val preferredGraph: String) extends FusionFunction {

  private val log = LoggerFactory.getLogger(getClass.getName)

  override def fuse(values: Traversable[IndexedSeq[NodeTrait]]) : Traversable[IndexedSeq[NodeTrait]] = {
    var kept = IndexedSeq[NodeTrait]()
    values.foreach( n => { // for each property
      // get value for first property path
      val propertyValue = n(0) //TODO treat the case where the path is a tree (more than one property value)
      //log.info("factum:"+propertyValue);
      //log.info("graph:"+propertyValue.graph);
      if (propertyValue.graph == preferredGraph)
        kept = IndexedSeq(propertyValue)
    })
    Seq(kept)
  }

}
