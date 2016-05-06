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

package com.vlaaad.dice.editor

import java.awt.Toolkit
import java.util.Comparator

import com.badlogic.gdx.{Input, Gdx}
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.backends.lwjgl.{LwjglApplicationConfiguration, LwjglApplication}
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.{InputEvent, InputListener, Actor}
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle
import com.badlogic.gdx.scenes.scene2d.ui.{SelectBox, Table, Window}
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.vlaaad.common.gdx.{State, App}
import com.vlaaad.common.gdx.App.AppListener
import com.vlaaad.dice.Config
import com.vlaaad.dice.editor.states.{CreateMapState, MenuState}
import com.vlaaad.dice.editor.world.Position
import com.vlaaad.dice.game.config.levels.{LevelDescription, LevelElementType}
import com.vlaaad.dice.states.LoadGameResourcesState
import com.vlaaad.dice.states.LoadGameResourcesState.Callback
import com.vlaaad.dice.util.DicePreferences


/** Created 26.02.14 by vlaaad */
class DiceEditor extends App(2f) {

  def create(): Unit = {
    Config.shapeRenderer = new ShapeRenderer
    Config.assetManager = new AssetManager
    Config.preferences = new DicePreferences(Gdx.app.getPreferences("com.vlaaad.dice.preferences"), this)
    addListener(new AppListener {
      override def enterState(state: State): Unit = {
        state.stage.addListener(new InputListener() {
          var prevWidth = 900
          var prevHeight = 600

          override def keyDown(event: InputEvent, keycode: Int): Boolean = {
            if (keycode == Input.Keys.F11) {
              if (Gdx.graphics.isFullscreen) {
                Gdx.graphics.setDisplayMode(prevWidth, prevHeight, false)
              } else {
                prevWidth = Gdx.graphics.getWidth
                prevHeight = Gdx.graphics.getHeight
                val size = Toolkit.getDefaultToolkit.getScreenSize
                Gdx.graphics.setDisplayMode(size.width, size.height, true)
              }
              true
            } else {
              super.keyDown(event, keycode)
            }
          }
        })
      }

      override def exitState(state: State): Unit = ()
    })
    setState(new LoadGameResourcesState(new Callback {

      def onResourcesLoaded(): Unit = {
        openMainMenu()
      }
    }))
  }

  def openMainMenu(): Unit = {
    setState(new MenuState(List(
      "Create new map" -> (() => setState(new CreateMapState(this, () => openMainMenu()))),
      "Load map" -> (() => {
        val style = new WindowStyle(
          Config.skin.getFont("default"),
          Color.WHITE,
          Config.skin.newDrawable("ui-window-background", new Color(1, 1, 1, 0.3f))
        )
        val window = new Window("", style)
        val content = new Table(Config.skin)
        val sb = new SelectBox[LevelDescription](Config.skin)
        val items = new com.badlogic.gdx.utils.Array[LevelDescription](Config.levels.byType(classOf[LevelDescription]))
        items.sort(new Comparator[LevelDescription] {
          override def compare(o1: LevelDescription, o2: LevelDescription): Int = o1.name.compareTo(o2.name)
        })
        sb.setItems(items)
        window.setModal(true)
        content.add(sb)
        content.setBackground("ui-creature-info-background")
        window.add(content)
        window.setFillParent(true)
        getState.stage.addActor(window)
        sb.addListener(new ChangeListener {
          override def changed(event: ChangeEvent, actor: Actor): Unit = {
            window.remove()
            val state = new CreateMapState(DiceEditor.this, () => openMainMenu())
            val level = sb.getSelected
            setState(state)
            import collection.convert.wrapAsScala._
            for (i <- level.getElements(LevelElementType.cover)) {
              state.world.add(Position(LevelElementType.cover, i.getKey.x(), i.getKey.y()), i.getValue)
            }
            for (i <- level.getElements(LevelElementType.tile)) {
              state.world.add(Position(LevelElementType.tile, i.getKey.x(), i.getKey.y()), i.getValue)
            }
            for (i <- level.getElements(LevelElementType.spawn)) {
              state.world.add(Position(LevelElementType.spawn, i.getKey.x(), i.getKey.y()), i.getValue)
            }
            for (i <- level.getElements(LevelElementType.obstacle)) {
              state.world.add(Position(LevelElementType.obstacle, i.getKey.x(), i.getKey.y()), i.getValue)
            }
            for (i <- level.getElements(LevelElementType.stepDetector)) {
              state.world.add(Position(LevelElementType.stepDetector, i.getKey.x(), i.getKey.y()), i.getValue)
            }
            for (i <- level.getElements(LevelElementType.enemy)) {
              state.world.dice += i.getValue
              state.world.add(Position(LevelElementType.enemy, i.getKey.x(), i.getKey.y()), i.getValue)
            }
          }
        })
      }),
      "Show level stats" -> (() => {
        import collection.convert.wrapAsScala._

        Config.levels.toList.sortBy(_.name).foreach {
          case level: LevelDescription =>
            println(s"level: ${level.name}")
            val totalValue = level.getElements(LevelElementType.enemy).map(_.getValue.abilities().map(_.cost).sum).sum
            println(s"total value = $totalValue")
            level.getElements(LevelElementType.spawn).size() match {
              case 0 =>
                println("this level has no spawns")
              case v =>
                val perPlayerDie = totalValue / v
                println(s"per player's die = $perPlayerDie")
            }
            println("---")
          case _ =>
        }
      }),
      "Exit" -> (() => Gdx.app.exit())
    )))
  }

  override def dispose(): Unit = {
    super.dispose()
    Config.assetManager.dispose()
    Config.shapeRenderer.dispose()
  }
}

object DiceEditor {
  def main(args: Array[String]): Unit = {
    val config = new LwjglApplicationConfiguration
    config.title = "Dice Heroes editor"
    config.width = 900
    config.height = 600
    new LwjglApplication(new DiceEditor, config)
  }
}
