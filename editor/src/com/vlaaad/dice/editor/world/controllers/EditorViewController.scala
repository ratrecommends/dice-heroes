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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener
import com.badlogic.gdx.scenes.scene2d.{Actor, Group, InputEvent, InputListener}
import com.badlogic.gdx.{Gdx, Input}
import com.vlaaad.common.util.MathHelper
import com.vlaaad.dice.Config
import com.vlaaad.dice.editor.events.imp.{AddWorldObjectEvent, ClickEvent, RemoveWorldObjectEvent, UpdateDieView}
import com.vlaaad.dice.editor.world.{Controller, EditorWorld, Position}
import com.vlaaad.dice.game.config.levels.LevelElementType
import com.vlaaad.dice.game.world.players.Fraction
import com.vlaaad.dice.game.world.players.util.PlayerHelper

//import com.vlaaad.dice.game.objects.Creature.Fraction
import com.vlaaad.dice.game.objects.{StepDetector, Creature, Obstacle}
import com.vlaaad.dice.game.user.Die
import com.vlaaad.dice.game.world.controllers.ViewController
import com.vlaaad.dice.game.world.view.{Tile, WorldObjectView}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/** Created 26.02.14 by vlaaad */
class EditorViewController extends Controller {
  val views = collection.mutable.Map[Position[_], Actor]()
  val tileBackgrounds = collection.mutable.Map[Position[_], mutable.ArrayBuffer[Actor]]()

  var world: EditorWorld = _

  val outlinesLayer = new Group
  val tileLayer = new Group
  val stepDetectorLayer = new Group
  val overTileLayer = new Group
  val coverLayer = new Group
  val spawnLayer = new Group
  val objectLayer = new Group

