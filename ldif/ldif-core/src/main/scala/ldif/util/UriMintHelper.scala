package ldif.util

import java.net.URLEncoder

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/8/11
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */

object UriMintHelper {
  def mintURI(nameSpace: String, label: String): String = {
    nameSpace + URLEncoder.encode(label.replace(' ', '_'), "UTF-8")
  }

}