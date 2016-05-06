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

package com.vlaaad.dice.editor.ui

import com.vlaaad.common.ui.GameWindow
import com.vlaaad.dice.editor.world.EditorWorld
import com.vlaaad.dice.game.user.{UserData, Die}
import com.badlogic.gdx.scenes.scene2d.ui.{TextButton, SelectBox, Table, TextField}
import com.vlaaad.dice.Config
import com.vlaaad.dice.ui.components.{DieInventory, DieNet}
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener
import collection.JavaConversions._
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter.DigitsOnlyFilter
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.Actor
import com.vlaaad.dice.editor.Implicits._
import com.vlaaad.dice.game.config.abilities.Ability.Type

/** Created 28.02.14 by vlaaad */
class EditDieWindow extends GameWindow[(EditorWorld, Die, Die => Unit)] {

  protected def doShow(i: (EditorWorld, Die, (Die) => Unit)): Unit = {
    val (world, die, callback) = i
    if (die.profession == null) die.profession = Config.professions.get("warrior")
    if (die.name == null) die.name = "name"


    val nameTable = new Table(Config.skin)

    val name = new TextField(if (die.name == null) "name" else die.name, Config.skin)
    val ru = new TextField("", Config.skin)
    val en = new TextField("", Config.skin)

    name.setMaxLength(4)
    ru.setMaxLength(4)
    en.setMaxLength(4)

    name.setTextFieldListener(new TextFieldListener {
      def keyTyped(textField: TextField, key: Char): Unit = {
        die.name = textField.getText
        val data = Config.thesaurus.getData(name.getText)
        if (data != null) {
          if (data.ru != null) ru.setText(data.ru)
          if (data.en != null) en.setText(data.en)
        }
      }
    })

    nameTable.defaults().pad(2)
    nameTable.add("Name:")
    nameTable.add(name).width(50)
    nameTable.add("ru:")
    nameTable.add(ru).width(50)
    nameTable.add("en:")
    nameTable.add(en).width(50)

    val professionTable = new Table(Config.skin)

    val professions = for (p <- Config.professions) yield p
    val names = professions.map(_.name).toIndexedSeq
    val professionSelector = new SelectBox[String](Config.skin)
    professionSelector.setItems(names.toList)

    val level = new TextField(die.getCurrentLevel.toString, Config.skin)
    level.setTextFieldFilter(new DigitsOnlyFilter)

    professionTable.defaults().pad(2)
    professionTable.add("Profession:")
    professionTable.add(professionSelector)
    professionTable.add("Level:")
    professionTable.add(level).width(50)

    val ok = new TextButton("ok", Config.skin)
    ok.addListener(new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = {
        die.inventory.clear()
        callback(die)
        hide()
      }
    })

    val netInv = new Table()
    val content = new Table(Config.skin)
    content.setBackground("ui-creature-info-background")
    content.defaults().pad(2)

    content.add(nameTable).row()
    content.add(professionTable).row()
    content.add(netInv).row()
    content.add(ok).width(40).row()

    def recreateNetInv(removeEquipped: Boolean) = {
      netInv.clearChildren()
      die.inventory.clear()
      if (removeEquipped) {
        die.abilities.clear()
      }
      for (ability <- Config.abilities.byType(Type.wearable)) {
        if (ability.requirement.canBeSatisfied(die)) {
          die.inventory.put(ability, 12)
        }
      }
      val userData = new UserData
      val net = new DieNet(die)
      val inv = new DieInventory(die, userData)
      net.setInventory(inv)
      inv.setNet(net)
      netInv.add(net).row()
      netInv.add(inv).row()
    }

    professionSelector.addListener(new ChangeListener {
      def changed(event: ChangeEvent, actor: Actor): Unit = {
        die.profession = Config.professions.get(professionSelector.getSelected)
        die.exp = die.profession.getExpForLevel(level.getText.toInt)
        recreateNetInv(removeEquipped = true)
      }
    })

    level.setTextFieldListener(new TextFieldListener {
      def keyTyped(textField: TextField, key: Char): Unit = {
        val levelValue = try {
          level.getText.toInt
        } catch {
          case _: NumberFormatException => 1
        }
        die.exp = die.profession.getExpForLevel(levelValue)
        recreateNetInv(removeEquipped = false)
      }
    })

    recreateNetInv(removeEquipped = false)

    table.add(content)
  }

  protected override def canBeClosed: Boolean = false

  override def handleBackPressed(): Boolean = true
}
