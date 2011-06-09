package blueeyes.util.logging

import org.specs.Specification
import blueeyes.parsers.W3ExtendedLogAST._
import RollPolicies._
import java.io.File
import scala.io.Source

class W3ExtendedLoggerSpec extends Specification with blueeyes.concurrent.FutureDeliveryStrategySequential {
  private val directives = FieldsDirective(List(DateIdentifier, TimeIdentifier))

  private var w3Logger: W3ExtendedLogger = _
  "W3ExtendedLogger" should {
    "creates log file" in {
      w3Logger = W3ExtendedLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3.log", Never, directives, 1)

      new File(w3Logger.fileName.get).exists must be (true)
      cleanUp()
    }
    "init log file" in {
      w3Logger = W3ExtendedLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3_1.log", Never, directives, 1)

      val content = getContents(new File(w3Logger.fileName.get))
      cleanUp()

      content.indexOf("#Version: 1.0")      must notEq (-1)
      content.indexOf("#Date: ")            must notEq (-1)
      content.indexOf(directives.toString)  must notEq (-1)
    }

    "flush entries while closing" in{
      w3Logger = W3ExtendedLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3_2.log", Never, directives, 1)

      w3Logger("foo")
      w3Logger("bar")

      val future = w3Logger.close
      future.value must eventually (beSomething)

      val content = getContents(new File(w3Logger.fileName.get))
      cleanUp()

      content.indexOf("foo") must notEq (-1)
      content.indexOf("bar") must notEq (-1)
    }

    "write log entries" in {
      w3Logger = W3ExtendedLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3_3.log", Never, directives, 1)

      w3Logger("foo")
      w3Logger("bar")
      w3Logger("baz")

      val file = new File(w3Logger.fileName.get)

      getContents(file).indexOf("foo") must eventually(notEq (-1))
      getContents(file).indexOf("bar") must eventually(notEq (-1))

      cleanUp()
    }

    def cleanUp(){
      val future = w3Logger.close
      future.value must eventually (beSomething)

      new File(w3Logger.fileName.get).delete
    }
  }

  private def getContents(file: File) = Source.fromFile(file, "UTF-8").getLines.mkString("\n")
}
