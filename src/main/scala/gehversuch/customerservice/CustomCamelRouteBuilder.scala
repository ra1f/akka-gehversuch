package gehversuch.customerservice

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.dataformat.SoapJaxbDataFormat
import org.apache.camel.dataformat.soap.name.ServiceInterfaceStrategy
import de.gehversuch.customerservice.{NoSuchCustomerException, CustomerService}
import akka.actor.{ActorSystem, ActorRef}

/**
 * Created by rdu on 30.04.14.
 */
class CustomCamelRouteBuilder extends RouteBuilder {

  def configure {

    val soapDF = new SoapJaxbDataFormat("de.gehversuch.customerservice",
      new ServiceInterfaceStrategy(classOf[CustomerService], false))

    //onException().marshal(soapDF)

    from("direct:customerService")
      .unmarshal(soapDF)
      .process(new CustomerServiceProcessor)
      .marshal(soapDF)
  }

}
