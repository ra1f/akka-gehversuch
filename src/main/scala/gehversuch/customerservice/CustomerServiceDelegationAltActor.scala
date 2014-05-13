package gehversuch.customerservice

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import de.gehversuch.customerservice._
import gehversuch.soap._
import akka.camel.{CamelExtension, CamelMessage}
import scala.concurrent.{Future, ExecutionContext}
import java.util.concurrent.ForkJoinPool
import gehversuch.soap.SOAPMarshallingSuccessMessage
import gehversuch.soap.SOAPMarshallingUnmodeledFaultMessage

/**
 * Created by dueerkopra on 13.05.2014.
 */
class CustomerServiceDelegationAltActor extends Actor with ActorLogging {

  val customerService: CustomerService = new CustomerServiceImpl
  val camelContext = CamelExtension(context.system).context
  val objectFactory = new ObjectFactory

  def receive = {

    case msg: CamelMessage =>

      msg.getHeaderAs("SOAPAction", classOf[String], camelContext) match {

        case "getCustomersByName" =>
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
              sender ! SOAPMarshallingSuccessMessage(
                objectFactory createGetCustomersByNameResponse response, originalSender)
          }
          f onFailure {
            case e: NoSuchCustomerException => sender ! modeledFault(e, originalSender)
            case e: Exception => sender ! unmodeledFault(e, originalSender)
          }


        case "updateCustomer" =>
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
              sender ! SOAPMarshallingSuccessMessage(
                objectFactory createUpdateCustomerResponse response, originalSender)
          }
          f onFailure {
            case e: NoSuchCustomerException => sender ! modeledFault(e, originalSender)
            case e: Exception => sender ! unmodeledFault(e, originalSender)
          }

      }

    case msg: SOAPMarshallingUnmodeledFaultMessage => sender ! msg
  }

  def modeledFault(e: NoSuchCustomerException, originalSender: ActorRef) = {

    val faultBean = objectFactory.createNoSuchCustomer(e.getFaultInfo)
    SOAPMarshallingModeledFaultMessage(faultBean, e.getMessage, originalSender)
  }

  def unmodeledFault(e: Exception, originalSender: ActorRef) = SOAPMarshallingUnmodeledFaultMessage(e, originalSender)
}
