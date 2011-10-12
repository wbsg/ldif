package ldif.util

object MemoryUsage {
  def getMemoryUsage(): Double = {
    val runtime = Runtime.getRuntime
    runtime.gc()
    Thread.sleep(10000)
    val used = runtime.totalMemory - runtime.freeMemory
    runtime.gc()
    Thread.sleep(5000)
    return (math.min(used, runtime.totalMemory - runtime.freeMemory) / (1024*1024)).toLong
  }
}