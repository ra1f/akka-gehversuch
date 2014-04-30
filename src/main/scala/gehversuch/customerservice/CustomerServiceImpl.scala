package gehversuch.customerservice

import java.util.GregorianCalendar
import java.math.BigDecimal
import scala.collection.JavaConversions._
import cc.notsoclever.customerservice.{CustomerType, Customer, CustomerService}

/**
 * Created by dueerkopra on 28.04.2014.
 */
class CustomerServiceImpl extends CustomerService {

  /*private def customers: Map[String, Customer] = {

    val customer1 = new Customer
    val name = "Johns, Mary"
    customer1 setName name
    customer1.getAddress.add("Pine Street 200")
    val bDate = new GregorianCalendar(2009, 1, 1).getTime
    customer1 setBirthDate bDate
    customer1 setNumOrders 1
    customer1 setRevenue 10000
    customer1 setTest new BigDecimal(1.5)
    customer1 setType CustomerType.BUSINESS
    Map(name -> customer1)
  }*/

  private def customers: List[Customer] = {

    val customer1 = new Customer
    val name = "Johns, Mary"
    customer1 setName name
    customer1.getAddress :+ "Pine Street 200"
    val bDate = new GregorianCalendar(2009, 1, 1).getTime
    customer1 setBirthDate bDate
    customer1 setNumOrders 1
    customer1 setRevenue 10000
    customer1 setTest new BigDecimal(1.5)
    customer1 setType CustomerType.BUSINESS
    //val c = new java.util.ArrayList[Customer]
    //c.add(customer1)
    List(customer1)
  }


  override def getCustomersByName(name: String): Array[Customer] = {

    //seqAsJavaList[Customer](customers.filter((p:(String, Customer)) => p._2.getName startsWith  name).values.toList)
    //customers.filter((p:(String, Customer)) => p._2.getName startsWith  name).values.toList

    //customers.filter[Customer, java.util.ArrayList[Customer]](c => {if (c.getName.startsWith(name)) c})
    //val c = new java.util.ArrayList[Customer]
    //c.add(customers.get(0))

    Array(customers.get(0))
  }

  override def updateCustomer(customer: Customer): Customer = {
    //customers :+ customer
    customer
  }
}
