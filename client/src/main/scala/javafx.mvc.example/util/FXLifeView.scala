package javafx.mvc.example.util

import akka.actor.ActorRef

case object LifeViewClosed

trait FXLifeView {
  var manager = Option.empty[ActorRef]
}
