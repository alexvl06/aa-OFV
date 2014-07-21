import java.util.regex.Pattern
import org.joda.time.format.DateTimeFormat

/**
 *
 * @author smontanez
 */

val fmt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm")

fmt.print(System.currentTimeMillis())


val value = Pattern.quote(".!@#$%^&~<>-*()+".replace("-","""\-""")).replace("""\Q""","").replace("""\E""","")

val pattern = s"""(?=(?:.*?[^a-zA-Z0-9${value}]))""".r
pattern findFirstIn  "123456" match {
  case Some(_) => println("Some")
  case _ => println("None")
}

