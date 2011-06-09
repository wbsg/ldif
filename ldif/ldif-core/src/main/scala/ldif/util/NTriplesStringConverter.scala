package ldif.util

import java.io.CharConversionException

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 09.06.11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */

class NTriplesStringConverter

object NTriplesStringConverter {

  def convertFromEscapedString(value: String): String = {
    val sb = new java.lang.StringBuilder

    // iterate over code points (http://blogs.sun.com/darcy/entry/iterating_over_codepoints)
    val inputLength = value.length
    var offset = 0

    while(offset < inputLength)
    {
      val c = value.charAt(offset)
      if(c!='\\')
        sb append c
      else {
        offset += 1
        val specialChar = value.charAt(offset)
        specialChar match {
          case '"' => sb append '"'
          case 't' => sb append '\t'
          case 'r' => sb append '\r'
          case '\\' => sb append '\\'
          case 'n' => sb append '\n'
          case 'u' => {
            offset += 1
            val codepoint = value.substring(offset, offset + 4)
            val character = Integer.parseInt(codepoint, 16).asInstanceOf[Char]
            sb append character
            offset += 3
          }
          case 'U' => {
            offset += 1
            val codepoint = value.substring(offset, offset + 8)
            val character = Integer.parseInt(codepoint, 16)
            sb appendCodePoint character
            offset += 7
          }
        }
      }
      offset += 1
    }
    sb.toString
  }

  def convertToEscapedString(input: String): String = {
    val sb = new StringBuilder

    // iterate over code points (http://blogs.sun.com/darcy/entry/iterating_over_codepoints)
    val inputLength = input.length
    var offset = 0

    while (offset < inputLength)
    {
      val c = input.codePointAt(offset)
      offset += Character.charCount(c)
      //Ported from Jena's NTripleWriter
     	if (c == '\\' || c == '"')
    	{
    		sb append '\\' append c.toChar
    	}	else if (c == '\n')	{
    		sb append "\\n"
    	}	else if (c == '\r')	{
    		sb append "\\r";
    	}	else if (c == '\t')	{
    		sb append "\\t"
    	}	else if (c >= 32 && c < 127) {
    		sb append c.toChar
    	}	else {
    		val hexStr = c.toHexString.toUpperCase
        val hexStrLen = hexStr.length

        if (c <= 0xffff)
        {
        // 16-bit code point
          sb append "\\u"
          sb append "0" * (4 - hexStrLen)  // leading zeros
        } else if (c <= 0x10ffff) { // biggest representable code point
          // 32-bit code point
          sb append "\\U"
          sb append "0" * (8 - hexStrLen)  // leading zeros
        } else {
          throw new CharConversionException("code point "+c+" outside of range (0x0000..0x10ffff)")
        }
       	sb append hexStr
    	}
    }

    sb.toString
  }
}
