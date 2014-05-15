package gehversuch.customerservice

import akka.actor.{ActorRef, ActorLogging, Actor}
import de.gehversuch.customerservice._
import scala.concurrent.{Future, ExecutionContext}
import java.util.concurrent.ForkJoinPool
import gehversuch.soap.{MarshallingRequest, MarshallingModeledFaultRequest, MarshallingUnmodeledFaultRequest}

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
              originalSender ! MarshallingRequest(objectFactory createGetCustomersByNameResponse response)
          }
          f onFailure {
            case e: NoSuchCustomerException => originalSender ! modeledFault(e)
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
              originalSender ! MarshallingRequest(objectFactory createUpdateCustomerResponse response)
          }
          f onFailure {
            case e: NoSuchCustomerException => originalSender ! modeledFault(e)
            case e: Exception => originalSender ! unmodeledFault(e, originalSender)
          }

  }

  def modeledFault(e: NoSuchCustomerException) = {

    val faultBean = objectFactory.createNoSuchCustomer(e.getFaultInfo)
    MarshallingModeledFaultRequest(faultBean, e.getMessage)
  }

  def unmodeledFault(e: Exception, originalSender: ActorRef) = MarshallingUnmodeledFaultRequest(e)
}
