package gehversuch.customerservice

import org.apache.camel.{Exchange, Processor}
import cc.notsoclever.customerservice._

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
        val response = new GetCustomersByNameResponse
        response.setReturn(customers)
        exchange.getOut.setBody(response)
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
