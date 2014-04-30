package gehversuch.customerservice

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.dataformat.SoapJaxbDataFormat
import org.apache.camel.dataformat.soap.name.ServiceInterfaceStrategy
import cc.notsoclever.customerservice.CustomerService

/**
 * Created by rdu on 30.04.14.
 */
class CustomCamelRouteBuilder extends RouteBuilder {

  def configure {

    val soapDF = new SoapJaxbDataFormat("cc.notsoclever.customerservice", new ServiceInterfaceStrategy(classOf[CustomerService], false))
    /*from("direct:welcome").process(new Processor() {
      def process(exchange: Exchange) {
        // Create a 'welcome' message from the input message
        exchange.getOut.setBody("Welcome %s" format exchange.getIn.getBody)
      }
    })*/
    from("direct:customerService").unmarshal(soapDF).process(new CustomerServiceProcessor()).marshal(soapDF)
  }
}
