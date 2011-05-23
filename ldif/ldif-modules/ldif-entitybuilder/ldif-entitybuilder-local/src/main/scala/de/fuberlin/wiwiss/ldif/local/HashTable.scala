package de.fuberlin.wiwiss.ldif.local

import ldif.entity.Node
import collection.mutable.Set

trait HashTable {
  //TODO key :  Pair[String,String]
  def put(key : Pair[Node,String], value: Node)
  def get(key : Pair[Node,String]) : Option[Set[Node]]
  def clear
}