package gehversuch.customerservice

import akka.actor.{Props, Actor}
import akka.camel.{CamelMessage, Consumer}
import gehversuch.Configuration
import de.gehversuch.customerservice.{UpdateCustomer, GetCustomersByName}
import gehversuch.soap.{SOAPUnmarshallingMessage, SOAPUnmarshallingActor}

/**
 * Created by rdu on 30.04.14.
 */
class CustomerServiceConsumer extends Actor with Consumer {

  val portHttp = Configuration.portHttp
  val host = Configuration.host

  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  val unmarshaller = context.actorOf(Props[SOAPUnmarshallingActor])

  def receive = {
    case msg: CamelMessage =>  msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {
      case "getCustomersByName" =>
        unmarshaller forward SOAPUnmarshallingMessage(msg.withBodyAs(classOf[String])(camelContext), new GetCustomersByName)
      case "updateCustomer" =>
        unmarshaller forward SOAPUnmarshallingMessage(msg.withBodyAs(classOf[String])(camelContext), new UpdateCustomer)
    }

  }
}
