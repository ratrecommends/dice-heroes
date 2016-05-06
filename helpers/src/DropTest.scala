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

import com.badlogic.gdx.utils.{ObjectIntMap, Array}
import com.vlaaad.dice.game.config.items.drop.{Roll, Drop, RangeItemCount, Range}
import com.vlaaad.dice.game.config.items.Item

/** Created 06.03.14 by vlaaad */
object DropTest extends App {

  val coin = new Item("coin", 1, Item.Type.resource)
  val stick = new Item("stick", 1, Item.Type.resource)

  val dropped = new Array[RangeItemCount]()
  dropped.add(new RangeItemCount(null, coin, 1))
  dropped.add(new RangeItemCount(new Range(1, 25), stick, 1))

  val rolls = new Array[Roll](1)
  rolls.add(new Roll(new Range(1, 100), dropped))

  val drop = new Drop(rolls)
  val total = new ObjectIntMap[Item]()
  for (_ <- 1 to 1000000) {
    val rolled = drop.roll()
    val it = rolled.keys().iterator()
    while (it.hasNext()) {
      val item = it.next()
      total.getAndIncrement(item, 0, rolled.get(item, 0))
    }
  }
  println(total)
}
