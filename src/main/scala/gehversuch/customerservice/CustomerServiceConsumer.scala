package gehversuch.customerservice

import akka.actor.{Props, Actor, ActorRef}
import akka.camel.{CamelMessage, Consumer}
import gehversuch.Configuration
import org.apache.camel.builder.Builder
import akka.actor.Status.Failure
import org.apache.camel.model.dataformat.SoapJaxbDataFormat
import org.apache.camel.dataformat.soap.name.ServiceInterfaceStrategy
import de.gehversuch.customerservice.{GetCustomersByName, CustomerService}
import gehversuch.soap.SOAPUnmarshallingActor

/**
 * Created by rdu on 30.04.14.
 */
class CustomerServiceConsumer extends Actor with Consumer {

  val portHttp = Configuration.portHttp
  val host = Configuration.host
  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  val unmarshaller = context.actorOf(Props(classOf[SOAPUnmarshallingActor[CustomerService]], new GetCustomersByName))

  def receive = {
    case msg: CamelMessage => unmarshaller forward (msg.withBodyAs[String])
  }

  override def onRouteDefinition = (rd) => rd.onException(classOf[Exception]).
    handled(true).transform.constant("Shit happens!!!").end

  /*final override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
  }*/

}

import akka.camel.Producer

class CustomerServiceProducer extends Actor with Producer {

  override def endpointUri: String = "direct:customerService"



}
