package gehversuch.soap

import akka.actor._
import javax.xml.bind._
import java.io.StringReader
import javax.xml.transform.stream.StreamSource
import javax.xml.stream.XMLInputFactory

/**
 * Created by dueerkopra on 02.05.2014.
 */

case class UnmarshallingRequest[P](message: String, prototype: P)
case class UnmarshallingResponse[P](product: P)

class SOAPUnmarshaller extends Actor with ActorLogging {

  def receive = {

    case UnmarshallingRequest(message, prototype) =>
      try {
        val xif = XMLInputFactory.newInstance
        val reader = xif.createXMLStreamReader(new StreamSource(new StringReader(message)))
        reader.nextTag

        while (reader getLocalName match {
          case "Header" => true
          case "Body" => true
          case "Envelope" => true
          case _ => false
        }) {
          reader.nextTag
        }

        val jaxbContext = JAXBContext.newInstance(prototype.getClass)
        val o = jaxbContext.createUnmarshaller.unmarshal(reader, prototype.getClass)
        sender ! UnmarshallingResponse(o.getValue)
      } catch {
        case e: Exception => sender ! MarshallingUnmodeledFaultRequest(e)
      }
  }
}
