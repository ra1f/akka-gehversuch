package gehversuch.customerservice

import akka.actor.{ActorRef, ActorLogging, Actor}
import de.gehversuch.customerservice._
import scala.concurrent.{Future, ExecutionContext}
import java.util.concurrent.ForkJoinPool
import gehversuch.soap.{SOAPMarshallingSuccessMessage, SOAPModeledFaultMessage, SOAPUnmodeledFaultMessage}

/**
 * Created by dueerkopra on 13.05.2014.
 */
class CustomerServiceDelegator extends Actor with ActorLogging {

  val customerService: CustomerService = new CustomerServiceImpl
  val objectFactory = new ObjectFactory

  def receive = {

        case o: GetCustomersByName =>
          val name = o.getName
          implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool)
          val f = Future {
            customerService getCustomersByName name
          }
          val originalSender = sender
          f onSuccess {
            case customers: Array[Customer] =>
              val response = new GetCustomersByNameResponse
              response setReturn customers
              originalSender ! SOAPMarshallingSuccessMessage(
                objectFactory createGetCustomersByNameResponse response, originalSender)
          }
          f onFailure {
            case e: NoSuchCustomerException => originalSender ! modeledFault(e, originalSender)
            case e: Exception => originalSender ! unmodeledFault(e, originalSender)
          }


        case o: UpdateCustomer =>
          val customer = o.getCustomer
          implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool)
          val f = Future {
            customerService updateCustomer customer
          }
          val originalSender = sender
          f onSuccess {
            case customer: Customer =>
              val response = new UpdateCustomerResponse
              response setReturn customer
              originalSender ! SOAPMarshallingSuccessMessage(
                objectFactory createUpdateCustomerResponse response, originalSender)
          }
          f onFailure {
            case e: NoSuchCustomerException => originalSender ! modeledFault(e, originalSender)
            case e: Exception => originalSender ! unmodeledFault(e, originalSender)
          }

  }

  def modeledFault(e: NoSuchCustomerException, originalSender: ActorRef) = {

    val faultBean = objectFactory.createNoSuchCustomer(e.getFaultInfo)
    SOAPModeledFaultMessage(faultBean, e.getMessage, originalSender)
  }

  def unmodeledFault(e: Exception, originalSender: ActorRef) = SOAPUnmodeledFaultMessage(e, originalSender)
}
