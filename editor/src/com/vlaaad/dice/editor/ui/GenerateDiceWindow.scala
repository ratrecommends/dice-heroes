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
import com.vlaaad.dice.game.user.Die
import com.badlogic.gdx.scenes.scene2d.ui._
import com.vlaaad.dice.Config
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter.DigitsOnlyFilter
import com.vlaaad.dice.game.world.players.util.PlayerHelper
import scala.languageFeature.implicitConversions
import scala.collection.JavaConversions._
import com.vlaaad.dice.game.config.professions.ProfessionDescription
import com.vlaaad.dice.game.config.abilities.Ability
import com.badlogic.gdx.utils.{Timer, ObjectIntMap}
import com.badlogic.gdx.scenes.scene2d.Touchable
import scala.List
import com.vlaaad.dice.editor.Implicits._
import com.badlogic.gdx.utils.Timer.Task
import com.vlaaad.dice.game.objects.Creature
import com.vlaaad.dice.game.world.controllers.ViewController
import com.vlaaad.dice.ui.components.AbilityIcon
import com.badlogic.gdx.Gdx


/** Created 20.03.14 by vlaaad */
class GenerateDiceWindow extends GameWindow[List[Die] => Unit] {

  val professionCounters = collection.mutable.Map[ProfessionDescription, TextField]()

  val abilitiesCheckBoxes = collection.mutable.Map[ProfessionDescription, collection.mutable.Map[Ability, CheckBox]]()

  var selection: List[Die] = _
  var callback: (List[Die] => Unit) = _

  var doAdd: Boolean = false

  protected override def initialize(): Unit = {
    val menu = new Table(Config.skin)
    menu.defaults().pad(2)

    val targetValue = new TextField("200", Config.skin)
    targetValue.setTextFieldFilter(new DigitsOnlyFilter)

    //target
    val target = new Table(Config.skin)
    target.add("Target: ")
    target.add(targetValue).width(30)

    menu.add(target).row()

    //professions
    for (p <- Config.professions) {
      val profTable = new Table(Config.skin)
      profTable.left().defaults().pad(2)
      val counter = new TextField("1", Config.skin)
      counter.setTextFieldFilter(new DigitsOnlyFilter)
      professionCounters += p -> counter

      profTable.add(p.name)
      profTable.add(counter).width(30)

      menu.add(profTable).left().row()

      val die = new Die(p, "asd", p.getExpForLevel(6), new com.badlogic.gdx.utils.Array[Ability](), new ObjectIntMap[Ability]())

      val checkBoxMap = abilitiesCheckBoxes.getOrElseUpdate(p, collection.mutable.Map())

      for (a <- Config.abilities.byType(Ability.Type.wearable).filter(_.requirement.canBeSatisfied(die))) {
        val abilityTable = new Table(Config.skin)
        abilityTable.left().defaults().padRight(2)
        val cb = new CheckBox("", Config.skin)
        cb.setChecked(true)
        checkBoxMap += a -> cb
        abilityTable.add(cb).padTop(3).padLeft(2)
        abilityTable.add(s" - ${a.name}")

        menu.add(abilityTable).left().pad(-2).row()
      }
    }
    val right = new Table()
    right.defaults.pad(2)
    val results = new Table(Config.skin)
    results.defaults.pad(2)

    val buttons = new Table(Config.skin)
    buttons.defaults().pad(2)
    right.add(results).row()
    right.add(buttons).row()

    val start = new TextButton("Start search", Config.skin)
    buttons.add(start)
    var stopSearch = false
    val stop = new TextButton("Stop search", Config.skin)
    buttons.add(stop)
    stop.setDisabled(true)
    stop.addListener(() => {
      stopSearch = true
      Timer.instance().scheduleTask(new Task {
        def run(): Unit = {
          stop setDisabled true
          start setDisabled false
        }
      }, 0.5f)
    })
    val add = new TextButton("Add", Config.skin)
    add.setDisabled(true)
    add.addListener(() => {
      doAdd = true;
      hide()
    })
    buttons.add(add)

    start.addListener(() => {
      add.setDisabled(true)
      stopSearch = false
      stop.setDisabled(false)
      start.setDisabled(true)
      val abilities: Map[ProfessionDescription, List[Ability]] = Config.professions.map(p => {
        p -> (for ((k, v) <- abilitiesCheckBoxes(p) if v.isChecked) yield k).toList
      }).toMap

      val diceCount: Map[ProfessionDescription, Int] = professionCounters.map(e => (e._1, e._2.getText.toInt)).toMap
      val thread = new Thread(() => {
        var delta = Int.MaxValue
        val target = targetValue.getText.toInt
        while (delta > 0 && !stopSearch) {
          val dice = diceCount.map(p => {
            (1 to p._2).map(_ => (p._1, new Die(p._1, "None", p._1.getExpForLevel(6), (1 to 6).map(_ => abilities(p._1).random), new ObjectIntMap[Ability]())))
          }).reduce((a, b) => a ++ b)
          val res = dice.map(_._2.abilities().map(_.cost).sum).sum
          val dt = math.abs(target - res)
          if (dt < delta) {
            delta = dt
            Gdx.app.postRunnable(() => {
              results.clearChildren()
              results.add(s"result: $res, needed: $target, delta: $dt").row()
              selection = dice.map(_._2).toList
              for (d <- dice) {
                val die = d._2
                val c = new Creature(die, PlayerHelper.defaultAntagonist)
                val dieTable = new Table()

                dieTable.defaults.pad(2)
                dieTable.add(ViewController.createView(c))
                for (a <- d._2.abilities().sortBy(_.cost).reverse) {
                  dieTable.add(new AbilityIcon(a))
                }
                results.add(dieTable).row()
              }
              add.setDisabled(false)
            })
          }
        }
        if (stopSearch) println("stopped!")
        stop.setDisabled(true)
        start.setDisabled(false)
      })
      thread.start()
    })

    val content = new Table(Config.skin)
    content.setBackground("ui-creature-info-background")
    content.setTouchable(Touchable.enabled)

    val rightPane = new ScrollPane(right)
    rightPane.setOverscroll(false, true)
    rightPane.setScrollingDisabled(true, false)
    content.add(new ScrollPane(menu))
    content.add(rightPane).width(200)
    table.add(content)
  }

  protected def doShow(callback: (List[Die]) => Unit): Unit = {
    this.callback = callback
  }

  protected override def onHide(): Unit = {
    if (selection != null && doAdd) {
      callback(selection)
      doAdd = false
    }
  }
}
