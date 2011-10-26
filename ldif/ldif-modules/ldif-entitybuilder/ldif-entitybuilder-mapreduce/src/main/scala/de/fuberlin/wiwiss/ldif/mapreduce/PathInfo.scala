package de.fuberlin.wiwiss.ldif.mapreduce

import ldif.entity.{Path, EntityDescription}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 19:22
 * To change this template use File | Settings | File Templates.
 */

case class PathInfo(entityDescriptionIndex: Int, patternIndex: Int, pathIndex: Int, path: Path, isRestrictionPath: Boolean, length: Int)

