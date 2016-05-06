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

import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.ui.{SelectBox, TextButton, TextField}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, InputListener}
import com.vlaaad.dice.Config
import com.vlaaad.dice.editor.Implicits._
import com.vlaaad.dice.editor.ui.EditRelationsWindow
import com.vlaaad.dice.editor.world.EditorWorld
import com.vlaaad.dice.editor.world.controllers.ToolsController
import com.vlaaad.dice.game.config.levels.LevelElementType
import com.vlaaad.dice.game.world.players.util.PlayerHelper
import com.vlaaad.dice.game.world.players.{Fraction, FractionRelation}

import scala.language.implicitConversions

/** Created 12.07.14 by vlaaad */
class SpawnTool(val tools: ToolsController) extends Tool {


  override def start(world: EditorWorld): Unit = {
    world.toolTable.clearChildren()
    val selector = new SelectBox[Fraction](Config.skin)
    selector.addListener(() => tools.tool = Some(new DrawTool[Fraction](LevelElementType.spawn, selector.getSelected)))
    val remover = new SelectBox[Fraction](Config.skin)
    var isRefreshing = false
    def refresh() = {
      isRefreshing = true
      selector.setItems(world.fractions)
      remover.setItems(world.fractions)
      isRefreshing = false
    }
    remover.addListener(() => {
      if (!isRefreshing) {
        Option(remover.getSelected).foreach(fraction => {
          world.fractions.remove(fraction)
          world.fractionRelations = world.fractionRelations.map(v => v.filter(a => a._1._1 != fraction && a._1._2 != fraction))
        })
        refresh()
      }
    })
    remover.addListener(() => Option(remover.getSelected).foreach(world.fractions.remove))

    val addDefaults = new TextButton("add default fractions", Config.skin)
    addDefaults.addListener(() => {
      world.fractions.add(PlayerHelper.protagonist)
      world.fractions.add(PlayerHelper.antagonist)
      world.fractionRelations = Some(Map((PlayerHelper.protagonist, PlayerHelper.antagonist) -> FractionRelation.enemy))
      refresh()
    })

    val editRelations = new TextButton("edit fraction relations", Config.skin)
    editRelations.addListener(() => {
      new EditRelationsWindow().show((world.fractions.toSet, v => {
        world.fractionRelations = Option(v)
      }))
    })

    val addFraction = new TextField("", Config.skin)
    addFraction.addListener(new InputListener {
      override def keyUp(event: InputEvent, keycode: Int): Boolean = {
        if (keycode == Input.Keys.ENTER) {
          world.fractions.add(Fraction.valueOf(addFraction.getText))
          refresh()
          addFraction.setText("")
        }
        true
      }
    })
    addFraction.setMessageText("add fraction...")



    world.toolTable.add(addDefaults).width(100).pad(2).row()
    world.toolTable.add(editRelations).width(100).pad(2).row()
    world.toolTable.add(addFraction).width(100).pad(2).row()

    world.toolTable.add("create spawn for fraction:").row()
    world.toolTable.add(selector).row()
    world.toolTable.add("select fraction to delete:").row()
    world.toolTable.add(remover).row()

    refresh()

  }

  override def end(world: EditorWorld): Unit = ()
}
