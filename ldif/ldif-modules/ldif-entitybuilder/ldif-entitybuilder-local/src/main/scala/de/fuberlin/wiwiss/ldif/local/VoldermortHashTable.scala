package de.fuberlin.wiwiss.ldif.local

import ldif.entity.Node
import java.util.ArrayList
import collection.mutable.HashSet

// Voldermort adapter

class VoldermortHashTable (storeName : String) extends HashTable {
  val store = VoldermortStoreFactory.getStore(storeName)

  override def put(key : Pair[Node,String], value: Node) {
    val keyAsList = convertKey(key)
    val newValue = convertValue(value)

    val prev = store.getValue(keyAsList)
    if (prev!=null){
      prev.add(newValue)
      store.put(keyAsList, prev)
    }
    else {
      val valueAsList = new ArrayList[String]
      valueAsList.add(newValue)
      store.put(keyAsList,valueAsList)
    }

  }

  override def get(key : Pair[Node,String]) = {
    val result = new HashSet[Node]
    val keyAsList = convertKey(key)
    val values = store.getValue(keyAsList)
    if(values!=null){
      val it = values.iterator
      while(it.hasNext())  {
        result += decodeGraphVal(it.next)
      }
    }
    if (result.isEmpty)
      None
    else
      Some(result)
  }

  override def clear {
    //TODO empty the store
  }

  private def convertKey (key : Pair[Node,String]) = {
    val keyAsList = new ArrayList[String]
    val node = Pair.unapply(key).get._1
    // add graph name if blankNode
    if(node.isBlankNode)
      keyAsList.add(encodeGraphVal(node))
    else
      keyAsList.add( node.value )
    keyAsList.add( Pair.unapply(key).get._2 )
    keyAsList
  }

  private def convertValue (node : Node) = {
    encodeGraphVal(node)
  }

  private def encodeGraphVal (node : Node) = {
    node.toString +"@^" + node.graph
  }

  private def decodeGraphVal (nodeStr : String) = {
    val i = nodeStr.lastIndexOf("@^")
    Node.fromString(nodeStr.substring(0,i),nodeStr.substring(i+2))
  }



}