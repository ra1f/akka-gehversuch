package gehversuch.customerservice

import akka.actor._
import akka.camel.{CamelExtension, CamelMessage}
import de.gehversuch.customerservice._
import scala.reflect.ClassTag
import akka.actor.SupervisorStrategy.{Resume, Decider, Escalate}
import gehversuch.soap.SOAPMarshallingActor
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

/**
 * Created by dueerkopra on 02.05.2014.
 */
class CustomerServiceDelegationActor extends Actor with ActorLogging {

  val customerService: CustomerService = new CustomerServiceImpl()
  val camelContext = CamelExtension(context.system).context
  val marshaller = context.actorOf(Props[SOAPMarshallingActor])
  val objectFactory = new ObjectFactory;

  def receive = {
    case msg: CamelMessage => {
      msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {

        case "getCustomersByName" => {
          val name = msg.getBodyAs(classOf[GetCustomersByName], camelContext).getName
          // TODO: Avoid blocking (eventually)!!!
          val customers: Array[Customer] = customerService getCustomersByName name
          val response = new GetCustomersByNameResponse
          response setReturn customers
          marshaller forward (objectFactory createGetCustomersByNameResponse response)
        }

        case "updateCustomer" => {
          var customer: Customer = msg.getBodyAs(classOf[UpdateCustomer], camelContext).getCustomer
          // TODO: Avoid blocking (eventually)!!!
          customer = customerService updateCustomer customer
          val response = new UpdateCustomerResponse
          response setReturn customer
          marshaller forward (objectFactory createUpdateCustomerResponse response)
        }

        //TODO: Else send Failure!!!
      }
    }
  }

  /*override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0) {
    case _: NoSuchElementException => Escalate
    case _ => Resume
  }*/

  /*override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    reason match {
      case e: NoSuchCustomerException => marshaller forward (objectFactory createNoSuchCustomer e.getFaultInfo)
    }
  }*/

  //@throws[T](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    reason match {
      case e: NoSuchCustomerException => marshaller forward (objectFactory createNoSuchCustomer e.getFaultInfo)
    }
  }
}
