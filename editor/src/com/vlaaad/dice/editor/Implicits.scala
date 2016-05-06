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

package com.vlaaad.dice.editor

/** Created 03.03.14 by vlaaad */

import scala.language.implicitConversions
import scala.collection.mutable.ArrayBuffer
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.Actor

object Implicits {
  implicit def gdxArray2List[T](v: com.badlogic.gdx.utils.Array[T]): List[T] = {
    val b = ArrayBuffer[T]()
    var i = 0
    val n = v.size
    while (i < n) {
      b += v.get(i)
      i += 1
    }
    b.toList
  }

  implicit def list2gdxArray[T](v: Iterable[T]): com.badlogic.gdx.utils.Array[T] = {
    val r = new com.badlogic.gdx.utils.Array[T](v.size)
    for (el <- v)
      r.add(el)
    r
  }

  implicit def fn2changeListener[A](f: () => A): ChangeListener = {
    new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = f()
    }
  }

  implicit def fn2runnable[R](f: () => R): Runnable = {
    new Runnable {
      def run(): Unit = f()
    }
  }
}
