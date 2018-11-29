package org.seekloud.thor.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.seekloud.thor.core.UserActor.{ChangeUserInfo, ChangeWatchedPlayerId}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.utils.byteObject.MiddleBufferInJvm
import org.slf4j.LoggerFactory

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object UserManager {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  final case class ChildDead[U](name: String, childRef: ActorRef[U]) extends Command

  final case class GetWebSocketFlow(id: String, name:String,replyTo:ActorRef[Flow[Message,Message,Any]], roomId:Option[Long] = None) extends Command
  final case class GetWebSocketFlow4Watch(roomId: Long, watchedPlayerId:String,replyTo:ActorRef[Flow[Message,Message,Any]], watchingId: String, name: String) extends Command

  def create(): Behavior[Command] = {
    log.debug(s"UserManager start...")
    Behaviors.setup[Command] {
      ctx =>
        Behaviors.withTimers[Command] {
          implicit timer =>
            val uidGenerator = new AtomicLong(1L)
            idle(uidGenerator)
        }
    }
  }

  private def idle(uidGenerator: AtomicLong)
                  (
                    implicit timer: TimerScheduler[Command]
                  ): Behavior[Command] = {
    Behaviors.receive[Command]{
      (ctx, msg) =>
        msg match {
          case GetWebSocketFlow(id, name,replyTo, roomIdOpt) =>

//              val playerInfo = UserInfo(uidGenerator.getAndIncrement().toString, name)
            val playerInfo = UserInfo(if(id.equals("test")) uidGenerator.getAndIncrement().toString else id, name)

            val userActor = getUserActor(ctx, playerInfo.playerId, playerInfo)
            replyTo ! getWebSocketFlow(userActor)
            userActor ! UserActor.StartGame(roomIdOpt)
            Behaviors.same

          case GetWebSocketFlow4Watch(roomId, watchedPlayerId, replyTo, watchingId, name) =>
            log.debug(s"$watchingId GetWebSocketFlow4Watch")
            val playerInfo = UserInfo(watchingId, name)
            getUserActorOpt(ctx, watchingId) match {
              case Some(userActor) =>
                userActor ! UserActor.ChangeBehaviorToInit
              case None =>
            }
            val userActor = getUserActor(ctx, watchingId, playerInfo)
            replyTo ! getWebSocketFlow(userActor)
            userActor ! ChangeUserInfo(playerInfo)
            //发送用户观战命令
            userActor ! UserActor.StartWatching(roomId, watchedPlayerId)
            Behaviors.same

          case msg:ChangeWatchedPlayerId =>
            getUserActor(ctx,msg.playerInfo.playerId,msg.playerInfo) ! msg
            Behaviors.same

          case ChildDead(child, childRef) =>
            ctx.unwatch(childRef)
            Behaviors.same

          case unknown =>
            log.error(s"${ctx.self.path} recv an unknown msg when idle:${unknown}")
            Behaviors.same
        }
    }
  }

  private def getWebSocketFlow(userActor: ActorRef[UserActor.Command]): Flow[Message, Message, Any] = {
    import scala.language.implicitConversions
    import org.seekloud.byteobject.ByteObject._
    import org.seekloud.byteobject.MiddleBufferInJvm

    implicit def parseJsonString2WsMsgFront(s: String): Option[WsMsgFront] = {
      import io.circe.generic.auto._
      import io.circe.parser._

      try {
        val wsMsg = decode[WsMsgFront](s).right.get
        Some(wsMsg)
      } catch {
        case e: Exception =>
          log.warn(s"parse front msg failed when json parse,s=${s}")
          None
      }
    }

    Flow[Message]
      .collect {
        case TextMessage.Strict(m) =>
          UserActor.WsMessage(m)

        case BinaryMessage.Strict(m) =>
          val buffer = new MiddleBufferInJvm(m.asByteBuffer)
          bytesDecode[WsMsgFront](buffer) match {
            case Right(req) => UserActor.WsMessage(Some(req))
            case Left(e) =>
              log.error(s"decode binaryMessage failed,error:${e.message}")
              UserActor.WsMessage(None)
          }
      }.via(UserActor.flow(userActor))
      .map {
        case t: Wrap =>
          BinaryMessage.Strict(ByteString(t.ws))

        case x =>
          log.debug(s"akka stream receive unknown msg=${x}")
          TextMessage.apply("")
      }.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }

  private val decider: Supervision.Decider = {
    e: Throwable =>
      e.printStackTrace()
      log.error(s"WS stream failed with $e")
      Supervision.Resume
  }

  private def getUserActor(ctx: ActorContext[Command],id: String, userInfo: UserInfo):ActorRef[UserActor.Command] = {
    val childName = s"UserActor-${id}"
    ctx.child(childName).getOrElse{
      val actor = ctx.spawn(UserActor.create(id, userInfo),childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor
    }.upcast[UserActor.Command]
  }

  private def getUserActorOpt(ctx: ActorContext[Command],id:String):Option[ActorRef[UserActor.Command]] = {
    val childName = s"UserActor-${id}"
    ctx.child(childName).map(_.upcast[UserActor.Command])
  }

}
