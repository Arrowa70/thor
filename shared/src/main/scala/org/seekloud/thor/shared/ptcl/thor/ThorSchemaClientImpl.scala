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

package org.seekloud.thor.shared.ptcl.thor

import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.draw._
import org.seekloud.thor.shared.ptcl.util.middleware._

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:17
  */
case class ThorSchemaClientImpl (
  drawFrame: MiddleFrame,
  ctx: MiddleContext,
  override val config: ThorGameConfig,
  myId: String,
  myName: String,
  var canvasSize:Point,
  var canvasUnit:Float,
  var preCanvasFood: List[MiddleCanvas] = Nil,
  var preCanvasAdventurer: List[MiddleCanvas] = Nil,
  var preCanvasWeapon: List[MiddleCanvas] = Nil
) extends ThorSchemaImpl(config, myId, myName)
with AdventurerClient
with FoodClient
with BackgroundClient
with DrawOtherClient
with FpsRender{

  var killerNew : String = "?"
  var duringTime : String = "0"
  val ifTest: Boolean = false
  val preTime: Long = System.currentTimeMillis()

  def drawGame(mainId: String, offSetTime:Long, canvasUnit: Float, canvasBounds: Point): Unit ={
    if(!waitSyncData){
      adventurerMap.get(mainId) match{
        case Some(adventurer) =>
          //保持自己的adventurer在屏幕中央~
          val moveDistance = getMoveDistance(adventurer, offSetTime)
          val offset = canvasBounds/2 - (adventurer.getAdventurerState.position + moveDistance)

          drawBackground(offset, canvasUnit, canvasBounds)
          drawFood(offset, canvasUnit, canvasBounds)
          drawAdventurers(offSetTime, offset, canvasUnit, canvasBounds)
          drawEnergyBar(adventurer)

//          if(ifTest)
//            drawAttacking(offset, adventurer, attackingAdventureMap.getOrElse(adventurer.playerId, 3))

        case None => println("None!!!!!!")
      }
    }
    else{
      println("waitSyncData!!!!")
    }
  }

  def updateSize(bounds: Point, unit: Float): Unit ={
    canvasSize = bounds
    canvasUnit = unit
  }

  def changePreCanvas(
  preFood: List[MiddleCanvas] = Nil,
  preAdventurer: List[MiddleCanvas] = Nil,
  preWeapon: List[MiddleCanvas] = Nil
  ): Unit ={
    preCanvasFood = preFood
    preCanvasAdventurer = preAdventurer
    preCanvasWeapon = preWeapon
  }

}
