package ldif.local.datasources.crawl

import com.ontologycentral.ldspider.hooks.sink.{Provenance, Sink}
import ldif.local.runtime.impl.QuadQueue

class SinkImpl extends Sink{

  def newDataset(provenance : Provenance) = {
    //Headers.processHeaders(provenance.getUri(), provenance.getHttpStatus(), provenance.getHttpHeaders(), CallbackQuadQueue)
    new CallbackQuadQueue(new QuadQueue)
  }


}