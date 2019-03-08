/*
 *   Copyright 2018 seekloud (https://github.com/seekloud)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.seekloud.thor

import akka.actor.{ActorSystem, Scheduler}
import akka.actor.typed.scaladsl.adapter._
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import akka.util.Timeout
import javafx.application.Platform
import javafx.stage.Stage
import org.seekloud.thor.actor.WsClient
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.controller.{GameController, ModeSelectController}
import org.seekloud.thor.model.{GameServerInfo, PlayerInfo, UserInfo}
import org.seekloud.thor.scene.{GameScene, ModeScene}
import org.slf4j.LoggerFactory

import concurrent.duration._
import scala.language.postfixOps

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 19:00
  */

object ClientBoot {

  import org.seekloud.thor.common.AppSettings._

  implicit val system: ActorSystem = ActorSystem("thor", config)
  implicit val executor: MessageDispatcher = system.dispatchers.lookup("akka.actor.my-blocking-dispatcher")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = Timeout(20 seconds)

  def addToPlatform(fun: => Unit) = {
    Platform.runLater(() => fun)
  }
}

class ClientBoot extends javafx.application.Application {

  import ClientBoot._

  private[this] val log = LoggerFactory.getLogger(this.getClass)

  override def start(primaryStage: Stage): Unit = {

    val context = new StageContext(primaryStage)
    val gameScene = new GameScene

//    val wsClient = system.spawn(WsClient.create(gameController), "WsClient")

    //TODO
//    val modeScene = new ModeScene()
//    val modeSelectController = new ModeSelectController(wsClient, modeScene, context)
//    modeSelectController.showScene()
    addToPlatform{
      context.switchScene(gameScene.getScene, title = "Thor Play", true, false)
    }

    new GameController(PlayerInfo(UserInfo(1000L,"test2","test",949848944L),"test1","test2","asd"),
      GameServerInfo("",30376L,""),context,gameScene).start()


  }


}