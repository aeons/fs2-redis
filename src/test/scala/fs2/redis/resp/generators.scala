package fs2.redis.resp

import RData._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen._
import org.scalacheck._

object generators {
  def escape(s: String) = s.replaceAll("\r", "/r").replaceAll("\n", "/n")

  val genSingleLineString: Gen[String] =
    arbitrary[String].suchThat(s => !s.contains('\r') && !s.contains('\n'))

  val genData: Gen[(String, RData)] = sized { depth =>
    if (depth > 10) {
      oneOf(genSimpleString, genBulkString, genInteger, genError)
    } else {
      oneOf(genArray, genSimpleString, genBulkString, genInteger, genError)
    }
  }

  val genSimpleString: Gen[(String, RString)] =
    genSingleLineString
      .map(s => (s"+$s\r\n", RString(s)))

  val genNullBulkString: Gen[(String, RNull.type)] =
    const(("$-1\r\n", RNull))

  val genNonNullBulkString: Gen[(String, RString)] =
    arbitrary[String]
      .map(s => (s"$$${s.length}\r\n$s\r\n", RString(s)))

  val genBulkString: Gen[(String, RData)] =
    lzy(frequency(1 -> genNullBulkString, 9 -> genBulkString))

  val genInteger: Gen[(String, RInteger)] =
    arbitrary[Long].map(l => (s":$l\r\n", RInteger(l)))

  val genNullArray: Gen[(String, RNull.type)] =
    const(("*-1\r\n", RNull))

  val genNonNullArray: Gen[(String, RArray)] =
    listOf(sized(depth => resize(depth + 1, genData)))
      .map(xs => (s"*${xs.length}\r\n${xs.map(_._1).mkString("")}", RArray(xs.map(_._2))))

  val genArray: Gen[(String, RData)] =
    frequency(1 -> genNullArray, 9 -> genNonNullArray)

  val genError: Gen[(String, RError)] =
    for {
      t   <- alphaUpperStr.suchThat(!_.isEmpty)
      msg <- genSingleLineString
    } yield (s"-$t $msg\r\n", GenericError(s"$t $msg"))

  val arbSimpleString       = Arbitrary(genSimpleString)
  val arbBulkString         = Arbitrary(genNonNullBulkString)
  val arbBulkStringWithNull = Arbitrary(genBulkString)
  val arbArrayWithNull      = Arbitrary(genArray)

  implicit val arbData    = Arbitrary(genData)
  implicit val arbString  = Arbitrary(oneOf(genSimpleString, genNonNullBulkString))
  implicit val arbInteger = Arbitrary(genInteger)
  implicit val arbArray   = Arbitrary(genNonNullArray)
  implicit val arbError   = Arbitrary(genError)
}
