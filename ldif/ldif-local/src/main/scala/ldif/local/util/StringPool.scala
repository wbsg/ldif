package ldif.local.util

import java.util.concurrent.ConcurrentHashMap

/**
 * String Canonicalisation with an explicit collection
 * (see http://www.javamex.com/tutorials/memory/string_saving_memory.shtml)
 */

class StringPool(poolSize:Int, maxSize:Int){

  private var map = new ConcurrentHashMap[String,String](poolSize)

  def getCanonicalVersion(str:String) = {
    if (map.size > maxSize) {
      map.clear
    }
    val canon = map.putIfAbsent(str, str)
    if (canon == null)
      str
    else canon
  }
}