package ldif.modules.sieve.fusion.functions

/*
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import ldif.modules.sieve.fusion.FusionFunction
import ldif.util.Prefixes

/**
 * Fusion function that does nothing. Passes values on to the next component in the pipeline (or user app)
 * @author pablomendes
 */

class PassItOn extends FusionFunction(metricId="")

object PassItOn {

  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes) : FusionFunction = {
    new PassItOn
  }
}

