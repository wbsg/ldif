package ldif.util

class StopWatch {

  private var lastTime = System.currentTimeMillis

  def getTimeSpanInSeconds(): Double = {
    val newTime = System.currentTimeMillis
    val span = newTime - lastTime
    lastTime = newTime
    span / 1000.0
  }

}