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

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle
import com.badlogic.gdx.scenes.scene2d.ui.{ScrollPane, Table}
import com.badlogic.gdx.scenes.scene2d.{Group, Stage}
import com.vlaaad.common.gdx.State
import com.vlaaad.dice.Config
import com.vlaaad.dice.editor.DiceEditor
import com.vlaaad.dice.editor.ui.ToolsPanel
import com.vlaaad.dice.editor.world.EditorWorld
import com.vlaaad.dice.editor.world.controllers._

/** Created 26.02.14 by vlaaad */
class CreateMapState(editor: DiceEditor, cb: () => Any) extends State {

  val toolsPanel = new ToolsPanel
  val toolTable = new Table(Config.skin)
  val root = new Group
  val tools = new ToolsController()

  val world = new EditorWorld(this, editor, root, toolTable, toolsPanel, stage, List(
    new EditorViewController(),
    tools
  ))

  protected def init(): Unit = {
    val table = new Table
    stage.addActor(root)
    stage.addActor(table)
    table.left()
    table.setFillParent(true)
    table.add(new ScrollPane(toolsPanel, new ScrollPaneStyle())).left().expandY().fillY().width(100)
    table.add(toolTable).expand().right().top()

    world.init()
  }

  protected def resume(): Unit = {

  }

  protected def pause(): Unit = {

  }

  protected def dispose(): Unit = {

  }


  override protected def onBackPressed(): Unit = {
    if (!tools.onBackPressed()) cb()
  }

  override protected def resume(isStateChange: Boolean): Unit = ()

  override protected def dispose(isStateChange: Boolean, stage: Stage): Unit = ()

  override protected def pause(isStateChange: Boolean, stage: Stage): Unit = ()
}
