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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 01.12.13 by vlaaad
 */
public class GameMessage {
    private final Stage stage;
    private final Table table = new Table();

    public GameMessage(String locKey, Stage stage) {
        this.stage = stage;
        LocLabel label = new LocLabel(locKey);
        label.setAlignment(Align.center);
        table.add(label).padTop(-1);
        table.setBackground(Config.skin.getDrawable("ui-message-background"));
        table.setSize(stage.getWidth(), label.getPrefHeight() + 4);
        table.setY(stage.getHeight() - table.getHeight());
    }

    public void show() {
        stage.addActor(table);
    }

    public void hide() {
        table.remove();
    }
}
