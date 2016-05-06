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

import com.badlogic.gdx.math.Interpolation

/** Created 06.04.14 by vlaaad */
object RewardGen extends App {
  final val min = 32
  final val max = 130
  final val replayRate = 0.2f
  var total = 0

  for (level <- 1 to 25) {
    val progress = level / 25.0
    val reward = Interpolation.linear.apply(min, max, progress.toFloat).toInt
    total += reward
    val drop = reward * replayRate
    val pass = reward - drop
    println(s"level: ${10 + level}, pass: ${pass.toInt}, drop: ${drop.toInt}")
  }
  println(s"total user reward: $total")
}
