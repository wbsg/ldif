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
   * Count how many of the characters in a String are letters (of any language)
   * Used as heuristic to measure how "readable" a String is.
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
   * Count how many of the characters in a String are "non-legit" characters (not [a-zA-Z()]
   * Used as heuristic to measure how "readable" a String is to Anglo-Saxonic and Latin eyes.
   */
  def countNonLegitChars(string: String) = {
    var nLetters = 0;
    val nChars = string.length()
    for(i <- 0 until nChars) {
      val char = string.charAt(i)
      if(!(char >= 'a' && char <= 'z' || char >= 'A' && char <='Z' || char == '(' || char==')'))
        nLetters = nLetters + 1
    };
    nLetters
  }

  /**
   * This function is used to decide when String "left" is considered "smaller" (worse) than the String "right".
   * First they are compared based on countNonLegitChars. If they are the same there use lexicographic oder.
   */
  def lessThan(left: String, right: String): Boolean = {
    val leftCount = countNonLegitChars(left)
    val rightCount = countNonLegitChars(right)
    if(leftCount==rightCount)
      left < right
    else
      leftCount > rightCount
  }
}