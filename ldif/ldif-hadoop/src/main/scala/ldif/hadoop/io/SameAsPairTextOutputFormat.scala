package ldif.hadoop.io

import org.apache.hadoop.io.NullWritable
import ldif.hadoop.types.SameAsPairWritable
import org.apache.hadoop.mapred.{TextOutputFormat, SequenceFileOutputFormat}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */

class SameAsPairTextOutputFormat extends TextOutputFormat[NullWritable, SameAsPairWritable]