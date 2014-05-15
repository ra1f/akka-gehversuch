package gehversuch.soap

import akka.actor.Actor
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.bind.{JAXBElement, JAXBContext}
import javax.xml.soap._
import java.io.ByteArrayOutputStream
import javax.xml.namespace.QName
import org.w3c.dom.Document

/**
 * Created by dueerkopra on 02.05.2014.
 */

case class MarshallingRequest[T](element: JAXBElement[T])
case class MarshallingModeledFaultRequest[T](element: JAXBElement[T], message: String)
case class MarshallingUnmodeledFaultRequest(exception: Exception)
case class MarshallingResponse(message: String)

class SOAPMarshaller extends Actor {

  def receive = {

    case MarshallingRequest(element) =>

      val document = marshal(element)
      val soapMessage = MessageFactory.newInstance.createMessage

      soapMessage.getSOAPBody.addDocument(document)

      sender ! response(soapMessage)

    case MarshallingModeledFaultRequest(element, message) =>

      val document = marshal(element)
      val soapMessage = MessageFactory.newInstance.createMessage

      soapMessage.getSOAPBody.
        addFault(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "SERVER"), message).
        addDetail.addChildElement(SOAPFactory.newInstance.createElement(document.getDocumentElement))

      sender ! response(soapMessage)

    case MarshallingUnmodeledFaultRequest(exception) =>

      val soapMessage = MessageFactory.newInstance.createMessage

      soapMessage.getSOAPBody.
        addFault(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "SERVER"), exception.getClass.getName).
        addDetail.addTextNode(exception.toString)

      sender ! response(soapMessage)
  }

  def marshal(element: JAXBElement[Any]): Document = {

    val document = DocumentBuilderFactory.newInstance.newDocumentBuilder.newDocument
    val marshaller = JAXBContext.newInstance(element.getDeclaredType).createMarshaller
    marshaller.marshal(element, document)
    return document
  }

  def response(soapMessage: SOAPMessage) = {

    val outputStream = new ByteArrayOutputStream
    soapMessage.writeTo(outputStream)
    MarshallingResponse(new String(outputStream.toByteArray))
  }
}
