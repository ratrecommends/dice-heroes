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

package com.vlaaad.dice.editor.world.commands

import scala.collection.mutable.ArrayBuffer

/** Created 28.02.14 by vlaaad */
class CommandExecutor {
  val commands = ArrayBuffer[(() => Unit, () => Unit)]()
  var idx = -1

  def add(cmd: (() => Unit, () => Unit)) = {
    val targetIndex = idx + 1
    commands.insert(targetIndex, cmd)
    if (targetIndex + 1 < commands.size)
      commands.remove(targetIndex + 1, commands.size - targetIndex - 1)
    idx = targetIndex
    cmd._1()
  }

  def undo(): Unit = {
    if (idx == -1)
      return
    commands(idx)._2()
    idx -= 1
  }

  def redo(): Unit = {
    val targetIndex = idx + 1
    if (targetIndex >= commands.size)
      return
    commands(targetIndex)._1()
    idx += 1
  }
}
