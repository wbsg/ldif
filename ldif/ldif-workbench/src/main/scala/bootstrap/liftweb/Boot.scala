/*
 * LDIF
 *
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

import net.liftweb.http.LiftRules
import _root_.net.liftweb.sitemap._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */

class Boot {
  def boot {
    // Where to search snippet
    LiftRules.addToPackages("ldif")

    // Build SiteMap
     val entries =
        Menu(Loc("Workspace", List("index"), "Workspace")) :: Nil

    LiftRules.setSiteMap(SiteMap(entries:_*))

  }
}




