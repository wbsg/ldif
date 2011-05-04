package de.fuberlin.wiwiss.r2r.parser;

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 03.05.11
 * Time: 19:04
 * To change this template use File | Settings | File Templates.
 */

public class NodeTriple {
	private Node subject, predicate, object;

	public NodeTriple(Node subject, Node predicate, Node object) {
	  super();
	  this.subject = subject;
	  this.predicate = predicate;
	  this.object = object;
  }

	public Node getSubject() {
  	return subject;
  }

	public Node getPredicate() {
  	return predicate;
  }

	public Node getObject() {
  	return object;
  }

	public String toString() {
		return subject + " " + predicate + " " + object;
	}
}
