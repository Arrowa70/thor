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

package org.seekloud.thor.common

import java.net.URLEncoder

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
object Routes {
  def getJoinGameWebSocketUri(playerId: String, name:String, accessCode: String, domain:String,roomIdOpt:Option[String]):String ={
    val wsProtocol = "ws"
    val domain = "localhost:30376"

    s"$wsProtocol://${domain}/thor/game/join?name=${URLEncoder.encode(name,"utf-8")}"
    "ws://flowdev.neoap.com/thor/game/join?name=111"
  }

  def wsJoinGameUrl(playerId: String, name:String, accessCode: String, roomIdOpt:Option[String]):String = {
    s"game/playGame/userJoin?playerId=$playerId&playerName=$name&accessCode=$accessCode" +
      (roomIdOpt match {
        case Some(roomId) =>
          s"&roomId=$roomId"
        case None =>
          ""
      })
  }
}
