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

case class SOAPMarshallingSuccessMessage[T](element: JAXBElement[T])

case class SOAPModeledFaultMessage[T](element: JAXBElement[T], message: String)

case class SOAPUnmodeledFaultMessage(exception: Exception)

class SOAPMarshaller extends Actor {

  def receive = {

    case SOAPMarshallingSuccessMessage(element) =>

      val document = marshal(element)
      val soapMessage = MessageFactory.newInstance.createMessage

      soapMessage.getSOAPBody.addDocument(document)

      sender ! response(soapMessage)

    case SOAPModeledFaultMessage(element, message) =>

      val document = marshal(element)
      val soapMessage = MessageFactory.newInstance.createMessage

      soapMessage.getSOAPBody.
        addFault(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "SERVER"), message).
        addDetail.addChildElement(SOAPFactory.newInstance.createElement(document.getDocumentElement))

      sender ! response(soapMessage)

    case SOAPUnmodeledFaultMessage(exception) =>

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
    new String(outputStream.toByteArray)
  }
}
