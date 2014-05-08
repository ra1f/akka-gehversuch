package gehversuch.customerservice

import java.util.GregorianCalendar
import java.math.BigDecimal
import de.gehversuch.customerservice._

/**
 * Created by dueerkopra on 28.04.2014.
 */
class CustomerServiceImpl extends CustomerService {

  private var customers: List[Customer] = {

    val customer1 = new Customer
    customer1.setCustomerId(1234)
    customer1.setName("Johns, Mary")
    customer1.getAddress :+ "Pine Street 200"
    val bDate = new GregorianCalendar(2009, 1, 1).getTime
    customer1.setBirthDate(bDate)
    customer1.setNumOrders(1)
    customer1.setRevenue(10000)
    customer1.setTest(new BigDecimal(1.5))
    customer1.setType(CustomerType.BUSINESS)
    List(customer1)
  }


  override def getCustomersByName(name: String): Array[Customer] = {

    name match {
      case "RuntimeException" => throw new RuntimeException("A completely unknown error has occurred")
      case _ => customers.filter(p => name.length > 0 && p.getName.startsWith(name)) match {
        case l => l.isEmpty match {
          case true =>
            new NoSuchCustomer match {
              case nsc: NoSuchCustomer =>
                nsc.setCustomerName(name)
                throw new NoSuchCustomerException("Customer not found", nsc)
            }
          case false => l.toArray
        }
      }
    }
  }

  override def updateCustomer(customer: Customer): Customer = {

    // Updates the customers list by customer argument (merge) if the argument
    // identified by customer name is already part of the list
    // otherwise exception is thrown
    customers.filter(c => c.getCustomerId == customer.getCustomerId) match {
      case l => l.isEmpty match {
        case true =>
          new NoSuchCustomer match {
            case nsc: NoSuchCustomer =>
              nsc.setCustomerName(customer.getName)
              throw new NoSuchCustomerException("Customer not found", nsc)
          }
        case false =>
          val id = customer.getCustomerId
          customers = customers.map(c => c.getCustomerId match {case `id` => customer; case _ => c})
      }

    }
    return customer
  }
}
