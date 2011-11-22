/*
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test

import ldif.modules.r2r.hadoop.RunHadoopR2RJob

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/22/11
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */

object RunLDIFHadoopPipeline {
  def main(args: Array[String]) {
    RunHadoopEntityBuilder.main(args)
    RunHadoopR2RJob.main(args)
  }

}