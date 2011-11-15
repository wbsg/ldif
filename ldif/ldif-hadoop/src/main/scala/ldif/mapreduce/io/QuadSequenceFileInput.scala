package ldif.mapreduce.io

import org.apache.hadoop.mapred.SequenceFileInputFormat
import ldif.mapreduce.types.QuadWritable
import org.apache.hadoop.io.NullWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/15/11
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */

class QuadSequenceFileInput extends SequenceFileInputFormat[NullWritable, QuadWritable]