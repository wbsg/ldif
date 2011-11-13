package ldif.local

import ldif.entity.Node
import collection.mutable.{HashMap, MultiMap, Set}

// Scala MultiMap adapter

class MemHashTable extends HashTable {

  val hashTable:MultiMap[Pair[Node,String], Node] = new HashMap[Pair[Node,String], Set[Node]] with MultiMap[Pair[Node,String], Node]

  override def put(key : Pair[Node,String], value: Node) {
    hashTable.addBinding(key,value)
  }

  override def get(key : Pair[Node,String]) = {
    hashTable.get(key)
  }

  override def clear {
    hashTable.clear
  }

}