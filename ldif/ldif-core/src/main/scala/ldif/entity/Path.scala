/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.entity

import collection.mutable.{SynchronizedMap, WeakHashMap}
import ldif.util.Prefixes
import actors.threadpool.AtomicInteger

/**
 * Represents an RDF path.
 */
case class Path(variable : String, operators : List[PathOperator])
{
  val gid = Path.pathCounter.getAndIncrement()
  /**
   * Serializes this path using the Silk RDF path language.
   */
  def serialize(implicit prefixes : Prefixes = Prefixes.empty) = "?" + variable + operators.map(_.serialize).mkString

  /**
   * This version of equals checks if this path equals another path in a global fashion
   */
//  override def equals(other: Any): Boolean = {
//    if(other.isInstanceOf[Path] && gid==other.asInstanceOf[Path].gid)
//      return true
//    else
//      return false
//  }

//  override def hashCode = gid.hashCode()

  override def toString = serialize(Prefixes.empty)

  /**
   * Tests if this path equals another path
   */
  override def equals(other : Any) = other.isInstanceOf[Path] && toString == other.toString

  override def hashCode = toString.hashCode
}

object Path
{
  private val pathCache = new WeakHashMap[String, Path]() with SynchronizedMap[String, Path]

  /**
   * Parses a path string.
   * May return a cached copy.
   */
  def parse(pathStr : String)(implicit prefixes : Prefixes = Prefixes.empty) =
  {
    //Try to retrieve a cached copy. If not found, parse the path
    pathCache.getOrElseUpdate(pathStr, new PathParser(prefixes).parse(pathStr))
  }

  val pathCounter = new AtomicInteger(0)
}
