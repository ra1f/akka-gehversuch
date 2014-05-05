package gehversuch.customerservice

import org.apache.camel.{Exchange, Processor}
import de.gehversuch.customerservice._

/**
 * Created by dueerkopra on 28.04.2014.
 */
class CustomerServiceProcessor extends Processor {

  private val customerService: CustomerService = new CustomerServiceImpl()

  override def process(exchange: Exchange): Unit = {

    val inMessage = exchange.getIn
    inMessage.getHeader("SOAPAction") match {

      case "getCustomersByName" => {
        val name: String = inMessage.getBody(classOf[GetCustomersByName]).getName
        val customers: Array[Customer] = customerService.getCustomersByName(name)
        try {
          val response = new GetCustomersByNameResponse
          response.setReturn(customers)
          exchange.getOut.setBody(response)
        } catch {
          case e: NoSuchCustomerException => {
            exchange.getOut.setFault(true)
            val errorResponse = new NoSuchCustomer
            exchange.getOut.setBody(errorResponse)
          }
        }
      }

      case "updateCustomer" => {
        var customer: Customer = inMessage.getBody(classOf[UpdateCustomer]).getCustomer
        customer = customerService.updateCustomer(customer)
        val response = new UpdateCustomerResponse
        response.setReturn(customer)
        exchange.getOut.setBody(response)
      }

    }
  }
}
