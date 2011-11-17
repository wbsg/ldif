package ldif.hadoop.io

import org.apache.hadoop.mapred.SequenceFileInputFormat
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types.ValuePathWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathSequenceFileInput extends SequenceFileInputFormat[IntWritable, ValuePathWritable]