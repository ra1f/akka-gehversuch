package gehversuch.customerservice

import akka.actor.{ActorRef, Props, Actor}
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

class CustomerServiceController extends Actor {

  val unmarshaller = context.actorOf(Props[SOAPUnmarshaller])
  val serviceDelegator = context.actorOf(Props[CustomerServiceDelegator])
  val marshaller = context.actorOf(Props[SOAPMarshaller])
  var responseChannel: ActorRef = null

  override def receive = {

    case UnmarshallingRequest(message, prototype) => unmarshaller ! UnmarshallingRequest(message, prototype)
      responseChannel = sender

    case UnmarshallingResponse(result) => serviceDelegator ! result

    case MarshallingRequest(element) => marshaller ! MarshallingRequest(element)

    case MarshallingModeledFaultRequest(faultBean, message) => marshaller ! MarshallingModeledFaultRequest(faultBean, message)

    case MarshallingUnmodeledFaultRequest(e) => marshaller ! MarshallingUnmodeledFaultRequest(e)

    case MarshallingResponse(soapResponse) => responseChannel ! soapResponse
  }
}
