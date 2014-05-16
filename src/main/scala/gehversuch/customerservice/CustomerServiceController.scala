package gehversuch.customerservice

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import gehversuch.soap._
import gehversuch.soap.MarshallingRequest
import gehversuch.soap.UnmarshallingRequest
import gehversuch.soap.UnmarshallingResponse
import gehversuch.soap.MarshallingUnmodeledFaultRequest
import gehversuch.soap.MarshallingModeledFaultRequest

/**
 * Created by dueerkopra on 15.05.2014.
 */

/*sealed trait TransformationState
case object Unattended extends TransformationState
case object Received extends TransformationState
case object Unmarshalled extends TransformationState
case object Processed extends TransformationState*/

class CustomerServiceController extends Actor with ActorLogging {

  val unmarshaller = context.actorOf(Props[SOAPUnmarshaller])
  val serviceDelegator = context.actorOf(Props[CustomerServiceDelegator])
  val marshaller = context.actorOf(Props[SOAPMarshaller])
  var responseChannel: ActorRef = null

  override def receive = {

    case UnmarshallingRequest(message, prototype) => log.debug("SOAP request received: {}, ready to marshall", message)
      unmarshaller ! UnmarshallingRequest(message, prototype)
      responseChannel = sender

    case UnmarshallingResponse(result) => log.debug("Unmarshalling finished, ready call service")
      serviceDelegator ! result

    case MarshallingRequest(element) => log.debug("Service finished successfully, ready to marshall to response")
      marshaller ! MarshallingRequest(element)

    case MarshallingModeledFaultRequest(faultBean, message) => log.debug("Service threw exception, ready to marshall to fault")
      marshaller ! MarshallingModeledFaultRequest(faultBean, message)

    case MarshallingUnmodeledFaultRequest(e) => log.debug("Some unexpected exception, ready to marshall to fault")
      marshaller ! MarshallingUnmodeledFaultRequest(e)

    case MarshallingResponse(message) => log.debug("SOAP response marshalled: {}, ready to answer", message)
      responseChannel ! message
  }
}
