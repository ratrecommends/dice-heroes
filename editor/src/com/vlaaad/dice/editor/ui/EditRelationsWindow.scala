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

import com.badlogic.gdx.scenes.scene2d.ui.{SelectBox, Table, TextButton}
import com.vlaaad.common.ui.GameWindow
import com.vlaaad.dice.Config
import com.vlaaad.dice.editor.Implicits._
import com.vlaaad.dice.game.world.players.{Fraction, FractionRelation}

/**
 * Created 12.07.14 by vlaaad
 */
class EditRelationsWindow extends GameWindow[(Set[Fraction], (Map[(Fraction, Fraction), FractionRelation]) => Unit)] {

  var callback: (Map[(Fraction, Fraction), FractionRelation]) => Unit = _
  var result: Option[Map[(Fraction, Fraction), FractionRelation]] = None

  override protected def doShow(i: (Set[Fraction], (Map[(Fraction, Fraction), FractionRelation]) => Unit)): Unit = {
    callback = i._2
    val combos = i._1.toList.combinations(2).map(v => v(0) -> v(1))
    val content = new Table(Config.skin)
    content.setBackground("ui-store-window-background")
    val res = collection.mutable.Map[(Fraction, Fraction), FractionRelation]()
    for (v <- combos) {
      content.add(v.toString())
      val selector = new SelectBox[FractionRelation](Config.skin)
      selector.setItems(FractionRelation.values().toList)
      selector.addListener(createListener(selector, v, res))
      res += v -> selector.getSelected
      content.add(selector).row()
    }

    val ok = new TextButton("Ok", Config.skin)
    ok.addListener(() => {
      result = Option(res.toMap)
      hide()
    })

    content.add(ok).colspan(2).minWidth(50)
    table.add(content)
  }


  override protected def onHide(): Unit = {
    result.foreach(callback)
  }

  def createListener(selector: SelectBox[FractionRelation],
                     combo: (Fraction, Fraction),
                     target: collection.mutable.Map[(Fraction, Fraction), FractionRelation]
                      ): () => Unit = () => {
    target += combo -> selector.getSelected
  }
}