  def init(world: EditorWorld): Unit = {
    this.world = world
    world.addListener {
      case v: RemoveWorldObjectEvent[_] =>
        removeView(v.position)
        true
      case v: AddWorldObjectEvent[_] =>
        addView(v.position, v.obj)
        true
      case v: UpdateDieView =>
        world.objects.filter(p => p._2 == v.die).foreach(o => ViewController.updateView(new Creature(v.die, PlayerHelper.defaultAntagonist), views(o._1).asInstanceOf[WorldObjectView]))
        true
      case _ => false
    }

    world.viewRoot.addActor(outlinesLayer)
    world.viewRoot.addActor(tileLayer)
    world.viewRoot.addActor(overTileLayer)
    world.viewRoot.addActor(coverLayer)
    world.viewRoot.addActor(stepDetectorLayer)
    world.viewRoot.addActor(new Actor {
      val from = new Vector2()
      val to = new Vector2()
      val lineColor = new Color(0.3f, 0.3f, 0.3f, 0.5f)

      override def draw(batch: Batch, parentAlpha: Float): Unit = {
        batch.end()
        Config.shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        Config.shapeRenderer.setColor(lineColor)
        Config.shapeRenderer.setProjectionMatrix(getStage.getCamera.combined)
        stageToLocalCoordinates(from.set(0, 0))
        stageToLocalCoordinates(to.set(getStage.getWidth, getStage.getHeight))
        from.x = MathHelper.snapToLowest(from.x, ViewController.CELL_SIZE)
        from.y = MathHelper.snapToLowest(from.y, ViewController.CELL_SIZE)
        to.x = MathHelper.snapToHighest(to.x, ViewController.CELL_SIZE)
        to.y = MathHelper.snapToHighest(to.y, ViewController.CELL_SIZE)
        for (x <- from.x to to.x by ViewController.CELL_SIZE) {
          Config.shapeRenderer.line(world.viewRoot.getX + x, world.viewRoot.getY + from.y, world.viewRoot.getX + x, world.viewRoot.getY + to.y)
        }
        for (y <- from.y to to.y by ViewController.CELL_SIZE) {
          Config.shapeRenderer.line(world.viewRoot.getX + from.x, world.viewRoot.getY + y, world.viewRoot.getX + to.x, world.viewRoot.getY + y)
        }
        Config.shapeRenderer.end()
        batch.begin()
      }
    })
    world.viewRoot.addActor(spawnLayer)
    world.viewRoot.addActor(objectLayer)

    world.stage.addListener(new InputListener {
      override def keyDown(event: InputEvent, keycode: Int): Boolean = keycode match {
        case Input.Keys.Z =>
          if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
              world.executor.redo()
            } else {
              world.executor.undo()
            }
          }
          true
        case _ => false
      }
    })
    world.stage.addListener(new ActorGestureListener() {

      def eventIsOk(event: InputEvent): Boolean = {
        event.getTarget == world.stage.getRoot || event.getTarget.isDescendantOf(world.viewRoot)
      }

      override def pan(event: InputEvent, x: Float, y: Float, deltaX: Float, deltaY: Float): Unit = {
        if (eventIsOk(event))
          world.viewRoot.moveBy(deltaX, deltaY)
      }

      override def tap(event: InputEvent, x: Float, y: Float, count: Int, button: Int): Unit = {
        if (eventIsOk(event)) {
          val p = world.viewRoot.stageToLocalCoordinates(new Vector2(event.getStageX, event.getStageY)).scl(1 / ViewController.CELL_SIZE)
          world.dispatch(ClickEvent(MathUtils.floor(p.x), MathUtils.floor(p.y)))
        }
      }
    })
  }

  def createView[T](position: Position[T], value: T): Actor = {
    position.elementType match {
      case LevelElementType.tile =>
        val suffix = if (((position.x + position.y) % 2) == 1) "odd" else "even"
        new Tile(s"tile/$value-$suffix")
      case LevelElementType.obstacle =>
        new Tile(s"obstacle/${value.asInstanceOf[Obstacle].worldObjectName}")
      case LevelElementType.stepDetector =>
        new Tile(s"step-detector/${value.asInstanceOf[StepDetector].worldObjectName}-off")
      case LevelElementType.cover =>
        val suffix = if (((position.x + position.y) % 2) == 1) "odd" else "even"
        new Tile(s"cover/$value-$suffix")
      case LevelElementType.spawn =>
        val tile = new Tile("selection/spawn")
        val f = value.asInstanceOf[Fraction]
        var s = f.name.hashCode.toString
        while (s.length < 6) {
          s = s + "f"
        }
        tile.setColor(Color.valueOf(s.substring(0, 6)))
        tile.getColor.a = 0.7f
        tile
      case LevelElementType.enemy =>
        ViewController.createView(new Creature(value.asInstanceOf[Die], PlayerHelper.defaultAntagonist))
    }
  }

  def addView[T](position: Position[T], value: T) = {
    val view = createView(position, value)
    views += position -> view
    view.setPosition(position.x * ViewController.CELL_SIZE, position.y * ViewController.CELL_SIZE)

    position.elementType match {
      case LevelElementType.tile =>
        tileLayer.addActor(view)
        recreateOutLines(position)
      case LevelElementType.stepDetector =>
        stepDetectorLayer.addActor(view)
      case LevelElementType.obstacle =>
        objectLayer.addActor(view)
      case LevelElementType.enemy =>
        objectLayer.addActor(view)
      case LevelElementType.cover =>
        coverLayer.addActor(view)
      case LevelElementType.spawn =>
        spawnLayer.addActor(view)
    }
  }

  def removeView[T](position: Position[T]) = {
    views.remove(position).get.remove()
    position.elementType match {
      case LevelElementType.tile =>
        recreateOutLines(position)
      case _ =>
    }
  }

  def clearTileBackgrounds(position: Position[_]) = {
    tileBackgrounds.remove(position) match {
      case Some(v) => v.filter(_ != null).foreach(_.remove())
      case None =>
    }
  }

  def showTileBackground(set: mutable.Set[Position[_]], x: Int, y: Int): Unit = {
    val c = Position(LevelElementType.tile, x, y)
    clearTileBackgrounds(c)
    val actors = ArrayBuffer[Actor]()
    tileBackgrounds.put(c, actors)
    val current = world.objects.get(c)
    val top = world.objects.get(c.copy(x = x, y = y + 1))
    val bottom = world.objects.get(c.copy(x = x, y = y - 1))
    val left = world.objects.get(c.copy(x = x - 1, y = y))
    val right = world.objects.get(c.copy(x = x + 1, y = y))
    if (current.isDefined) {
      if (top.isDefined && current.get != top.get) {
        if (top.get == left.orNull) {
          actors += ViewController.addTopLeftCornerTransition(top.get.asInstanceOf[String], x, y, overTileLayer)
        }
        if (top.get == right.orNull) {
          actors += ViewController.addTopRightCornerTransition(top.get.asInstanceOf[String], x, y, overTileLayer)
        }
      }
      if (bottom.isDefined && current.get != bottom.get) {
        if (bottom.get == left.orNull) {
          actors += ViewController.addBottomLeftCornerTransition(bottom.get.asInstanceOf[String], x, y, overTileLayer)
        }
        if (bottom.get == right.orNull) {
          actors += ViewController.addBottomRightCornerTransition(bottom.get.asInstanceOf[String], x, y, overTileLayer)
        }
      }
    }
    if (world.objects.isDefinedAt(c) || !set.add(c))
      return

    val topLeft = world.objects.get(c.copy(x = x - 1, y = y + 1))
    val topRight = world.objects.get(c.copy(x = x + 1, y = y + 1))
    val bottomLeft = world.objects.get(c.copy(x = x - 1, y = y - 1))
    val bottomRight = world.objects.get(c.copy(x = x + 1, y = y - 1))

    if (topLeft.nonEmpty && top.isEmpty && left.isEmpty) {
      actors += ViewController.addBottomRightCorner(x, y, topLeft.get.asInstanceOf[String], outlinesLayer)
    }

    if (topRight.nonEmpty && top.isEmpty && right.isEmpty) {
      actors += ViewController.addBottomLeftCorner(x, y, topRight.get.asInstanceOf[String], outlinesLayer)
    }

    if (top.nonEmpty) {
      val hasLeft = world.objects.get(c.copy(x = x - 1, y = y + 1)).nonEmpty
      val hasRight = world.objects.get(c.copy(x = x + 1, y = y + 1)).nonEmpty
      val overhandType = if (hasLeft && hasRight) "both" else if (hasLeft) "left" else if (hasRight) "right" else "none"
      actors += ViewController.addOverHang(x, y, top.get.asInstanceOf[String], overhandType, outlinesLayer)
      actors += ViewController.addTopOutline(x, y, top.get.asInstanceOf[String], outlinesLayer)
    }

    if (bottom.nonEmpty) {
      actors += ViewController.addBottomOutline(x, y, bottom.get.asInstanceOf[String], outlinesLayer)
    }
    if (left.nonEmpty) {
      actors += ViewController.addLeftOutline(x, y, left.get.asInstanceOf[String], outlinesLayer)
    }
    if (right.nonEmpty) {
      actors += ViewController.addRightOutline(x, y, right.get.asInstanceOf[String], outlinesLayer)
    }
    if (bottomLeft.nonEmpty && bottom.isEmpty && left.isEmpty) {
      actors += ViewController.addTopRightCorner(x, y, bottomLeft.get.asInstanceOf[String], outlinesLayer)
    }
    if (bottomRight.nonEmpty && bottom.isEmpty && right.isEmpty) {
      actors += ViewController.addTopLeftCorner(x, y, bottomRight.get.asInstanceOf[String], outlinesLayer)
    }
  }

  def recreateOutLines(position: Position[_]) = {
    val processed = collection.mutable.Set[Position[_]]()
    showTileBackground(processed, position.x - 1, position.y - 1)
    showTileBackground(processed, position.x - 1, position.y)
    showTileBackground(processed, position.x - 1, position.y + 1)
    showTileBackground(processed, position.x, position.y - 1)
    showTileBackground(processed, position.x, position.y)
    showTileBackground(processed, position.x, position.y + 1)
    showTileBackground(processed, position.x + 1, position.y - 1)
    showTileBackground(processed, position.x + 1, position.y)
    showTileBackground(processed, position.x + 1, position.y + 1)
  }
}
