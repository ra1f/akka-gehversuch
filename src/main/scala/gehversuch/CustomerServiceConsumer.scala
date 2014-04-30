package gehversuch

import akka.actor.{Actor, ActorRef}
import akka.camel.{CamelMessage, Consumer}

/**
 * Created by rdu on 30.04.14.
 */
class CustomerServiceConsumer(producer: ActorRef) extends Actor with Consumer {
  def endpointUri: String = "jetty:http://0.0.0.0:9692/CustomerServicePort"

  def receive = {
    case msg: CamelMessage => producer.forward(msg.withBodyAs[String])
  }

  //def endpointUri: String = "jetty:http://localhost:9090/CustomerServicePort"
}
