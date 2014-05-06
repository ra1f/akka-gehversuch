package gehversuch.soap

import akka.actor.{ActorRef, Actor}
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.bind.{JAXBElement, JAXBContext}
import javax.xml.soap.MessageFactory
import java.io.ByteArrayOutputStream

/**
 * Created by dueerkopra on 02.05.2014.
 */

case class SOAPMarshallingMessage[T](element: JAXBElement[T], originalSender: ActorRef)

class SOAPMarshallingActor extends Actor {

  def receive = {
    case SOAPMarshallingMessage(element, originalSender) => {
      val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument
      val marshaller = JAXBContext.newInstance(element.getDeclaredType).createMarshaller
      marshaller.marshal(element, document)
      val soapMessage = MessageFactory.newInstance.createMessage
      soapMessage.getSOAPBody.addDocument(document)
      val outputStream = new ByteArrayOutputStream
      soapMessage.writeTo(outputStream)
      val xml = new String(outputStream.toByteArray)
      originalSender ! xml
    }
  }
}
