package gehversuch.customerservice

import akka.actor.{Actor, ActorRef}
import akka.camel.{CamelMessage, Consumer}
import gehversuch.Configuration
import org.apache.camel.builder.Builder
import akka.actor.Status.Failure

/**
 * Created by rdu on 30.04.14.
 */
class CustomerServiceConsumer(producer: ActorRef) extends Actor with Consumer {

  val portHttp = Configuration.portHttp
  val host = Configuration.host
  def endpointUri: String = s"jetty:http://$host:$portHttp/CustomerServicePort"

  def receive = {
    case msg: CamelMessage => producer.forward(msg.withBodyAs[String])
  }

  /*override def onRouteDefinition = (rd) => rd.onException(classOf[Exception]).
    handled(true).transform(Builder.exceptionMessage).end

  final override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
  }*/

}

import akka.camel.Producer

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
