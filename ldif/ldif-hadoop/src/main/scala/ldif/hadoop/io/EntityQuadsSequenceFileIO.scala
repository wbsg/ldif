package ldif.hadoop.io

import ldif.hadoop.types.QuadWritable
import ldif.entity.NodeWritable
import org.apache.hadoop.mapred.{SequenceFileInputFormat, SequenceFileOutputFormat}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityQuadsSequenceFileInput extends SequenceFileInputFormat[NodeWritable, QuadWritable]

class EntityQuadsSequenceFileOutput extends SequenceFileOutputFormat[NodeWritable, QuadWritable]