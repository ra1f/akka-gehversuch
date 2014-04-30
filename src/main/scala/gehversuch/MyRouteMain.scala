package gehversuch

import org.apache.camel.main.Main
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import akka.camel.{Producer, CamelMessage, Consumer, CamelExtension}
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.{Exchange, Processor}
import org.apache.camel.model.dataformat.SoapJaxbDataFormat
import cc.notsoclever.customerservice.{Customer, GetCustomersByName, CustomerService}
import org.apache.camel.dataformat.soap.name.ServiceInterfaceStrategy
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import javax.xml.bind.JAXBContext
import javax.xml.soap.MessageFactory
import java.io.ByteArrayOutputStream

/**
 * A Main to run Camel with MyRouteBuilder
 */
object MyRouteMain /*extends RouteBuilderSupport*/ {

  def main(args: Array[String]): Unit = {
    /*val main = new Main()
    // enable hangup support so you need to use ctrl + c to stop the running app
    main.enableHangupSupport();
    main.addRouteBuilder(new MyRouteBuilder())
    // must use run to start the main application
    main.run();*/
    val system = ActorSystem("some-system")
    //val producer = system.actorOf(Props[RouteProducer])
    val producer = system.actorOf(Props[CustomerServiceProducer])
    //val mediator = system.actorOf(Props(classOf[RouteTransformer], producer))
    //val consumer = system.actorOf(Props(classOf[RouteConsumer], mediator))
    val consumer = system.actorOf(Props(classOf[CustomerServiceConsumer], producer))
    val extension = CamelExtension(system)
    extension.context.addRoutes(new CustomRouteBuilder)
  }

  /*class RouteConsumer(transformer: ActorRef) extends Actor with Consumer {
    def endpointUri = "jetty:http://0.0.0.0:8877/camel/welcome"

    def receive = {
      // Forward a string representation of the message body to transformer
      case msg: CamelMessage => transformer.forward(msg.withBodyAs[String])
    }
  }*/

  class CustomerServiceConsumer(producer: ActorRef) extends Actor with Consumer {
    def endpointUri: String = "jetty:http://0.0.0.0:8877/camel/welcome"

    def receive = {
      case msg: CamelMessage => producer.forward(msg.withBodyAs[String])
    }

    //def endpointUri: String = "jetty:http://localhost:9090/CustomerServicePort"
  }

  /*class RouteProducer extends Actor with Producer {
    def endpointUri = "direct:welcome"
  }*/

  class CustomerServiceProducer extends Actor with Producer {

    override def endpointUri: String = "direct:customerService"

    /*override protected def transformResponse(msg: Any): Any =  msg match {

      case msg: CamelMessage => {
          val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument
          val marshaller = JAXBContext.newInstance(classOf[Customer]).createMarshaller
          val body = msg.body
          body match {
            case body: java.util.List => body
          }
          marshaller.marshal(msg.body, document)
          val soapMessage = MessageFactory.newInstance.createMessage
          soapMessage.getSOAPBody.addDocument(document)
          val outputStream = new ByteArrayOutputStream
          soapMessage.writeTo(outputStream)
          val xml = new String(outputStream.toByteArray)
          new CamelMessage(xml, msg.headers)
        }
        case _ => msg
      }*/
  }

  class CustomRouteBuilder extends RouteBuilder {

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
}

