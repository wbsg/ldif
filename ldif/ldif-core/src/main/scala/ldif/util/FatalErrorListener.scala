package ldif.util

trait FatalErrorListener {
  def reportError(e: Exception)
}

object FatalErrorListener extends FatalErrorListener {
  def reportError(e: Exception) {
    e.printStackTrace
    System.exit(1)
  }
}
