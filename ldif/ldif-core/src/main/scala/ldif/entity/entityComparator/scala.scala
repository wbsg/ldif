package ldif.entity.entityComparator

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/30/11
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */

object entityComparator {
    /**
   * Count how many of the characters in a String are alpha-numeric [a-zA-Z0-9]
   * Used as heuristic to measure how "readable" a String is to Anglo-Saxonic and Latin eyes.
   * @author pablomendes
   */
  def countAlphaNumChars(string: String) = {
    var nLetters = 0;
    val nChars = string.length()
    for(i <- 0 until nChars) {
      if(Character.isLetter(string.charAt(i)))
        nLetters = nLetters + 1
    };
    nLetters.toDouble / nChars
  }

  /**
   * This function is used to decide when String "left" is considered smaller than the String "right".
   * First they are compared based on countAlphaNumChars. If they are the same there use lexicographic oder.
   */
  def lessThan(left: String, right: String): Boolean = {
    val leftCount = countAlphaNumChars(left)
    val rightCount = countAlphaNumChars(right)
    if(leftCount==rightCount)
      left < right
    else
      leftCount < rightCount
  }
}