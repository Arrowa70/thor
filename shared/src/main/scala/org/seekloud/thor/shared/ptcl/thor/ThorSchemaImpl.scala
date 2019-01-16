package org.seekloud.thor.shared.ptcl.thor

import org.seekloud.thor.shared.ptcl.component.{AdventurerImpl, AdventurerState, Food}
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/14
  * Time: 16:54
  */
class ThorSchemaImpl(
  override val config: ThorGameConfig,
  myId: String,
  myName: String
) extends ThorSchema with EsRecover {

  import scala.language.implicitConversions

  protected var aId: String = myId

  def changeAdventurerId(id: String) = aId = id

  override def debug(msg: String): Unit = {}

  override def info(msg: String): Unit = println(msg)

  private val esRecoverSupport: Boolean = true

  private val uncheckedActionMap = mutable.HashMap[Byte, Int]() //serinum -> frame

  private var thorSchemaStateOpt: Option[ThorSchemaState] = None

  protected var waitSyncData: Boolean = true

  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset

  private var justSyncFrame = 0

  override protected implicit def adventurerState2Impl(adventurer: AdventurerState) = {
    new AdventurerImpl(config, adventurer)
  }

  def receiveGameEvent(e: GameEvent) = {
    if (e.frame >= systemFrame) {
      addGameEvent(e)
    } else if (esRecoverSupport) {
//      println(s"rollback-frame=${e.frame}, curFrame=${this.systemFrame},e=$e")
      if (e.isInstanceOf[EatFood] || e.isInstanceOf[GenerateFood]) {
        e match {
          case event: EatFood => addGameEvent(event.copy(frame = systemFrame))
          case event: GenerateFood => addGameEvent(event.copy(frame = systemFrame))
          case _ => println(s"unknown case.")
        }
      } else {
        rollback4GameEvent(e)
      }
    }
  }

  //接受服务器的用户事件
  def receiveUserEvent(e: UserActionEvent) = {
    val pIdStr = byteId2PlayerId(e.playerId)
    pIdStr match {
      case Right(pId) =>
        if (pId == aId) {
          uncheckedActionMap.get(e.serialNum) match {
            case Some(preFrame) =>
              if (e.frame != preFrame) {
                //            println(s"preFrame=$preFrame eventFrame=${e.frame} curFrame=$systemFrame")
                if (preFrame < e.frame && esRecoverSupport) {
                  if (preFrame >= systemFrame) {
                    removePreEvent(preFrame, e.playerId, e.serialNum)
                    addUserAction(e)
                  } else if (e.frame >= systemFrame) {
                    //preFrame 比 systemFrame小，但事件frame比systemFrame大，删除preFrame历史数据，回滚后加入事件
                    removePreEventHistory(preFrame, e.playerId, e.serialNum)
                    println(s"roll back to $preFrame curFrame $systemFrame because of UserActionEvent $e")
                    addRollBackFrame(preFrame)
                    addUserAction(e)
                  } else {
                    //preFrame 比 systemFrame小，事件frame比systemFrame小，删除preFrame历史数据，加入事件e为历史，回滚
                    removePreEventHistory(preFrame, e.playerId, e.serialNum)
                    addUserActionHistory(e)
                    println(s"roll back to $preFrame curFrame $systemFrame because of UserActionEvent $e")
                    addRollBackFrame(preFrame)
                  }
                }
              }
            case None =>
              if (e.frame >= systemFrame) {
                addUserAction(e)
              } else if (esRecoverSupport) {
                rollback4UserActionEvent(e)
              }
          }
        } else {
          if (e.frame >= systemFrame) {
            addUserAction(e)
          } else if (esRecoverSupport) {
            //        println(s"rollback-frame=${e.frame},curFrame=${this.systemFrame},e=$e")
            rollback4GameEvent(e)
          }
        }
      case Left(_) => // do nothing
    }


  }

  def preExecuteUserEvent(action: UserActionEvent) = {
    addUserAction(action)
    uncheckedActionMap.put(action.serialNum, action.frame)
  }

  final def addMyAction(action: UserActionEvent): Unit = {
    val pIdStr = byteId2PlayerId(action.playerId)
    pIdStr match {
      case Right(pId) =>
        if (pId == aId) {
          myAdventurerAction.get(action.frame - preExecuteFrameOffset) match {
            case Some(actionEvents) => myAdventurerAction.put(action.frame - preExecuteFrameOffset, action :: actionEvents)
            case None => myAdventurerAction.put(action.frame - preExecuteFrameOffset, List(action))
          }
        }
      case Left(_) => // do nothing
    }

  }

  protected def handleThorSchemaState(thorSchemaSate: ThorSchemaState, isRollBack: Boolean = false) = {
    val curFrame = systemFrame
    val startTime = System.currentTimeMillis()
    (math.max(curFrame, thorSchemaSate.f - 100) until thorSchemaSate.f).foreach { _ =>
      super.update()
      if (esRecoverSupport) addGameSnapshot(systemFrame, getThorSchemaState())
    }
    val endTime = System.currentTimeMillis()
    if (curFrame < thorSchemaSate.f) {
      println(s"handleThorSchemaState update to now use time=${endTime - startTime}")
      justSyncFrame = thorSchemaSate.f
    } else if (!isRollBack) {
      println(s"handleThorSchemaState roll back to ${thorSchemaSate.f}.")
      justSyncFrame = thorSchemaSate.f
    }

    systemFrame = thorSchemaSate.f

    quadTree.clear()
    tmpAdventurerMap.clear()
    if (!thorSchemaSate.isIncrement) tmpFoodMap.clear()
    thorSchemaSate.adventurer.foreach { a =>
      val adventurer = new AdventurerImpl(config, a)
      quadTree.insert(adventurer)
      tmpAdventurerMap.put(a.playerId, adventurer)
    }
    if (!thorSchemaSate.isIncrement) {
      thorSchemaSate.food.foreach { f =>
        val food = Food(f)
        quadTree.insert(food)
        tmpFoodMap.put(f.fId, food)
      }
    } else {
      thorSchemaSate.food.foreach { f =>
        val food = Food(f)
        tmpFoodMap.put(f.fId, food)
      }
      tmpFoodMap.values.foreach { f => quadTree.insert(f) }
    }
    adventurerMap = tmpAdventurerMap
    foodMap = tmpFoodMap
    waitSyncData = false
  }

  def receiveThorSchemaState(thorSchemaState: ThorSchemaState): Unit = {
    if (thorSchemaState.f > systemFrame) {
      thorSchemaStateOpt = Some(thorSchemaState)
    } else if (thorSchemaState.f == systemFrame) {
      info(s"收到同步数据，立即同步，curSystemFrame=$systemFrame, sync thor schema state frame=${thorSchemaState.f}")
      thorSchemaStateOpt = None
      handleThorSchemaState(thorSchemaState)
    } else {
      if (systemFrame - thorSchemaState.f < 10) {
        info(s"收到滞后数据，立即同步，curSystemFrame=$systemFrame, sync game container state frame=${thorSchemaState.f}")
        handleThorSchemaState(thorSchemaState)
      } else {
        info(s"收到滞后数据，不同步，curSystemFrame=$systemFrame, sync game container state frame=${thorSchemaState.f}")
      }
    }

  }

  override def update(): Unit = {
    if (thorSchemaStateOpt.nonEmpty) {
      val thorSchemaState = thorSchemaStateOpt.get
      info(s"逻辑帧同步，curSystemFrame=$systemFrame, sync thor schema state frame=${thorSchemaState.f}")
      handleThorSchemaState(thorSchemaState)
      thorSchemaStateOpt = None
      if (esRecoverSupport) {
        clearEsRecoverData()
        addGameSnapshot(systemFrame, this.getThorSchemaState())
      }
    } else {
//      super.update()
      if (esRecoverSupport) {
        if (rollBackFrame.nonEmpty) {
          rollBackFrame.distinct.filterNot(r => r < justSyncFrame || r >= systemFrame).sortWith(_ < _).headOption.foreach(rollback)
          super.update()
        } else {
          super.update()
          addGameSnapshot(systemFrame, this.getThorSchemaState())
        }
      } else {
        super.update()
      }
    }
  }


  override protected def clearEventWhenUpdate(): Unit = {
    super.clearEventWhenUpdate()
    if (esRecoverSupport) {
      addEventHistory(systemFrame, gameEventMap.getOrElse(systemFrame, Nil), actionEventMap.getOrElse(systemFrame, Nil))
    }
    gameEventMap -= systemFrame
    actionEventMap -= systemFrame
    systemFrame += 1
  }

  protected def rollbackUpdate(): Unit = {
    super.update()
    if (esRecoverSupport) addGameSnapshot(systemFrame, getThorSchemaState())
  }
}
