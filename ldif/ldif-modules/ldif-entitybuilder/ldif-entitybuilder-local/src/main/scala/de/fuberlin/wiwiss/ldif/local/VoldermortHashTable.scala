package de.fuberlin.wiwiss.ldif.local

import ldif.entity.Node
import java.util.ArrayList
import java.util.List
import collection.mutable.HashSet
import voldemort.store.UnreachableStoreException

// Voldermort adapter

class VoldermortHashTable (storeName : String) extends HashTable {
  var store = VoldermortStoreFactory.getStore(storeName)
  var lastKey: List[String] = null
  var values: java.util.List[String] = null

  override def put(key : Pair[Node,String], value: Node) {
    val keyAsList: List[String] = convertKey(key)
    val newValue = convertValue(value)

    if(lastKey==keyAsList) // Key is the same, don't write list back to Voldemort
      values.add(newValue)
    else {
      if(lastKey!=null) { // Not the first run
        putValue(lastKey, values)
        lastKey = keyAsList
      } else // First run
        lastKey = keyAsList
        values = getValue(keyAsList)
      if(values==null)
          values = new ArrayList[String]
      values.add(newValue)
    }
  }

  def finishPut() {
    if(lastKey!=null)
      putValue(lastKey, values)
  }

  override def get(key : Pair[Node,String]) = {
    val result = new HashSet[Node]
    val keyAsList = convertKey(key)
    val values = getValue(keyAsList)

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

  private def getValue(key: List[String]): java.util.List[String] = {
    val retries = 10;
    var tries = 0;

    var throwable: Throwable = null
    while(tries<retries) {
      try {
        return store.getValue(key)
      } catch {
        case t: Throwable => {
          tries += 1;
          throwable=t;
          System.err.println("Voldemort: Store not reachable, waiting 10 seconds, try nr. " + tries)
          VoldermortStoreFactory.reset()
          store = VoldermortStoreFactory.getStore(storeName)
          Thread.sleep(10000)
        }
      }
    }
    throw throwable
  }

  private def putValue(key: List[String], values: List[String]) {
    val retries = 10;
    var tries = 0;

    var throwable: Throwable = null
    while(tries<retries) {
      try {
        store.put(key, values)
        return
      } catch {
        case t: Throwable => {
          tries += 1;
          throwable=t;
          System.err.println("Voldemort: Store not reachable, waiting 10 seconds, try nr. " + tries)
          VoldermortStoreFactory.reset()
          store = VoldermortStoreFactory.getStore(storeName)
          Thread.sleep(10000)
        }
      }
    }
    throw throwable
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