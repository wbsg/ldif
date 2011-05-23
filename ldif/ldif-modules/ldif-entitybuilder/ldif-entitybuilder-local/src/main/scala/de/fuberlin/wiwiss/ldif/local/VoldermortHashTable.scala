package de.fuberlin.wiwiss.ldif.local

import ldif.entity.Node
import java.util.ArrayList
import collection.mutable.HashSet

// Voldermort adapter

class VoldermortHashTable (storeName : String) extends HashTable {
  val store = VoldermortStoreFactory.getStore(storeName.asInstanceOf[java.lang.String])

  override def put(key : Pair[Node,String], value: Node) {
    val keyAsList = convertKey(key)
    //TODO encode graph info
    val newValue = value.value.asInstanceOf[java.lang.String]

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
        result += Node.fromString(it.next,null)
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
    keyAsList.add( Pair.unapply(key).get._1.value )
    keyAsList.add( Pair.unapply(key).get._2 )
    keyAsList
  }

}