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

package org.seekloud.thor.shared.ptcl.thor.draw

import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

trait FpsRender {
  this:ThorSchemaClientImpl =>

  private var lastRenderTime = System.currentTimeMillis()
  var lastRenderTimes = 0
  private var renderTimes = 0

  def addFps(): Unit = {
    val time = System.currentTimeMillis()
    renderTimes += 1
    if (time - lastRenderTime > 1000){
      lastRenderTime = time
      lastRenderTimes = renderTimes
      renderTimes = 0
    }
  }


  def drawNetInfo(networkLatency:Long, drawTime: Long, frameTime: Long, peopleNum: Int): Unit = {
    addFps()
    ctx.setFont("Helvetica", baseFont * 15)
    ctx.setTextAlign("start")
    val fpsStr =  s"fps   : $lastRenderTimes"
    val pingStr = s"ping : ${networkLatency}ms"
    val drawStr = s"drawTime : ${drawTime}ms"
    val frameStr = s"frameTime : ${frameTime}ms"
    val peopleNumStr = s"总人数 : $peopleNum"
    val explainStr2 = s"左键挥刀"
    val explainStr1 = s"右键加速"
    ctx.setTextBaseLine("top")
    if (networkLatency > 100) ctx.setFill("#ff0000")
    else ctx.setFill("#ffffff")
    ctx.fillTextByMiddle(pingStr,window.x * 0.9,10)
    if (lastRenderTimes < 30) ctx.setFill("#ff0000")
    else ctx.setFill("#ffffff")
    ctx.fillTextByMiddle(fpsStr,window.x * 0.9,30)
    ctx.setFill("#ffffff")
    ctx.fillTextByMiddle(drawStr,window.x * 0.9,50)
    ctx.fillTextByMiddle(frameStr,window.x * 0.9,70)
    ctx.fillTextByMiddle(peopleNumStr,window.x * 0.9,100)
    ctx.setFont("Helvetica", baseFont * 19, "bold")
    ctx.setTextAlign("start")
    ctx.setFill("#ffffff")
    ctx.fillTextByMiddle(explainStr1,window.x * 0.9,130)
    ctx.fillTextByMiddle(explainStr2,window.x * 0.9,155)
  }
}
