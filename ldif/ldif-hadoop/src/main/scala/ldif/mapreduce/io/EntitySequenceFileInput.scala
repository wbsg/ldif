package ldif.mapreduce.io

import org.apache.hadoop.mapred.SequenceFileInputFormat
import org.apache.hadoop.io.IntWritable
import ldif.mapreduce.types.ValuePathWritable
import ldif.entity.EntityWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/15/11
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */

class EntitySequenceFileInput extends SequenceFileInputFormat[IntWritable, EntityWritable]