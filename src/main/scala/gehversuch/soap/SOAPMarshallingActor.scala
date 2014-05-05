package gehversuch.soap

import akka.actor.{ActorRef, Actor}
import akka.camel.CamelMessage
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.bind.{JAXBElement, JAXBContext}
import javax.xml.soap.MessageFactory
import java.io.ByteArrayOutputStream

/**
 * Created by dueerkopra on 02.05.2014.
 */
class SOAPMarshallingActor extends Actor {

  def receive = {
    case msg: JAXBElement[Any] => {
      val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument
      val marshaller = JAXBContext.newInstance(msg.getDeclaredType).createMarshaller
      marshaller.marshal(msg, document)
      val soapMessage = MessageFactory.newInstance.createMessage
      soapMessage.getSOAPBody.addDocument(document)
      val outputStream = new ByteArrayOutputStream
      soapMessage.writeTo(outputStream)
      val xml = new String(outputStream.toByteArray)
      sender ! xml
    }
    //case _ => msg
  }
}
