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

package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * copied from tank
  * 合并框架中的ctx
  */

/**
  * 本文件为了统一JvavFx和Js，请注意以下:
  * color：设置rgb或者rgba或者16进制
  *
  **/
trait MiddleContext {

  def setGlobalAlpha(alpha: Double): Unit

  def fill(): Unit

  def setFill(color: String): Unit

  def setStrokeStyle(color: String): Unit

  def moveTo(x: Double, y: Double): Unit

  def drawImage(image: Any, offsetX: Double, offsetY: Double, size: Option[(Double, Double)] = None, imgOffsetX: Option[Double] = None, imgOffsetY: Option[Double] = None, imgSize: Option[(Double, Double)] = None): Unit

  def fillRec(x: Double, y: Double, w: Double, h: Double): Unit

  def clearRect(x: Double, y: Double, w: Double, h: Double): Unit

  def beginPath(): Unit

  def closePath(): Unit

  def lineTo(x1: Double, y1: Double): Unit

  def stroke(): Unit

  def fillText(text: String, x: Double, y: Double, z: Double = 500): Unit

  def fillTextByMiddle(text: String, x: Double, y: Double, z: Double = 500): Unit

  def setTextBaseLine(s: String):Unit

  def setFont(fontFamily: String, fontSize: Double, wid: String = "normal"): Unit

  def setShadowColor(s: String) :Unit

  def setTextAlign(s: String)

  def rect(x: Double, y: Double, w: Double, h: Double)

  def strokeText(text: String, x: Double, y: Double, maxWidth: Double): Unit

  def rotate(d: Float): Unit

  def translate(x: Float, y: Float): Unit

  def save(): Unit

  def restore(): Unit

  def arc(x: Double, y: Double, r: Double, sAngle: Double, eAngle: Double, counterclockwise: Boolean)

  def lineWidth(width: Double) : Unit

  def measureText(s: String) : Double

}
