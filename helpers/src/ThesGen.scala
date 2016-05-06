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

import com.badlogic.gdx.backends.lwjgl.LwjglFiles
import org.yaml.snakeyaml.Yaml

import scala.collection.mutable.ArrayBuffer

/** Created 05.04.14 by vlaaad */
object ThesGen extends App {
  val files = new LwjglFiles()
  val levels = new Yaml().loadAll(files.internal("android/assets/levels.yml").read())

  import scala.collection.convert.wrapAsScala._

  val toGenerate = Set(
    "d-11", "d-12", "d-13", "d-14", "d-15"
  )

  val names = new ArrayBuffer[String]()
  //  JMapWrapper
  levels.collect {
    case j: java.util.Map[String, Object] if j.containsKey("name") && toGenerate.contains(j.get("name").toString) => j
  }.flatMap(_.get("shortcuts").asInstanceOf[java.util.Map[String, java.util.Map[String, Object]]].values()).
    map(_.get("enemy").asInstanceOf[java.util.Map[String, String]]).
    filter(_ != null).map(_.get("name")).toSet.toList.sorted.foreach(name => {
    println("---")
    println(s"{key: $name, en: ${name.toUpperCase}, ru: ${name.toUpperCase}}")
  })
}
