package de.fuberlin.wiwiss.r2r.parser;

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 03.05.11
 * Time: 18:55
 * To change this template use File | Settings | File Templates.
 */
public class Node {
	private String value, datatypeOrLanguage;
	private NodeType nodeType;

  Node(String value, String datatypeOrLanguage, NodeType nodeType) {
	  this.value = value;
	  this.datatypeOrLanguage = datatypeOrLanguage;
	  this.nodeType = nodeType;
  }

  public String datatype() {
  	if(nodeType == NodeType.TYPEDLITERAL)
  		return datatypeOrLanguage;
  	else
  		return null;
  }

  public String language() {
  	if(nodeType == NodeType.LANGUAGELITERAL)
  		return datatypeOrLanguage;
  	else
  		return null;
  }

  public String value() {
  	return value;
  }

  public NodeType nodeType() {
  	return nodeType;
  }

  public static Node createLiteral(String value) {
  	return new Node(value, null, NodeType.LITERAL);
  }

  public static Node createTypedLiteral(String value, String dataType) {
  	return new Node(value,dataType, NodeType.TYPEDLITERAL);
  }

  public static Node createLanguageLiteral(String value, String language) {
  	return new Node(value, language, NodeType.LANGUAGELITERAL);
  }

  public static Node createBlankNode(String value) {
  	return new Node(value, null, NodeType.BLANKNODE);
  }

  public static Node createUriNode(String value) {
  	return new Node(value, null, NodeType.URINODE);
  }

  public static Node createVariableNode(String value) {
  	return new Node(value, null, NodeType.VARIABLENODE);
  }

  public static Node createConstantLiteral(String value) {
  	return new Node(value, null, NodeType.CONSTANTLITERAL);
  }

  public boolean equals(Object other) {
  	if(this==other)	return true;
  	if(!(other instanceof Node)) return false;
  	Node otherNode = (Node) other;
    return (otherNode.nodeType==this.nodeType) &&
           (this.datatypeOrLanguage.equals(otherNode.datatypeOrLanguage)) &&
           (this.value.equals(otherNode.value));
  }

  public int hashCode() {
  	int hash = 1;
  	hash = hash*31 + value.hashCode();
  	hash = hash*31 + datatypeOrLanguage==null ? 0 : datatypeOrLanguage.hashCode();
  	hash = hash*31 + nodeType.hashCode();
  	return hash;
  }

  public String toString() {
  	StringBuilder sb = new StringBuilder();
  	if(nodeType==NodeType.URINODE)
  		sb.append("<");
  	else if(nodeType==NodeType.LITERAL || nodeType==NodeType.TYPEDLITERAL || nodeType==NodeType.LANGUAGELITERAL)
  		sb.append("\"");
  	else if(nodeType==NodeType.VARIABLENODE)
  		sb.append("?");

  	sb.append(value);

  	if(nodeType==NodeType.URINODE)
  		sb.append(">");
  	else if(nodeType==NodeType.LITERAL || nodeType==NodeType.TYPEDLITERAL || nodeType==NodeType.LANGUAGELITERAL)
  		sb.append("\"");

  	if(nodeType==NodeType.LANGUAGELITERAL)
  		sb.append("@").append(datatypeOrLanguage);
  	if(nodeType==NodeType.TYPEDLITERAL)
  		sb.append("^^<").append(datatypeOrLanguage).append(">");
  	return sb.toString();
  }
}
