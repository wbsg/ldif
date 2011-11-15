package ldif.mapreduce.io

import org.apache.hadoop.mapred.TextOutputFormat
import org.apache.hadoop.io.{NullWritable, Text}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/15/11
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */

class QuadTextFileOutput extends TextOutputFormat[NullWritable, Text]