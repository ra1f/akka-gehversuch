package gehversuch.customerservice

import akka.actor.{DeadLetter, ActorLogging, Props, Actor}
import akka.camel.{CamelMessage, Consumer}
import gehversuch.Configuration
import de.gehversuch.customerservice.{UpdateCustomer, GetCustomersByName}
import gehversuch.soap.{SOAPUnmarshallingMessage, SOAPUnmarshallingActor}

/**
 * Created by rdu on 30.04.14.
 */
class CustomerServiceConsumer extends Actor with Consumer with ActorLogging {

  val portHttp = Configuration.portHttp
  val host = Configuration.host

  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  val unmarshaller = context.actorOf(Props[SOAPUnmarshallingActor])

  def receive = {
    case msg: CamelMessage =>  msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {
      case "getCustomersByName" =>
        forward(msg, new GetCustomersByName)
      case "updateCustomer" =>
        forward (msg, new UpdateCustomer)
    }
    case dl: DeadLetter => log.debug(dl.toString)
  }

  def forward(msg: CamelMessage, prototype: Any) = {
    def message = msg.withBodyAs[String]
    log.debug("Received message: {}", message)
    unmarshaller forward SOAPUnmarshallingMessage(message, prototype)
  }
}
