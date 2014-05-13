package gehversuch.customerservice

import akka.camel.{CamelMessage, Consumer}
import akka.actor.{DeadLetter, Props, ActorLogging}
import gehversuch.Configuration
import de.gehversuch.customerservice.{UpdateCustomer, GetCustomersByName}
import gehversuch.soap_alt.{SOAPUnmarshallingMessage, SOAPUnmarshallingActor}

/**
 * Created by dueerkopra on 13.05.2014.
 */
class CustomerServiceConsumerAltActor extends Consumer with ActorLogging {

  val portHttp = Configuration.portHttp
  val host = Configuration.host

  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  val unmarshaller = context.actorOf(Props(classOf[SOAPUnmarshallingActor], Props[CustomerServiceDelegationActor]))

  def receive = {
    case msg: CamelMessage => msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {
      case "getCustomersByName" =>
        forward(msg, new GetCustomersByName)
      case "updateCustomer" =>
        forward(msg, new UpdateCustomer)
    }
    case dl: DeadLetter => log.debug(dl.toString)
  }

  def forward(msg: CamelMessage, prototype: Any) = {
    def message = msg.withBodyAs[String]
    log.debug("Received message: {}", message)
    unmarshaller forward SOAPUnmarshallingMessage(message, prototype)
  }

}