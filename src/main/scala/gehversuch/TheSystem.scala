package gehversuch

import akka.actor.{Props, ActorSystem}
import gehversuch.customerservice.CustomerServiceConsumer

/**
 * Created by rdu on 30.04.14.
 */
object TheSystem extends App {

  val system = ActorSystem("gehversuch-system")
  val consumer = system.actorOf(Props(classOf[CustomerServiceConsumer]))
  //system.eventStream.subscribe(consumer, classOf[DeadLetter])
}


object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("gehversuch.host")
  val portHttp = config.getInt("gehversuch.ports.http")
}

