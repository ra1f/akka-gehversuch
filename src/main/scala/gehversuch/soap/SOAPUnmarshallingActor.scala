package gehversuch.soap

import akka.actor._
import akka.camel.CamelMessage
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.bind._
import javax.xml.soap.MessageFactory
import java.io.{File, StringReader, ByteArrayOutputStream}
import javax.xml.transform.stream.{StreamResult, StreamSource}
import javax.xml.transform.Result
import javax.xml.stream.XMLInputFactory
import de.gehversuch.customerservice.{NoSuchCustomerException, GetCustomersByName}
import gehversuch.customerservice.CustomerServiceDelegationActor
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import akka.actor.OneForOneStrategy

/**
 * Created by dueerkopra on 02.05.2014.
 */
class SOAPUnmarshallingActor[P](product: P) extends Actor with ActorLogging {

  val dispatcher = context actorOf Props[CustomerServiceDelegationActor]

  def receive = {
    case msg: CamelMessage => {
      log.debug("Received message: {}", msg)

      val xif = XMLInputFactory newInstance
      val reader = xif.createXMLStreamReader(new StreamSource(new StringReader(msg.body.toString)))
      reader.nextTag

      while (reader getLocalName match {
        case "Header" => true
        case "Body" => true
        case "Envelope" => true
        case _ => false
      }){
        log.debug("Local name: {}", reader.getLocalName)
        reader.nextTag
      }

      val jaxbContext = JAXBContext newInstance product.getClass
      val unmarshaller = jaxbContext createUnmarshaller
      val o = unmarshaller unmarshal(reader, product.getClass)
      dispatcher forward (new CamelMessage(o.getValue, msg.headers))
    }
    //case _ => msg
  }

  /*override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0) {
    case _: NoSuchElementException => Escalate
    case _ => Resume
  }*/

}
