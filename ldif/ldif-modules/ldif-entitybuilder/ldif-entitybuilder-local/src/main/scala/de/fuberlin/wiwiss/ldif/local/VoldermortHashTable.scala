package de.fuberlin.wiwiss.ldif.local

import ldif.entity.Node
import java.util.ArrayList
import collection.mutable.HashSet

// Voldermort adapter

class VoldermortHashTable (storeName : String) extends HashTable {
  val store = VoldermortStoreFactory.getStore(storeName.asInstanceOf[java.lang.String])

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
        //TODO decode graph info
        result += Node.fromString(it.next)
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
    //TODO add graph name if blankNode
    keyAsList.add( Pair.unapply(key).get._1.value )
    keyAsList.add( Pair.unapply(key).get._2 )
    keyAsList
  }

  private def convertValue (node : Node) = {
    //TODO encode graph info
    node.toString.asInstanceOf[java.lang.String]    
  }




}