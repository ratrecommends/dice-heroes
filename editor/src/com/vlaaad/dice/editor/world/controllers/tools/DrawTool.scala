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

package com.vlaaad.dice.editor.world.controllers.tools

import com.vlaaad.dice.editor.world.{Position, EditorWorld}
import com.vlaaad.dice.editor.events.imp.ClickEvent
import com.vlaaad.dice.game.config.levels.LevelElementType
import com.vlaaad.dice.editor.events.Event

/** Created 27.02.14 by vlaaad */
class DrawTool[T](let: LevelElementType[T], id: T) extends Tool {

  var world: EditorWorld = _

  def start(world: EditorWorld): Unit = {
    this.world = world
    world.addListener(listener)
  }

  def end(world: EditorWorld): Unit = {
    world.removeListener(listener)
  }

  val listener: Event => Boolean = {
    case c: ClickEvent =>
      world.executor.add {
        val p = Position(let, c.x, c.y)
        var removed: Option[T] = None
        (
          () => removed = world.add(p, id),
          () => {
            world.remove(p)
            removed match {
              case Some(v) =>
                world.add(p, v)
              case _ =>
            }
          }
          )
      }
      true
    case _ =>
      false
  }
}
