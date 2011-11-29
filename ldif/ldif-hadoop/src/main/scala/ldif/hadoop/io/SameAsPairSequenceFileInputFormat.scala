package ldif.hadoop.io

import org.apache.hadoop.mapred.SequenceFileInputFormat
import ldif.hadoop.types.SameAsPairWritable
import org.apache.hadoop.io.NullWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/29/11
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */

class SameAsPairSequenceFileInputFormat extends SequenceFileInputFormat[NullWritable, SameAsPairWritable]