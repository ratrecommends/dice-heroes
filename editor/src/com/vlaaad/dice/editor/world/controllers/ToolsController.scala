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

package com.vlaaad.dice.editor.world.controllers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.vlaaad.dice.Config
import com.vlaaad.dice.editor.Implicits._
import com.vlaaad.dice.editor.world.controllers.tools._
import com.vlaaad.dice.editor.world.{Controller, EditorWorld}
import com.vlaaad.dice.game.config.levels.LevelElementType
import com.vlaaad.dice.game.objects.{StepDetector, Obstacle}
import com.vlaaad.dice.game.requirements.DieRequirement
import org.yaml.snakeyaml.{DumperOptions, Yaml}

import scala.collection.convert.WrapAsJava
import scala.language.implicitConversions

/** Created 27.02.14 by vlaaad */
class ToolsController extends Controller {

  private var world: EditorWorld = _
  private var _tool: Option[Tool] = None

  lazy val tilesTools: List[(String, () => Unit)] = Config.assetManager.get("gfx.atlas", classOf[TextureAtlas])
    .getRegions.filter(_.name.startsWith("tile/")).filterNot(_.name.endsWith("-overhang"))
    .filterNot(_.name.endsWith("-overhang-left")).filterNot(_.name.endsWith("-overhang-right"))
    .filterNot(_.name.endsWith("-overhang-none")).filterNot(_.name.endsWith("-overhang-both"))
    .filterNot(_.name.endsWith("-outline")).filterNot(_.name.endsWith("-corner-bottom-left"))
    .filterNot(_.name.endsWith("-corner-bottom-right")).filterNot(_.name.endsWith("-corner-top-left"))
    .filterNot(_.name.endsWith("-corner-top-right")).filterNot(_.name.endsWith("-even"))
    .map(v => v.name.substring(5, v.name.length - 4)).map(v => (v, () => tool = Some(new DrawTool[String](LevelElementType.tile, v))))

  lazy val obstacleTools: List[(String, () => Unit)] = Config.assetManager.get("gfx.atlas", classOf[TextureAtlas])
    .getRegions.filter(_.name.startsWith("obstacle/")).map(v => v.name.substring(9))
    .map(v => (v, () => tool = Some(new DrawTool[Obstacle](LevelElementType.obstacle, new Obstacle(v)))))

  lazy val coverTools: List[(String, () => Unit)] = Config.assetManager.get("gfx.atlas", classOf[TextureAtlas])
    .getRegions.filter(_.name.startsWith("cover/")).filterNot(_.name.endsWith("-even"))
    .map(v => v.name.substring(6, v.name.length - 4))
    .map(v => (v, () => tool = Some(new DrawTool[String](LevelElementType.cover, v))))

  val stepDetectorPattern = "step-detector/(.+)-on".r

  lazy val stepDetectorTools: List[(String, () => Unit)] = Config.assetManager.get("gfx.atlas", classOf[TextureAtlas])
    .getRegions.map(_.name).collect {
    case stepDetectorPattern(name) =>
      (name, () => tool = Some(new DrawTool[StepDetector](LevelElementType.stepDetector, new StepDetector(name, DieRequirement.ANY))))
  }

  def init(world: EditorWorld): Unit = {
    this.world = world
    world.toolsPanel.add("selection/turn-confirmation", "Edit", () => tool = Some(new MultiTool()))
    world.toolsPanel.add("tile/castle-bricks-odd", "Tiles", () => draw(tilesTools))
    world.toolsPanel.add("obstacle/fountain", "Obstacles", () => draw(obstacleTools))
    world.toolsPanel.add("cover/carpet-odd", "Covers", () => draw(coverTools))
    world.toolsPanel.add("selection/spawn", "Spawn", () => tool = Some(new SpawnTool(this)))
    world.toolsPanel.add("step-detector/warrior-on", "Step detectors", () => draw(stepDetectorTools))
    world.toolsPanel.add("profession/archer-enemy", "Enemies", () => tool = Some(new EnemyTool()))
    world.toolsPanel.add("ability/heavy-defence-icon", "Save", () => {
      val ops = new DumperOptions
      ops.setWidth(120)
      val dump: String = new Yaml(ops).dump(save())
      println(dump)
      Gdx.app.getClipboard.setContents(dump)
    })
  }

  def onBackPressed(): Boolean = {
    if (tool.isDefined && tool.get.isInstanceOf[MultiTool]) {
      false
    } else {
      tool = Some(new MultiTool())
      true
    }
  }

  def draw(tools: List[(String, () => Unit)]) = {
    world.toolTable.clearChildren()
    val selector = new SelectBox[String](Config.skin)
    selector.setItems(tools.map(_._1))
    selector.addListener(new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = tools(selector.getSelectedIndex)._2()
    })
    tools(0)._2()
    world.toolTable.add(selector)
  }

  def tool = _tool

  def tool_=(v: Option[Tool]) = {
    //    world.toolTable.clearChildren()
    _tool match {
      case Some(t) => t.end(world)
      case None =>
    }
    _tool = v
    _tool match {
      case Some(t) => t.start(world)
      case None =>
    }
  }

  def save(): java.util.Map[String, Any] = {
    val min = new GridPoint2(Int.MaxValue, Int.MaxValue)
    val max = new GridPoint2(Int.MinValue, Int.MinValue)
    world.objects.keys.foreach(p => {
      min.x = Math.min(min.x, p.x)
      min.y = Math.min(min.y, p.y)
      max.x = Math.max(max.x, p.x)
      max.y = Math.max(max.y, p.y)
    })
    val shortCuts = (for (c <- 'A' to 'z' if c.isLetter) yield c).iterator
    val cellToShortCut = collection.mutable.Map[Map[LevelElementType[_], Any], String]()
    val arr = Array.ofDim[String](max.y - min.y + 1, max.x - min.x + 1)
    for (x <- min.x to max.x; y <- min.y to max.y) {
      val cell: Map[LevelElementType[_], Any] = world.objects.filter(e => e._1.x == x && e._1.y == y).map(e => e._1.elementType -> encode(e._1.elementType, e._2)).toMap
      val shortCut = cellToShortCut.getOrElseUpdate(cell, shortCuts.next().toString)
      arr(y - min.y)(x - min.x) = shortCut
    }

    def encode[T](t: LevelElementType[T], any: Any) = t.decoder.encode(any.asInstanceOf[T])

    val shorts = WrapAsJava.mapAsJavaMap(cellToShortCut.map(v => {
      v._2 -> WrapAsJava.mapAsJavaMap(v._1.map(a => a._1.toString -> a._2))
    }).toMap)

    val map = WrapAsJava.mutableSeqAsJavaList(arr.reverseMap(v => WrapAsJava.mutableSeqAsJavaList(v)))

    val r = collection.mutable.Map[String, AnyRef]("shortcuts" -> shorts, "map" -> map)
    world.fractionRelations.foreach(v => {
      val fractions = WrapAsJava.seqAsJavaList(world.fractions.map(_.name).toSeq)
      val relations = WrapAsJava.mapAsJavaMap(v.map(a => s"${a._1._1.name}:${a._1._2.name}" -> a._2.toString))
      r += "fractions" -> fractions
      r += "relations" -> relations
    })

    WrapAsJava.mapAsJavaMap(r)
  }
}
