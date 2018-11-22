package org.seekloud.thor.shared.ptcl.protocol

import org.seekloud.thor.shared.ptcl.component.{AdventurerState, FoodState}
import org.seekloud.thor.shared.ptcl.config.{ThorGameConfig, ThorGameConfigImpl}
import org.seekloud.thor.shared.ptcl.model.Score
import org.seekloud.thor.shared.ptcl.thor.{ThorSchemaState}


object ThorGame {

  sealed trait GameEvent {
    val frame: Long
  }

  sealed trait UserEvent extends GameEvent

  trait EnvironmentEvent extends GameEvent

  sealed trait UserActionEvent extends UserEvent {
    val playerId: String
    val serialNum:Int
  }

  //前端
  sealed trait WsMsgFrontSource

  case object CompleteMsgFrontServer extends WsMsgFrontSource

  case class FailMsgFrontServer(ex: Exception) extends WsMsgFrontSource

  sealed trait WsMsgFront extends WsMsgFrontSource

  //后台
  sealed trait WsMsgSource

  case object CompleteMsgServer extends WsMsgSource

  case class FailMsgServer(ex: Exception) extends WsMsgSource
  final case class Wrap(ws:Array[Byte],isKillMsg:Boolean = false) extends WsMsgSource

  sealed trait WsMsgServer extends WsMsgSource


  final case class UserInfo(playerId: String, name: String) extends WsMsgServer

  final case class YourInfo(config: ThorGameConfigImpl, id: String, name: String) extends WsMsgServer

  final case class UserEnterRoom(playerId: String, name: String, adventurer: AdventurerState, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class UserLeftRoom(playerId: String, name: String, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class BeAttacked(playerId: String, name: String, killerId: String, killerName: String, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class EatFood(playerId: String, foodId: Long, foodLevel: Int, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class MouseMove(playerId: String, direction: Float, mouseDistance: Float, override val frame: Long, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickDownLeft(playerId: String, override val frame: Long, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickDownRight(playerId: String, override val frame: Long, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickUpRight(playerId: String, override val frame: Long, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  /*生成环境元素*/
  final case class GenerateFood(override val frame: Long, food: FoodState) extends EnvironmentEvent with WsMsgServer


  final case class RestartGame(name: String) extends WsMsgFront

  final case class Ranks(currentRank: List[Score], historyRank: List[Score]) extends WsMsgServer

  final case class GridSyncState(d: ThorSchemaState) extends WsMsgServer



  sealed trait GameSnapshot

  final case class ThorSnapshot(
    state: ThorSchemaState
  ) extends GameSnapshot

}