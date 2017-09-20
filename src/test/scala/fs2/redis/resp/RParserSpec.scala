package fs2.redis.resp

import RData._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class RParserSpec extends Specification with ScalaCheck {
  import atto._
  import Atto._
  import ParseResult.Done
  import generators._

  "RParser" should {
    "parse" >> {

      "simple strings" in {
        prop { t: (String, RString) =>
          RParser.simpleString.parseOnly(t._1) must_=== Done("", t._2.value)
        }.setArbitrary(arbSimpleString)
      }

      "simple strings as RString" in {
        prop { t: (String, RString) =>
          RParser.rSimpleString.parseOnly(t._1) must_=== Done("", t._2)
        }.setArbitrary(arbSimpleString)
      }

      "bulk strings as RData" in {
        prop { t: (String, RData) =>
          val result = RParser.rBulkString.parseOnly(t._1)
          t._2 match {
            case rString: RString => result must_=== Done("", rString)
            case RNull            => result must_=== Done("", RNull)
            case _                => ko
          }
        }.setArbitrary(arbBulkStringWithNull)
      }

      "integers" in {
        prop { t: (String, RInteger) =>
          RParser.integer.parseOnly(t._1) must_=== Done("", t._2.value)
        }
      }

      "integers as RInteger" in {
        prop { t: (String, RInteger) =>
          RParser.rInteger.parseOnly(t._1) must_=== Done("", t._2)
        }
      }

      "errors" in {
        prop { t: (String, RError) =>
          val result = RParser.error.parseOnly(t._1)
          t._2 match {
            case GenericError(message) => result must_=== Done("", message)
            case _                     => ko
          }
        }
      }

      "errors as RError" in {
        prop { t: (String, RError) =>
          val result = RParser.rError.parseOnly(t._1)
          t._2 match {
            case e: GenericError => result must_=== Done("", e)
            case _               => ko
          }
        }
      }

      "arrays as RData" in {
        prop { t: (String, RData) =>
          RParser.rArray.parseOnly(t._1) must_=== Done("", t._2)
        }.setArbitrary(arbArrayWithNull)
      }

      "rdata as RData" in {
        prop { t: (String, RData) =>
          RParser.rData.parseOnly(t._1) must_=== Done("", t._2)
        }
      }
    }
  }
}
