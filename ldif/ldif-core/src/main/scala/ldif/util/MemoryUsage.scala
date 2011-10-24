package ldif.util

object MemoryUsage {
  def getMemoryUsage(): Long = {
    val runtime = Runtime.getRuntime
    runtime.gc()
    Thread.sleep(2000)
    val used = runtime.totalMemory - runtime.freeMemory
    runtime.gc()
    Thread.sleep(2000)
    return (math.min(used, runtime.totalMemory - runtime.freeMemory) / (1024)).toLong
  }
}