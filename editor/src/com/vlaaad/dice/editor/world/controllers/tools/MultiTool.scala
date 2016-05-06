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

package com.vlaaad.dice.editor.world.controllers.tools

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.{ActorGestureListener, DragListener}
import com.badlogic.gdx.scenes.scene2d.{Actor, InputEvent}
import com.badlogic.gdx.{Gdx, Input}
import com.vlaaad.dice.editor.world.{EditorWorld, Position}
import com.vlaaad.dice.game.config.levels.LevelElementType
import com.vlaaad.dice.game.objects.{StepDetector, Creature, Obstacle}
import com.vlaaad.dice.game.user.Die
import com.vlaaad.dice.game.world.controllers.ViewController
import com.vlaaad.dice.game.world.players.Fraction
import com.vlaaad.dice.game.world.players.util.PlayerHelper
import com.vlaaad.dice.game.world.view.Tile

/** Created 03.07.14 by vlaaad */
class MultiTool extends Tool {
  val tmpVec = new Vector2()
  var world: EditorWorld = _


  val dragListener = new DragListener() {
    init()

    def init() = {
      setTapSquareSize(1)
    }

    var at: Option[(Position[_ >: Die with Obstacle with Fraction with StepDetector with String <: Object], Any)] = None

    var view: Option[Actor] = None

    override def dragStart(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
      at = getAt(event)
      if (at.isDefined) {
        event.getStage.cancelTouchFocusExcept(this, event.getListenerActor)
        view = Option(at.get match {
          case (Position(t: LevelElementType[_], x: Int, y: Int), die: Die) if t == LevelElementType.enemy =>
            ViewController.createView(new Creature(die, PlayerHelper.defaultAntagonist))
          case (Position(t: LevelElementType[_], x: Int, y: Int), obstacle: Obstacle) if t == LevelElementType.obstacle =>
            ViewController.createView(obstacle)
          case (Position(t: LevelElementType[_], x: Int, y: Int), detector: StepDetector) if t == LevelElementType.stepDetector =>
            ViewController.createView(detector)
          case (Position(t: LevelElementType[_], x: Int, y: Int), cover: String) if t == LevelElementType.cover =>
            new Tile("cover/" + cover + (if ((x + y) % 2 == 1) "-odd" else "-even"))
          case (Position(t: LevelElementType[_], x: Int, y: Int), tile: String) if t == LevelElementType.tile =>
            new Tile("tile/" + tile + (if ((x + y) % 2 == 1) "-odd" else "-even"))
          case (Position(t: LevelElementType[_], x: Int, y: Int), fraction:Fraction) if t == LevelElementType.spawn =>
            val r = new Tile("selection/spawn")
            r.setColor(Color.valueOf(fraction.name.hashCode.toString))
            r.getColor.a = 0.7f
            r
        })
        view.foreach(v => world.viewRoot.addActor(v))
        drag(event, 0, 0, 0)
      }
    }

    override def drag(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
      view.foreach(v => {
        val pos = v.getParent.stageToLocalCoordinates(tmpVec.set(event.getStageX, event.getStageY))
        v.setPosition(pos.x - v.getWidth / 2, pos.y - v.getHeight / 2)
      })

    }

    override def dragStop(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
      view.foreach(_.remove())
      view = None
      at.foreach(v => {
        val p = world.viewRoot.stageToLocalCoordinates(tmpVec.set(event.getStageX, event.getStageY))
        val x = (p.x / ViewController.CELL_SIZE).toInt
        val y = (p.y / ViewController.CELL_SIZE).toInt
        if (x != v._1.x || y != v._1.y) {
          var removedAtTarget: Option[Any] = None
          val target = v._1.copy(x = x, y = y).asInstanceOf[Position[Any]]
          val source = v._1.asInstanceOf[Position[Any]]
          world.executor.add(
            (
              () => {
                //move
                world.remove(source, v._2)
                removedAtTarget = world.removeIfExists(target)
                world.add(target, v._2)
              },
              () => {
                //move back
                world.remove(target, v._2)
                world.add(source, v._2)
                removedAtTarget.foreach(v => world.add(target, v))
              }
              )
          )
        }
      })
    }
  }

  val deleteListener = new ActorGestureListener() {
    override def tap(event: InputEvent, x: Float, y: Float, count: Int, button: Int): Unit = {
      if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
        val at = getAt(event)
        at.foreach(v => {
          val point = v._1.asInstanceOf[Position[Any]]
          var removed: Option[Any] = None
          world.executor.add(
            (
              () => removed = world.removeIfExists(point),
              () => removed.foreach(v => world.add(point, v))
              )
          )
        })
      }
    }
  }

  override def start(world: EditorWorld): Unit = {
    this.world = world
    world.toolTable.clearChildren()
    world.stage.addCaptureListener(dragListener)
    world.stage.addCaptureListener(deleteListener)
  }

  override def end(world: EditorWorld): Unit = {
    world.stage.removeCaptureListener(dragListener)
    world.stage.removeCaptureListener(deleteListener)
  }

  def getAt(event: InputEvent) = {
    val p = world.viewRoot.stageToLocalCoordinates(tmpVec.set(event.getStageX, event.getStageY))
    val x = (p.x / ViewController.CELL_SIZE).toInt
    val y = (p.y / ViewController.CELL_SIZE).toInt
    world.objects.get(Position(LevelElementType.enemy, x, y)).map(v => Position(LevelElementType.enemy, x, y) -> v) orElse
      world.objects.get(Position(LevelElementType.obstacle, x, y)).map(v => Position(LevelElementType.obstacle, x, y) -> v) orElse
      world.objects.get(Position(LevelElementType.spawn, x, y)).map(v => Position(LevelElementType.spawn, x, y) -> v) orElse
      world.objects.get(Position(LevelElementType.stepDetector, x, y)).map(v => Position(LevelElementType.stepDetector, x, y) -> v) orElse
      world.objects.get(Position(LevelElementType.cover, x, y)).map(v => Position(LevelElementType.cover, x, y) -> v) orElse
      world.objects.get(Position(LevelElementType.tile, x, y)).map(v => Position(LevelElementType.tile, x, y) -> v)

  }
}
