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


/** Created 26.02.14 by vlaaad */
trait Event {
  var target: EventDispatcher = null
  var listener: EventDispatcher = null
  var capture: Boolean = false
  var bubbles: Boolean = true
  var handled: Boolean = false
  var stopped: Boolean = false
  var cancelled: Boolean = false

  /** Marks this event as handled. This does not affect event propagation inside scene2d, but causes the {@link Stage}
    * event methods to return false, which will eat the event so it is not passed on to the application under the stage. */
  def handle() = handled = true

  /** Marks this event cancelled. This {@link #handle() handles} the event and {@link #stop() stops} the event
    * propagation. It also cancels any default action that would have been taken by the code that fired the event. Eg, if the
    * event is for a checkbox being checked, cancelling the event could uncheck the checkbox. */
  def cancel() = {
    cancelled = true
    stopped = true
    handled = true
  }

  def stop() = stopped = true

  def reset() = {
    target = null
    listener = null
    capture = false
    bubbles = true
    handled = false
    stopped = false
    cancelled = false
  }
}
