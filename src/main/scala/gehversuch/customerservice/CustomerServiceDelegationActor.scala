package gehversuch.customerservice

import akka.actor._
import akka.camel.{CamelExtension, CamelMessage}
import de.gehversuch.customerservice._
import gehversuch.soap.{SOAPMarshallingMessage, SOAPMarshallingActor}
import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.ForkJoinPool

/**
 * Created by dueerkopra on 02.05.2014.
 */
class CustomerServiceDelegationActor extends Actor with ActorLogging {

  val customerService: CustomerService = new CustomerServiceImpl
  val camelContext = CamelExtension(context.system).context
  val marshaller = context.actorOf(Props[SOAPMarshallingActor])
  val objectFactory = new ObjectFactory

  def receive = {

    case msg: CamelMessage => {
      msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {
        case "getCustomersByName" => {
          val name = msg.getBodyAs(classOf[GetCustomersByName], camelContext).getName
          implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool)
          val f = Future {
            customerService getCustomersByName name
          }
          val originalSender = sender
          f onSuccess {
            case customers: Array[Customer] =>
              val response = new GetCustomersByNameResponse
              response setReturn customers
              marshaller forward SOAPMarshallingMessage(
                objectFactory createGetCustomersByNameResponse response, originalSender)
          }
          f onFailure {
            case e: NoSuchCustomerException => marshaller forward SOAPMarshallingMessage(
              objectFactory createNoSuchCustomer e.getFaultInfo, originalSender)
            case e: Exception => throw e
          }
        }

        case "updateCustomer" => {
          val customer = msg.getBodyAs(classOf[UpdateCustomer], camelContext).getCustomer
          implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool)
          val f = Future {
            customerService updateCustomer customer
          }
          val originalSender = sender
          f onSuccess {
            case customer: Customer =>
              val response = new UpdateCustomerResponse
              response setReturn customer
              marshaller forward SOAPMarshallingMessage(
                objectFactory createUpdateCustomerResponse response, originalSender)
          }
          f onFailure {
            case e: NoSuchCustomerException => marshaller forward SOAPMarshallingMessage(
              objectFactory createNoSuchCustomer e.getFaultInfo, originalSender)
            case e: Exception => throw e
          }

        }

        //TODO: Else send Failure!!!
      }
    }

  }
}
