package ldif.local.util

import java.util.concurrent.ConcurrentHashMap
import ldif.util.Consts

/**
 * StringPool implements string canonicalization (with an explicit collection)
 * (see http://www.javamex.com/tutorials/memory/string_saving_memory.shtml)
 */

class StringPool(poolSize : Int, maxSize : Int){

  private val map = new ConcurrentHashMap[String,String](poolSize)
  private val encoder = new ConcurrentHashMap[String,Int](poolSize)
  private val decoder = new ConcurrentHashMap[Int,String](poolSize)

  def getCanonicalVersion(input : String, enableCompression : Boolean = false) : String  = {
    var str = input
    if (str != null)  {
      if (map.size > maxSize) {
        map.clear
      }
      if (enableCompression)
        str = compress(input)
      val canon = map.putIfAbsent(str, str)
      if (canon == null) str
      else canon
    }
    else null
  }

  def size = map.size

  def reset = map.clear

  // Simple URI prefix compression
  private def compress(str : String) : String = {
    var pos = str.lastIndexOf("#")+1
    if (pos == 0) pos = str.lastIndexOf("/")+1
    if(pos > 0) {
      val pref = str.substring(0,pos)
      val code = encoder.putIfAbsent(pref,encoder.size+1)
      if (code == 0) {
        decoder.put(encoder.size,pref)
        "<"+(encoder.size).toString+"<"+str.substring(pos)
      }
      else "<"+code.toString+"<"+str.substring(pos)
    }
    else str
  }

  def decompress(str : String) = {
    if(str != null && str.startsWith("<"))  {
      val p = str.lastIndexOf("<")
      if (p!=0)  {
        decoder.get(str.substring(1,p).toInt) + str.substring(p+1)
      }
      else
        str
    }
    else str
  }
}

object StringPool extends StringPool(Consts.POOL_STARTING_SIZE,Consts.POOL_MAX_SIZE)
