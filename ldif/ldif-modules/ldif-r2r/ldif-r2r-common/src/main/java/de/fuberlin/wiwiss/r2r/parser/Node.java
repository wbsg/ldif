/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  public static Node createUnknownLexicalNode(String value) {
      return new Node(value, null, NodeType.UNKNOWNLEXICALVALUE);
  }

  public boolean equals(Object other) {
  	if(this==other)	return true;
  	if(!(other instanceof Node)) return false;
  	Node otherNode = (Node) other;
    return (otherNode.nodeType==nodeType) &&
           compareDTorLang(this.datatypeOrLanguage,otherNode.datatypeOrLanguage) &&
           (this.value.equals(otherNode.value));
  }

  private boolean compareDTorLang(String v1, String v2) {
    if(v1==null)
      return v2==null;
    else
      return v1.equals(v2);
  }

  public int hashCode() {
  	int hash = 1;
  	hash = hash*31 + value.hashCode();
  	hash = hash*31 + (datatypeOrLanguage==null ? 0 : datatypeOrLanguage.hashCode());
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
