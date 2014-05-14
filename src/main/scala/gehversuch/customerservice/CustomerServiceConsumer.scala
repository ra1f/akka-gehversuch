package gehversuch.customerservice

import akka.camel.{CamelMessage, Consumer}
import akka.actor._
import gehversuch.Configuration
import de.gehversuch.customerservice.{UpdateCustomer, GetCustomersByName}
import gehversuch.soap._
import gehversuch.soap.SOAPMarshallingSuccessMessage
import gehversuch.soap.SOAPUnmarshallingMessage
import gehversuch.soap.SOAPUnmodeledFaultMessage
import gehversuch.soap.SOAPUnmarshallingResultMessage
import akka.actor.DeadLetter

/**
 * Created by dueerkopra on 13.05.2014.
 */
class CustomerServiceConsumer extends Consumer with ActorLogging {

  val portHttp = Configuration.portHttp
  val host = Configuration.host

  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  var responseChannel:ActorRef = null

  def receive = {

    case msg: CamelMessage =>
      responseChannel = sender
      msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {
        case "getCustomersByName" => startUnmarshalling(msg, new GetCustomersByName)
        case "updateCustomer" => startUnmarshalling(msg, new UpdateCustomer)
      }
    case soapResponse: String => responseChannel ! soapResponse

    case dl: DeadLetter => log.debug(dl.toString)
  }

  def startUnmarshalling(msg: CamelMessage, prototype: Any) = {
    val body = msg.getBodyAs(classOf[String], camelContext)
    log.debug("Received message: {}", body)
    context.actorOf(Props[CustomerServiceController]) ! SOAPUnmarshallingMessage(body, prototype)
  }

}

class CustomerServiceController extends Actor {

  val unmarshaller = context.actorOf(Props[SOAPUnmarshaller])
  val serviceDelegator = context.actorOf(Props[CustomerServiceDelegator])
  val marshaller = context.actorOf(Props[SOAPMarshaller])
  var responseChannel:ActorRef = null

  override def receive = {

    case SOAPUnmarshallingMessage(message, prototype) => unmarshaller ! SOAPUnmarshallingMessage(message, prototype)
      responseChannel = sender

    case SOAPUnmarshallingResultMessage(result) => serviceDelegator ! result

    case SOAPMarshallingSuccessMessage(element) => marshaller ! SOAPMarshallingSuccessMessage(element)

    case SOAPModeledFaultMessage(faultBean, message) => marshaller ! SOAPModeledFaultMessage(faultBean, message)

    case SOAPUnmodeledFaultMessage(e) => marshaller ! SOAPUnmodeledFaultMessage(e)

    case soapResponse: String => responseChannel ! soapResponse
  }
}