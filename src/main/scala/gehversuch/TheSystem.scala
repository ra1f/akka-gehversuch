package gehversuch

import akka.actor.{Props, ActorSystem}
import akka.camel.CamelExtension
import gehversuch.customerservice.{CustomerServiceDelegationActor, CustomerServiceProducer, CustomerServiceConsumer, CustomCamelRouteBuilder}
import gehversuch.soap.SOAPUnmarshallingActor
import de.gehversuch.customerservice.{GetCustomersByName, CustomerService}

/**
 * Created by rdu on 30.04.14.
 */
object TheSystem extends App {

  val system = ActorSystem("gehversuch-system")
  //val dispatcher = system.actorOf(Props[CustomerServiceServiceDispatcherActor])
  //val unmarshaller = system.actorOf(Props(classOf[SOAPUnmarshallingActor[CustomerService]], new GetCustomersByName))
  val consumer = system.actorOf(Props(classOf[CustomerServiceConsumer]))

  /*val producer = system.actorOf(Props[CustomerServiceProducer])
  val consumer = system.actorOf(Props(classOf[CustomerServiceConsumer], producer))*/
  val extension = CamelExtension(system)
  //val dispatcher = system.actorOf(Props[CustomerServiceServiceDispatcherActor])
  extension.context.addRoutes(new CustomCamelRouteBuilder)
}


object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("gehversuch.host")
  val portHttp = config.getInt("gehversuch.ports.http")
}

