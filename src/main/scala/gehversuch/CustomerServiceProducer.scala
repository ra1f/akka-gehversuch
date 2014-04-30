package gehversuch

import akka.actor.Actor
import akka.camel.Producer

/**
 * Created by rdu on 30.04.14.
 */
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
