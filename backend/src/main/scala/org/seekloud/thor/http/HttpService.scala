/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.http

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import org.seekloud.thor.common.AppSettings
import akka.actor.typed.scaladsl.AskPattern._
import java.net.URLEncoder

import scala.concurrent.{ExecutionContextExecutor, Future}
import org.seekloud.thor.Boot.{eSheepLinkClient, executor, scheduler, timeout, userManager}
import org.seekloud.thor.core.{ESheepLinkClient, UserManager}
import org.seekloud.thor.protocol.ESheepProtocol.{ErrorGetPlayerByAccessCodeRsp, GetPlayerByAccessCodeRsp}
import org.seekloud.thor.shared.ptcl.{TestPswRsp}


trait HttpService
  extends ResourceService
  with ServiceUtils
  with PlatService
  with RoomInfoService
  with ReplayService{

  import akka.actor.typed.scaladsl.AskPattern._
  import org.seekloud.utils.CirceSupport._
  import io.circe.generic.auto._

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler


  import akka.actor.typed.scaladsl.adapter._


  private val getTestPsw: Route = (path("getTestPsw") & get){
    val psw = AppSettings.testPsw
    complete(TestPswRsp(psw))
  }

  private val getVersion: Route = (path("getVersion") & get){
    val version = AppSettings.version
    complete(TestPswRsp(version))
  }

  lazy val routes: Route = pathPrefix(AppSettings.rootPath) {
    resourceRoutes ~ platEnterRoute ~ roomInfoRoutes ~ replayRoutes ~ getTestPsw ~ getVersion ~
      (pathPrefix("game") & get){
        pathEndOrSingleSlash{
          getFromResource("html/admin.html")
        } ~
          path("join"){
          parameter('name){ name =>
            val flowFuture:Future[Flow[Message,Message,Any]] = userManager ? (UserManager.GetWebSocketFlow("test", name,_,None))
            dealFutureResult(
              flowFuture.map(t => handleWebSocketMessages(t))
            )
          }
        } ~
          (path("watchGame") & get & pathEndOrSingleSlash){
            parameter(
              'roomId.as[Long],
              'playerId.as[String],
              'accessCode.as[String]
            ){
              case (roomId, playerId, accessCode) =>
                val VerifyAccessCode: Future[GetPlayerByAccessCodeRsp] = eSheepLinkClient ? (ESheepLinkClient.VerifyAccessCode(accessCode, _))
                dealFutureResult {
                  VerifyAccessCode.flatMap {
                    case GetPlayerByAccessCodeRsp(data, 0, "ok") =>
                      println("ws and access ok")
                      val flowFuture: Future[Flow[Message, Message, Any]] = userManager ? (UserManager.GetWebSocketFlow4Watch(roomId, playerId, _, data.get.playerId, data.get.nickname))
                      flowFuture.map(t => handleWebSocketMessages(t))
                    case GetPlayerByAccessCodeRsp(data, _, _) =>
                      println("ws and accessCode error")
                      Future(complete(ErrorGetPlayerByAccessCodeRsp))
                  }
                }
            }
        } ~
          (path("replay") & get & pathEndOrSingleSlash){
            parameter(
              'recordId.as[Long],
              'accessCode.as[String],
              'frame.as[Long],
              'playerId.as[String]
            ){
              case (recordId, accessCode, frame,  playerId) =>
                val VerifyAccessCode: Future[GetPlayerByAccessCodeRsp] = eSheepLinkClient ? (ESheepLinkClient.VerifyAccessCode(accessCode, _))
                dealFutureResult {
                  VerifyAccessCode.flatMap {
                    case GetPlayerByAccessCodeRsp(data, 0, "ok") =>
                      println("ws and access ok")
                      val flowFuture: Future[Flow[Message, Message, Any]] = userManager ? (UserManager.GetWebSocketFlow4Replay(recordId, frame, playerId, _, data.get.playerId, data.get.nickname))
                      flowFuture.map(t => handleWebSocketMessages(t))
                    case GetPlayerByAccessCodeRsp(data, _, _) =>
                      println("ws and accessCode error")
                      Future(complete(ErrorGetPlayerByAccessCodeRsp))
                  }
                }
            }
          } ~ platGameRoutes

      }
  }

  def platEnterRoute: Route = path("playGame"){
    parameter(
      'playerId.as[String],
      'playerName.as[String],
      'accessCode.as[String],
      'roomId.as[Long].?
    ) {
      case (playerId, playerName, accessCode, roomIdOpt) =>
        redirect(s"/thor/game/#/playGame/$playerId/${URLEncoder.encode(playerName, "utf-8")}" + roomIdOpt.map(s => s"/$s").getOrElse("") + s"/$accessCode",
          StatusCodes.SeeOther
        )

    }
  } ~ path("watchGame") {
    parameter(
      'roomId.as[Long],
      'accessCode.as[String],
      'playerId.as[String].?
    ) {
      case (roomId, accessCode, playerIdOpt) =>
        redirect(s"/thor/game/#/watchGame/$roomId" + playerIdOpt.map(s => s"/$s").getOrElse("") + s"/$accessCode",
          StatusCodes.SeeOther
        )

    }
  } ~ path("watchRecord") {
    parameter(
      'recordId.as[Long],
      'accessCode.as[String],
      'frame.as[Long],
      'playerId.as[String]
    ) {
      case (recordId, accessCode, frame,  playerId) =>
        redirect(s"/thor/game/#/watchRecord/$recordId" + s"/$playerId" + s"/$frame" + s"/$accessCode",
          StatusCodes.SeeOther
        )

    }
  }




}
