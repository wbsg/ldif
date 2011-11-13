package ldif.util

import java.util.logging.LogManager
import org.slf4j.bridge.SLF4JBridgeHandler

object LogUtil {

  /* Install an instance of SLF4JBridgeHandler
   * as the sole JUL handler in the system.
   * The SLF4JBridgeHandler instance will redirect
   * all JUL log records are redirected to the SLF4J API
   */
  def init { val julLogger = LogManager.getLogManager().getLogger("")
    val handlers = julLogger.getHandlers
    for (handler <- handlers) {
      // Remove JUL's default Handler,
      // so to avoid having everything logged twice
      julLogger.removeHandler(handler)
    }

    SLF4JBridgeHandler.install
  }
}