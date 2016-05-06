
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

/** Created 15.02.15 by vlaaad */
object SkillTest extends App {

  case class Ability(name: String, level: Int, skill: Int)

  val abilities = Seq(
    Ability("default-attack", 1, 2),
    Ability("default-defence", 1, 2),
    Ability("cleave", 2, 3),
    Ability("defensive-attack", 2, 3),
    Ability("speed-attack", 3, 3),
    Ability("heavy-defence", 3, 3),
    Ability("heavy-attack", 4, 3),
    Ability("heavy-defensive-attack", 4, 3),
    Ability("heavy-cleave", 5, 3),
    Ability("heavy-speed-attack", 5, 3)
  )

  val skills = Map(
    1 -> 8,
    2 -> 12,
    3 -> 15,
    4 -> 17,
    5 -> 18,
    6 -> 19
  )

  1 to 2 foreach (level => {
    val available = abilities.filter(_.level <= level)
    val skill = skills(level)
    println(s"LEVEL $level")
//    val fitting = comb(6, Seq.fill(6)(available).flatten.toList).map(v => v.map(_.skill).sum -> v).filter(_._1 <= skill)
//    val fitting = (1 to 2).map(diceCount => {
//      comb(6, available.combinations(diceCount).toList).toSet.flatten.map(v => v.map(_.skill).sum -> v).filter(_._1 <= skill)
//    }).flatten
//    val maxSkill = fitting.maxBy(_._1)._1
//    fitting.filter(_._1 == maxSkill).foreach(println)
  })


  def comb[T](n: Int, l: List[T]): List[List[T]] =
    n match {
      case 0 => List(List())
      case _ => for (el <- l;
                     sl <- comb(n - 1, l dropWhile {
                       _ != el
                     }))
      yield el :: sl
    }

}
