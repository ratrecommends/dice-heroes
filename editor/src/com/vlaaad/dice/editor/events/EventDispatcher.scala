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

package com.vlaaad.dice.editor.events

import com.badlogic.gdx.utils.{Pools, DelayedRemovalArray}

/** Created 26.02.14 by vlaaad */
trait EventDispatcher {
  def parent: Option[EventDispatcher] = None

  private final val listeners = new DelayedRemovalArray[Event => Boolean](0)
  private final val captureListeners = new DelayedRemovalArray[Event => Boolean](0)

  def addListener(listener: Event => Boolean): Boolean = {
    if (!listeners.contains(listener, true)) {
      listeners.add(listener)
      true
    } else {
      false
    }
  }

  def removeListener(listener: Event => Boolean): Boolean = listeners.removeValue(listener, true)


  def getListeners = listeners

  def addCaptureListener(listener: Event => Boolean): Boolean = {
    if (!captureListeners.contains(listener, true)) {
      captureListeners.add(listener)
      true
    } else {
      false
    }
  }

  def removeCaptureListener(listener: Event => Boolean): Boolean = captureListeners.removeValue(listener, true)

  def getCaptureListeners = captureListeners


  def dispatch(event: Event): Boolean = {
    event.target = this
    val ancestors = Pools.obtain(classOf[com.badlogic.gdx.utils.Array[_]]).asInstanceOf[com.badlogic.gdx.utils.Array[EventDispatcher]]
    var p = parent
    while (p != null) {
      p match {
        case Some(v) =>
          ancestors.add(v)
          p = v.parent
        case None =>
          p = null
      }
    }
    try {

      var i: Int = ancestors.size - 1
      while (i >= 0) {
        val currentTarget = ancestors.get(i)
        currentTarget.notify(event, capture = true)
        if (event.stopped)
          return event.cancelled
        i -= 1
      }

      notify(event, capture = true)

      if (event.stopped)
        return event.cancelled

      notify(event, capture = false)

      if (!event.bubbles || event.stopped)
        return event.cancelled

      i = 0
      val n: Int = ancestors.size
      while (i < n) {
        ancestors.get(i).notify(event, capture = false)
        if (event.stopped) return event.cancelled
        i += 1
      }

      event.cancelled
    } finally {
      ancestors.clear()
      Pools.free(ancestors)
    }
  }

  private def notify(event: Event, capture: Boolean): Boolean = {
    if (event.target == null)
      throw new IllegalArgumentException("The event target cannot be null.")
    val listeners = if (capture) captureListeners else this.listeners
    if (listeners.size == 0)
      return event.cancelled
    event.listener = this
    event.capture = capture
    listeners.begin()
    var i: Int = 0
    val n: Int = listeners.size
    while (i < n) {
      if (listeners.get(i)(event)) {
        event.handle()
      }
      i += 1
    }
    listeners.end()
    event.cancelled
  }
}
