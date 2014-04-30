package gehversuch

import akka.actor.{Props, ActorSystem}
import akka.camel.CamelExtension
import gehversuch.customerservice.{CustomerServiceProducer, CustomerServiceConsumer, CustomCamelRouteBuilder}

/**
 * Created by rdu on 30.04.14.
 */
object TheSystem extends App {

  val system = ActorSystem("gehversuch-system")
  val producer = system.actorOf(Props[CustomerServiceProducer])
  val consumer = system.actorOf(Props(classOf[CustomerServiceConsumer], producer))
  val extension = CamelExtension(system)
  extension.context.addRoutes(new CustomCamelRouteBuilder)
}


object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("gehversuch.host")
  val portHttp = config.getInt("gehversuch.ports.http")
}

