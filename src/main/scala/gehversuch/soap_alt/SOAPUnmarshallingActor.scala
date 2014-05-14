package gehversuch.soap_alt

import akka.actor._
import akka.camel.CamelMessage
import javax.xml.bind._
import java.io.StringReader
import javax.xml.transform.stream.StreamSource
import javax.xml.stream.XMLInputFactory

/**
 * Created by dueerkopra on 02.05.2014.
 */

case class SOAPUnmarshallingMessage[P](message: String, prototype: P)
case class SOAPUnmarshallingResultMessage[P](product: P)

class SOAPUnmarshallingActor extends Actor with ActorLogging {

  def receive = {

    case SOAPUnmarshallingMessage(message, prototype) =>
      try {

        val xif = XMLInputFactory newInstance
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

        val jaxbContext = JAXBContext newInstance prototype.getClass
        val o = jaxbContext.createUnmarshaller.unmarshal(reader, prototype.getClass)
        sender ! SOAPUnmarshallingResultMessage(o.getValue)
      } catch {

        case e: Exception =>
          sender ! SOAPMarshallingUnmodeledFaultMessage(e, sender)
      }
  }
}
