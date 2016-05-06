/*
 * Dice heroes is a turn based rpg-strategy game where characters are dice.
 * Copyright (C) 2016 Vladislav Protsenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vlaaad.dice.editor.states

import com.vlaaad.common.gdx.State
import com.badlogic.gdx.scenes.scene2d.ui.{Value, TextButton, Table}
import com.vlaaad.dice.Config
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.{Stage, EventListener, Actor}

/** Created 26.02.14 by vlaaad */
class MenuState(actions: List[(String, () => Any)]) extends State {


  def createListener(f: () => Any): EventListener = new ChangeListener {
    def changed(event: ChangeEvent, actor: Actor): Unit = f()
  }


  protected def init(): Unit = {
    val table = new Table(Config.skin)
    table.setFillParent(true)
    table.defaults.pad(2)
    stage.addActor(table)
    for ((k, v) <- actions) {
      val b = new TextButton(k, Config.skin)
      b.addListener(createListener(v))
      table.add(b).prefWidth(Value.percentWidth(0.25f)).minWidth(100).row()
    }
  }

  override protected def resume(isStateChange: Boolean): Unit = ()

  override protected def dispose(isStateChange: Boolean, stage: Stage): Unit = ()

  override protected def pause(isStateChange: Boolean, stage: Stage): Unit = ()
}
