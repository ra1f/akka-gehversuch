package gehversuch.customerservice

import akka.actor.{Actor, ActorRef}
import akka.camel.{CamelMessage, Consumer}
import gehversuch.Configuration

/**
 * Created by rdu on 30.04.14.
 */
class CustomerServiceConsumer(producer: ActorRef) extends Actor with Consumer {

  val portHttp = Configuration.portHttp
  val host = Configuration.host
  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  def receive = {
    case msg: CamelMessage => producer.forward(msg.withBodyAs[String])
  }

}
