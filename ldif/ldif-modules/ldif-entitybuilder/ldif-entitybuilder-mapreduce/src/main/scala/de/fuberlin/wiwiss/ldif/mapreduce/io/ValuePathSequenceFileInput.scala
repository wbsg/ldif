package de.fuberlin.wiwiss.ldif.mapreduce.io

import org.apache.hadoop.mapred.SequenceFileInputFormat
import org.apache.hadoop.io.IntWritable
import ldif.mapreduce.types.ValuePathWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 11:52 AM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathSequenceFileInput extends SequenceFileInputFormat[IntWritable, ValuePathWritable]