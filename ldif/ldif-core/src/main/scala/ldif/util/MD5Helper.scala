/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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