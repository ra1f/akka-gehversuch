package gehversuch.soap

import akka.actor._
import akka.camel.CamelMessage
import javax.xml.bind._
import java.io.StringReader
import javax.xml.transform.stream.StreamSource
import javax.xml.stream.XMLInputFactory
import gehversuch.customerservice.CustomerServiceDelegationActor

/**
 * Created by dueerkopra on 02.05.2014.
 */

case class SOAPUnmarshallingMessage[P](message: CamelMessage, prototype: P)

class SOAPUnmarshallingActor extends Actor with ActorLogging {

  val serviceDelegator = context.actorOf(Props[CustomerServiceDelegationActor])

  def receive = {

    case SOAPUnmarshallingMessage(message, prototype) =>
      try {

        val xif = XMLInputFactory newInstance
        val reader = xif.createXMLStreamReader(new StreamSource(
          new StringReader(message.body.toString)))
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
        serviceDelegator forward (new CamelMessage(o.getValue, message.headers))
      } catch {

        case e: Exception =>
          serviceDelegator forward SOAPMarshallingUnmodeledFaultMessage(e)
      }
  }
}
