package org.seekloud.thor.shared.ptcl.component

import org.seekloud.thor.shared.ptcl.model.Point

import org.seekloud.thor.shared.ptcl.model

/**
  * Created by Jingyi on 2018/11/9
  */
case class FoodState(fId: Int, level:Byte, position: Point, radius: Float, color: Byte)

trait Food extends CircleObjectOfGame {

  val fId: Int

  var level: Byte

  var color: Byte

  override protected var position: Point

  override var radius: Float

  def getFoodState: FoodState = {
    FoodState(fId, level, position, radius, color)
  }

}

object Food {
  def apply(foodState: FoodState): Food =
    AddNormalFood(foodState.fId, foodState.level, foodState.position, foodState.radius, foodState.color)
}

case class AddNormalFood(
                        fId: Int,
                        var level: Byte,
                        var position: model.Point,
                        var radius: Float,
                        var color: Byte
                        ) extends Food