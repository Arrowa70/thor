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

package org.seekloud.thor.front

/**
  * User: TangYaruo
  * Date: 2018/11/29
  * Time: 21:48
  */
package object model {
  case class PlayerInfo(userId:String, userName:String, accessCode:String, roomIdOpt:Option[Long])

  /**发送观看消息链接信息*/
  case class ReplayInfo(recordId:Long,playerId:String,frame:Int,accessCode:String)
}
