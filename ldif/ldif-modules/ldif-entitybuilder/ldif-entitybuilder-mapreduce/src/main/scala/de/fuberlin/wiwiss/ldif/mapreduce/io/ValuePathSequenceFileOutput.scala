package de.fuberlin.wiwiss.ldif.mapreduce.io

import org.apache.hadoop.mapred.SequenceFileOutputFormat
import de.fuberlin.wiwiss.ldif.mapreduce.types.ValuePathWritable
import org.apache.hadoop.io.IntWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathSequenceFileOutput extends SequenceFileOutputFormat[IntWritable, ValuePathWritable]