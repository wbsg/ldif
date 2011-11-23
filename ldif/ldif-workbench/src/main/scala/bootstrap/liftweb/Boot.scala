package bootstrap.liftweb

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




