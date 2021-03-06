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

package org.seekloud.thor.front.pages

import org.seekloud.thor.front.common.{Page, Routes}
import org.seekloud.thor.front.thorClient.{GameHolder, GameHolder4Play, GameHolder4Test}
import org.seekloud.thor.front.utils.{Http, JsFunc, Shortcut}
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.ThorGameInfo
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html.{Canvas, Input}
import mhtml._
import org.seekloud.thor.front.Main
import org.seekloud.thor.shared.ptcl.TestPswRsp

import scala.util.Random
import scala.xml.Elem
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import io.circe.syntax._
import org.seekloud.thor.shared.ptcl.model.Constants._
/**
  * Created by Jingyi on 2018/11/9
  */
object TestRender extends Page{

  private val random = new Random(System.currentTimeMillis())
  private val gameInfo = ThorGameInfo(name = Main.guestName(random.nextInt(Main.guestName.length)))
  private val canvas = <canvas id ="GameView" tabindex="1"> </canvas>

  def init() = {

    val gameHolder = new GameHolder4Test("GameView")
    gameHolder.start(gameInfo.name, gameInfo.pId, gameInfo.userAccessCode, gameInfo.rId)
  }

  val showGame = Var(0)
  val show = showGame.map{
    case 0 =>
      <div>
        <div class="entry">
          <div class="title">
            <h1>TEST</h1>
          </div>
          <div class="text">
            <input type="password" class="form-control" id="psw" placeholder="password"></input>
          </div>
          <div class="button">
            <button type="button" class="btn" onclick={()=>joinGame()}>join</button>
          </div>
        </div>
      </div>

    case 1 =>
      canvas
  }

  def joinGame(): Unit = {
    val psw = dom.document.getElementById("psw").asInstanceOf[Input].value
    Http.getAndParse[TestPswRsp](Routes.getPsw).map{
      rsp =>
        if(rsp.errCode == 0){
          if(rsp.psw == psw) {
            showGame := 1
            init()
          }
          else JsFunc.alert("密码错误！")
        }
        else JsFunc.alert(s"报错啦$rsp")
    }
  }

  override def render: Elem =
    <div>
      {show}
    </div>
  



}
