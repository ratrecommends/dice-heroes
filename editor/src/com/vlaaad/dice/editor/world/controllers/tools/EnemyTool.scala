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

import com.vlaaad.dice.editor.world.{Position, EditorWorld}
import com.vlaaad.dice.game.user.Die
import com.vlaaad.dice.game.world.controllers.ViewController
import com.vlaaad.dice.game.objects.Creature
import com.vlaaad.dice.game.world.players.util.PlayerHelper

//import com.vlaaad.dice.game.objects.Creature.Fraction
import com.vlaaad.dice.Config
import com.badlogic.gdx.scenes.scene2d.ui.{ScrollPane, TextButton, Table}
import com.badlogic.gdx.scenes.scene2d.utils.{DragListener, ChangeListener}
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Actor}
import com.vlaaad.dice.editor.ui.{GenerateDiceWindow, EditDieWindow}
import com.vlaaad.dice.game.world.view.WorldObjectView
import com.vlaaad.dice.game.config.levels.LevelElementType
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.vlaaad.common.util.Logger
import com.vlaaad.dice.editor.events.imp.UpdateDieView
import com.vlaaad.dice.editor.Implicits._

/** Created 28.02.14 by vlaaad */
class EnemyTool extends Tool {

  val dice = collection.mutable.Map[Die, Table]()
  val views = collection.mutable.Map[Die, WorldObjectView]()

  val diceList = new Table()

  def start(world: EditorWorld): Unit = {
    world.toolTable.clearChildren()
    val create = new TextButton("Create new enemy", Config.skin)
    create.addListener(new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = createDie(world)
    })
    world.toolTable.add(create).row()

    val generate = new TextButton("Generate Enemies", Config.skin)
    generate.addListener(() => generateDice(world))
    world.toolTable.add(generate).row()
    val pane = new ScrollPane(diceList)
    pane.setCancelTouchFocus(false)
    world.toolTable.add(pane).expandY().fill().row()

    for (die <- world.dice) {
      add(world, die)
    }
  }

  def end(world: EditorWorld): Unit = {

  }

  def add(world: EditorWorld, die: Die) = {
    val table = new Table(Config.skin)
    table.defaults().pad(2)

    val view = ViewController.createView(new Creature(die, PlayerHelper.defaultAntagonist))

    val edit = new TextButton("Edit", Config.skin)
    edit.addListener(new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = editDie(world, die)
    })

    val remove = new TextButton("-", Config.skin)
    remove.addListener(new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = removeDie(world, die)
    })

    table.add(view)
    table.add(edit)
    table.add(remove)
    dice += die -> table
    views += die -> view

//    world.toolTable.add(table).padTop(10).row()
    diceList.add(table).padTop(10).row()

    view.addListener(new DragListener() {
      var view: WorldObjectView = _


      def eventIsOk(event: InputEvent): Boolean = {
        true
        //        event.getTarget == world.stage.getRoot || event.getTarget.isDescendantOf(world.viewRoot)
      }

      override def dragStart(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
        view = ViewController.createView(new Creature(die, PlayerHelper.defaultAntagonist))
        world.stage.addActor(view)
        view.setPosition(event.getStageX - ViewController.CELL_SIZE / 2, event.getStageY - ViewController.CELL_SIZE / 2)
      }

      override def drag(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
        view.setPosition(event.getStageX - ViewController.CELL_SIZE / 2, event.getStageY - ViewController.CELL_SIZE / 2)
      }

      override def dragStop(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
        view.remove()
        if (eventIsOk(event)) {
//          Logger.debug("event is ok!")
          val p = world.viewRoot.stageToLocalCoordinates(new Vector2(event.getStageX, event.getStageY)).scl(1 / ViewController.CELL_SIZE)
          world.executor.add {
            val pos = Position(LevelElementType.enemy, MathUtils.floor(p.x), MathUtils.floor(p.y))
            var removed: Option[Die] = None
            (
              () => removed = world.add(pos, die),
              () => {
                world.remove(pos)
                removed match {
                  case Some(v) =>
                    world.add(pos, v)
                  case _ =>
                }
              }
              )
          }

        }
      }
    })
  }

  def editDie(world: EditorWorld, die: Die): Unit = {
    new EditDieWindow().show((world, die, die => {
      ViewController.updateView(new Creature(die, PlayerHelper.defaultAntagonist), views(die))
      world.dispatch(UpdateDieView(die))
    }))
  }

  def removeDie(world: EditorWorld, die: Die): Unit = {
    world.dice -= die
    diceList.getCell(dice(die)).pad(0)
    diceList.removeActor(dice(die))
  }

  def generateDice(world: EditorWorld): Unit = {
    new GenerateDiceWindow().show(dice => {
      dice.foreach(die => {
        world.dice += die
        add(world, die)
      })
    })
  }

  def createDie(world: EditorWorld): Unit = new EditDieWindow().show((world, new Die(), die => {
    world.dice += die
    add(world, die)
  }))
}
