package ldif.hadoop.entitybuilder

import ldif.entity.EntityDescription
import phases.{Phase4, Phase3, Phase2}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/28/11
 * Time: 4:12 PM
 * To change this template use File | Settings | File Templates.
 */

object RunHadoopEntityBuilder {
  def runHadoopEntityBuilder(inputPath: String, outputPath: String,  entityDescriptions: Seq[EntityDescription]): Int = {
    Phase2.runPhase(inputPath, outputPath+"/eb/phase2", entityDescriptions)
    Phase3.runPhase(outputPath+"/eb/phase2", outputPath+"/eb/phase3", entityDescriptions)
    Phase4.runPhase(outputPath+"/eb/phase3", outputPath+"/eb/phase4", entityDescriptions)
  }
}