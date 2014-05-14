package gehversuch.customerservice

import akka.camel.{CamelMessage, Consumer}
import akka.actor.{ActorRef, DeadLetter, Props, ActorLogging}
import gehversuch.Configuration
import de.gehversuch.customerservice.{UpdateCustomer, GetCustomersByName}
import gehversuch.soap_alt.{SOAPUnmarshallingResultMessage, SOAPMarshallingActor, SOAPUnmarshallingMessage, SOAPUnmarshallingActor, SOAPMarshallingSuccessMessage}

/**
 * Created by dueerkopra on 13.05.2014.
 */
class CustomerServiceConsumerAltActor extends Consumer with ActorLogging {

  val portHttp = Configuration.portHttp
  val host = Configuration.host

  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  val unmarshaller = context.actorOf(Props[SOAPUnmarshallingActor])
  val serviceDelegator = context.actorOf(Props[CustomerServiceDelegationAltActor])
  val marshaller = context.actorOf(Props[SOAPMarshallingActor])


  def receive = {
    case msg: CamelMessage => msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {
      case "getCustomersByName" =>
        sendToUnmarshaller(msg, new GetCustomersByName)
      case "updateCustomer" =>
        sendToUnmarshaller(msg, new UpdateCustomer)
    }
    case SOAPUnmarshallingResultMessage(result) => serviceDelegator ! result
    case SOAPMarshallingSuccessMessage(element, originalSender) =>  marshaller ! SOAPMarshallingSuccessMessage(element, originalSender)
    case soapResponse: String => sender ! soapResponse
    case dl: DeadLetter => log.debug(dl.toString)
  }

  def sendToUnmarshaller(msg: CamelMessage, prototype: Any) = {
    log.debug("Received message: {}", msg.body.toString)
    unmarshaller ! SOAPUnmarshallingMessage(msg.body.toString, prototype)
  }

}