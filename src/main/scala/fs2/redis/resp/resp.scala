package fs2.redis.resp

sealed trait RData extends Any with Product with Serializable

object RData {

  final case class RString(value: String) extends AnyVal with RData

  final case class RInteger(value: Long) extends AnyVal with RData

  final case class RArray(value: List[RData]) extends AnyVal with RData

  final case object RNull extends RData

  sealed trait RError extends RData

  final case class GenericError(message: String) extends RError

}

object RParser {
  import RData._
  import atto._
  import Atto._

  val newLine: Parser[Unit]       = string("\r\n").void
  val toEndOfLine: Parser[String] = takeWhile(_ != '\r')

  val simpleString: Parser[String] =
    char('+') ~> toEndOfLine <~ newLine

  val rSimpleString: Parser[RData] =
    simpleString.map(RString)

  val rBulkString: Parser[RData] =
    for {
      n <- char('$') ~> int.filter(_ >= -1) <~ newLine
      s <- n match {
        case -1 => ok(RNull)
        case 0  => ok(RString(""))
        case n  => (take(n) <~ newLine).map(RString)
      }
    } yield s

  val integer: Parser[Long] =
    char(':') ~> long <~ newLine

  val rInteger: Parser[RData] =
    integer.map(RInteger)

  val error: Parser[String] =
    char('-') ~> toEndOfLine <~ newLine

  val rError: Parser[RData] =
    error.map(GenericError)

  val rArray: Parser[RData] =
    for {
      n <- char('*') ~> int.filter(_ >= -1) <~ newLine
      values <- n match {
        case -1 => ok(RNull)
        case 0  => ok(RArray(List.empty))
        case n  => manyN(n, rData).map(RArray)
      }
    } yield values

  val rData: Parser[RData] =
    delay(rSimpleString | rBulkString | rInteger | rArray | rError)
}
