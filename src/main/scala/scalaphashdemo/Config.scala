package scalaphashdemo

import pureconfig.generic.auto._

object Config {
  case class ApplicationConfig(maxImageSize: Int, dctThreshold: Int, marrThreshold: Double, radialThreshold: Double)

  val config: ApplicationConfig = pureconfig.loadConfigOrThrow[ApplicationConfig]
}
