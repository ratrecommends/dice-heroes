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

package com.vlaaad.dice.editor.world

import com.vlaaad.dice.editor.events.EventDispatcher
import com.vlaaad.dice.editor.ui.ToolsPanel
import com.badlogic.gdx.scenes.scene2d.{Group, Stage}
import com.vlaaad.dice.editor.events.imp.{AddWorldObjectEvent, RemoveWorldObjectEvent}
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.vlaaad.dice.editor.world.commands.CommandExecutor
import com.vlaaad.dice.game.world.players.{FractionRelation, Fraction}
import scala.collection.mutable.ArrayBuffer
import com.vlaaad.dice.game.user.Die
import com.vlaaad.dice.editor.DiceEditor
import com.vlaaad.common.gdx.State


/** Created 26.02.14 by vlaaad */
class EditorWorld(val state:State, val editor:DiceEditor, val viewRoot: Group, val toolTable: Table, val toolsPanel: ToolsPanel, val stage: Stage, controllers: List[Controller]) extends EventDispatcher {

  val dice = ArrayBuffer[Die]()

  val executor = new CommandExecutor

  val objects = collection.mutable.Map[Position[_], Any]()

  val fractions = collection.mutable.Set[Fraction]()
  var fractionRelations: Option[Map[(Fraction, Fraction), FractionRelation]] = None

  def !!! = throw new IllegalStateException()

  def init() = {
    controllers.foreach(_.init(this))
  }

  def add[T](p: Position[T], o: T): Option[T] = {
    objects.get(p) match {
      case Some(v) =>
        remove(p, v.asInstanceOf[T])
        add(p, o)
        Some(v.asInstanceOf[T])
      case None =>
        objects += p -> o
        dispatch(new AddWorldObjectEvent(p, o))
        None
    }
  }

  def remove[T](pos: Position[T]):T = {
    objects.remove(pos) match {
      case Some(v) =>
        dispatch(new RemoveWorldObjectEvent(pos, v.asInstanceOf[T]))
        v.asInstanceOf[T]
      case None => !!!
    }
  }

  def removeIfExists[T](pos: Position[T]):Option[T] = {
    objects.remove(pos) match {
      case Some(v) =>
        dispatch(new RemoveWorldObjectEvent(pos, v.asInstanceOf[T]))
        Option(v.asInstanceOf[T])
      case None =>
        None
    }
  }

  def remove[T](pos: Position[T], o: T):T= {
    objects.remove(pos) match {
      case Some(v) =>
        assert(v == o, s"removed $v is no $o")
        dispatch(new RemoveWorldObjectEvent(pos, o))
        o
      case None => !!!
    }
  }

}
