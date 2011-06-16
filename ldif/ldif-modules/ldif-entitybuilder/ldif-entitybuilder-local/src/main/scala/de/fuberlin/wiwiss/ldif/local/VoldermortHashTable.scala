package de.fuberlin.wiwiss.ldif.local

import ldif.entity.Node
import java.util.ArrayList
import java.util.List
import collection.mutable.HashSet

// Voldermort adapter

class VoldermortHashTable (storeName : String) extends HashTable {
  val store = VoldermortStoreFactory.getStore(storeName)
  var lastKey: List[String] = null
  var values: java.util.List[String] = null

  override def put(key : Pair[Node,String], value: Node) {
    val keyAsList: List[String] = convertKey(key)
    val newValue = convertValue(value)

    if(lastKey==keyAsList) // Key is the same, don't write list back to Voldemort
      values.add(newValue)
    else {
      if(lastKey!=null) { // Not the first run
        store.put(lastKey, values)
        lastKey = keyAsList
      } else // First run
        lastKey = keyAsList
      values = store.getValue(keyAsList)
      if(values==null)
          values = new ArrayList[String]
      values.add(newValue)
    }
  }

  def finishPut() {
    if(lastKey!=null)
      store.put(lastKey, values)
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