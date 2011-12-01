package ldif.hadoop.io

import ldif.hadoop.types.SameAsPairWritable
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapred.{SequenceFileOutputFormat, SequenceFileInputFormat}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */

class SameAsPairSequenceFileInputFormat extends SequenceFileInputFormat[NullWritable, SameAsPairWritable]

class SameAsPairSequenceFileOutputFormat extends SequenceFileOutputFormat[NullWritable, SameAsPairWritable]