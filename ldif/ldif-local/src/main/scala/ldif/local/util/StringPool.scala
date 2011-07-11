package ldif.local.util

import java.util.concurrent.ConcurrentHashMap
import ldif.local.runtime.{ConfigProperties, ConfigParameters}

/**
 * StringPool implements string canonicalization (with an explicit collection)
 * (see http://www.javamex.com/tutorials/memory/string_saving_memory.shtml)
 */

class StringPool(poolSize:Int, maxSize:Int){

  private val map = new ConcurrentHashMap[String,String](poolSize)

  def getCanonicalVersion(str:String) = {
    if (str!=null)  {
      if (map.size > maxSize) {
        map.clear
      }
      val canon = map.putIfAbsent(str, str)
      if (canon == null) str
      else canon
    }
    else null
  }

  def size = map.size

  def reset = map.clear

}

object StringPool extends StringPool(Const.POOL_STARTING_SIZE,Const.POOL_MAX_SIZE)     
