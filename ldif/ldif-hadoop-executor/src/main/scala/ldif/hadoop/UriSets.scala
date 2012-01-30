package ldif.hadoop

import com.hp.hpl.jena.sparql.SystemARQ
import runtime.{HadoopQuadToTextConverter, RunHadoopURIClustering, RunHadoopQuadConverter}
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.hadoop.conf.Configuration

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 1/30/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */

object UriSets {
  def execute(args: Array[String]) {
    if(args.length < 2) {
      sys.error("Parameters: <sameAs input path> <output path>")
      sys.exit(1)
    }

    val tmp = "_tmp_" + System.currentTimeMillis() + "/"
    val quads = tmp + "quads"
    val clusteredUris = tmp + "clustered"
    val output = args(1)

    // remove existing output
    val conf = new Configuration
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(output)
    if (hdfs.exists(hdPath))
    hdfs.delete(hdPath, true)

    // Run parse job, clustering and then text conversion
    RunHadoopQuadConverter.execute(args(0), quads)
    RunHadoopURIClustering.runHadoopURIClustering(quads, clusteredUris)
    HadoopQuadToTextConverter.execute(clusteredUris, args(1))

    // Delete temp directory
    hdfs.delete(new Path(tmp), true)
  }
}