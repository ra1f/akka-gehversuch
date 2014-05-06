package gehversuch.soap

import akka.actor._
import akka.camel.CamelMessage
import javax.xml.bind._
import java.io.StringReader
import javax.xml.transform.stream.StreamSource
import javax.xml.stream.XMLInputFactory
import de.gehversuch.customerservice.NoSuchCustomerException
import gehversuch.customerservice.CustomerServiceDelegationActor
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._

/**
 * Created by dueerkopra on 02.05.2014.
 */

case class SOAPUnmarshallingMessage[P](message: CamelMessage, prototype: P)

class SOAPUnmarshallingActor extends Actor with ActorLogging {

  val serviceDelegator = context actorOf Props[CustomerServiceDelegationActor]

  def receive = {
    case SOAPUnmarshallingMessage(message, prototype) => {
      log.debug("Received message: {}", message)

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
        log.debug("Local name: {}", reader.getLocalName)
        reader.nextTag
      }

      val jaxbContext = JAXBContext newInstance prototype.getClass

      try {
        val o = jaxbContext.createUnmarshaller.unmarshal(reader, prototype.getClass)
        serviceDelegator forward (new CamelMessage(o.getValue, message.headers))
      } catch {
        case e: Exception =>
          sender ! e; throw e //TODO: error marshalling
      }
    }
  }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: NoSuchCustomerException => Resume
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

}
