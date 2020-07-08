package housingfinder.config

import enumeratum.EnumEntry._
import enumeratum._

import scala.collection.immutable

object environments {

  sealed abstract class AppEnvironment extends EnumEntry with Lowercase

  object AppEnvironment
      extends Enum[AppEnvironment]
      with CirisEnum[AppEnvironment] {

    case object Test extends AppEnvironment
    case object Prod extends AppEnvironment

    val values: immutable.IndexedSeq[AppEnvironment] = findValues

  }
}
