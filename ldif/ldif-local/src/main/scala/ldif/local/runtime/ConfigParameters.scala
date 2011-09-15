package ldif.local.runtime

import java.util.Properties

case class ConfigParameters(val configProperties: Properties, val otherQuadsWriter: QuadWriter = null, val sameAsWriter: QuadWriter = null)


