package mnet.de

import cc.notsoclever.customerservice._
import org.apache.camel.{Exchange, Processor}
import org.apache.camel.component.cxf.common.message.CxfConstants
import java.util
import scala.collection.JavaConversions._

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
        //exchange.getOut.setBody(seqAsJavaList[Customer](customers))
        //exchange.getOut.setBody(util.Arrays.asList(customers.toArray[Customer](Array())))
        //exchange.getOut.setBody[util.List[Customer]](customers, classOf[util.List[Customer]])
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
