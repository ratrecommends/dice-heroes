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

package com.vlaaad.dice.editor.ui

import com.badlogic.gdx.scenes.scene2d.ui.{Button, Table}
import com.vlaaad.dice.game.world.view.Tile
import com.vlaaad.dice.Config
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.Color

/** Created 26.02.14 by vlaaad */
class ToolsPanel extends Table(Config.skin) {
  val backgroundColor = new Color(1, 1, 1, 0.5f)
  val tmp = new Color()

  setBackground("ui-window-background")
  defaults.pad(2)
  top()

  override def drawBackground(batch: Batch, parentAlpha: Float, x: Float, y: Float): Unit = {
    tmp.set(getColor)
    setColor(backgroundColor)
    super.drawBackground(batch, parentAlpha, x, y)
    setColor(tmp)
  }

  def add(imageName: String, text: String, f: () => Unit): Unit = {
    val button: Button = new Button(Config.skin, "passive")
    button.defaults.pad(2)
    button.left()
    button.add(new Tile(imageName)).size(12, 12)
    button.add(text)
    button.addListener(new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = f()
    })
    this.add(button).expandX().fillX().row()
  }
}
