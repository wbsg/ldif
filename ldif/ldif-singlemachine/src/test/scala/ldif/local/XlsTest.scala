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
package ldif.local

import org.apache.any23.extractor.ExtractionContext
import org.apache.any23.extractor.ExtractionParameters
import org.apache.any23.extractor.ExtractionResult
import org.apache.any23.extractor.ExtractionResultImpl
import org.apache.any23.plugin.officescraper.ExcelExtractor
import org.apache.any23.rdf.RDFUtils
import org.apache.any23.vocab.Excel
import org.apache.any23.writer.CompositeTripleHandler
import org.apache.any23.writer.CountingTripleHandler
import org.apache.any23.writer.NTriplesWriter
import org.apache.any23.writer.TripleHandler
import org.mockito.Mockito
import org.openrdf.model.URI
import java.io.{FileInputStream, BufferedInputStream, ByteArrayOutputStream, InputStream}
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.util.{Consts, CommonUtils}


class XlsTest extends FlatSpec with ShouldMatchers {

   it should "load XLSX data correctly" in {
      val extractor = new ExcelExtractor
      val FILE: String = "scheduler/sources/source.xlsx"
      processFile(FILE, extractor)
    }

    private def processFile(resource: String, extractor : ExcelExtractor) {
      val extractionParameters: ExtractionParameters = ExtractionParameters.newDefault
      val extractionContext: ExtractionContext = new ExtractionContext(
        extractor.getDescription.getExtractorName,
        RDFUtils.uri(Consts.LDIF_WEBSITE + "/source.xlsx")
      )

      //var is: InputStream = this.getClass.getResourceAsStream(resource)
      val file = CommonUtils.getFileFromPath(resource)
      if(!file.exists()) {
        throw new Exception("Unable to load the dump. File not found: " + resource)
      }
      val is =  new BufferedInputStream(new FileInputStream(file))

      val out: ByteArrayOutputStream = new ByteArrayOutputStream
      val h = (new NTriplesWriter(out))
      val extractionResult: ExtractionResult = new ExtractionResultImpl(extractionContext, extractor, h)
      extractor.run(extractionParameters, extractionContext, is, extractionResult)
      h.close
      println(out.toString)
    }



}


