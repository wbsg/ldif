package ldif.util

import java.security.MessageDigest

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 19.08.11
 * Time: 14:42
 * To change this template use File | Settings | File Templates.
 */

object MD5Helper {

  def convertToHex(data: Array[Byte]): String = {
    val buf = new StringBuffer()
    for(i <- 0 to (data.length-1)) {
      var halfbyte = (data(i) >>> 4) & 0x0F;
      var two_halfs = 0
      do {
        if ((0 <= halfbyte) && (halfbyte <= 9))
          buf.append(('0' + halfbyte).toChar);
        else
          buf.append(('a' + (halfbyte - 10)).toChar);
        halfbyte = data(i) & 0x0F;
        two_halfs += 1
      } while(two_halfs < 1);
    }
    return buf.toString();
  }

  def md5(text: String): String =  {
    val md = MessageDigest.getInstance("sha")
    md.update(text.getBytes("iso-8859-1"), 0, text.length())
    val md5hash = md.digest()
    return convertToHex(md5hash)
  }

}