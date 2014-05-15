package gehversuch.customerservice

import akka.camel.{CamelMessage, Consumer}
import akka.actor._
import gehversuch.Configuration
import de.gehversuch.customerservice.{UpdateCustomer, GetCustomersByName}
import gehversuch.soap.UnmarshallingRequest
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
    context.actorOf(Props[CustomerServiceController]) ! UnmarshallingRequest(body, prototype)
  }

}