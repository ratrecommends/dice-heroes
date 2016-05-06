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

package com.vlaaad.dice.game.tutorial.tasks;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.common.tutorial.tasks.ArrowForceClick;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.states.GameMapState;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 11.11.13 by vlaaad
 */
public class ArrowForceClickInventory extends ArrowForceClick {
    private GameMapState mapState;

    @Override protected void init() {
        mapState = resources.get("map");
    }

    @Override protected Actor getTargetActor() {
        return mapState.diceWindowButton;
    }

    @Override protected Drawable getArrowDrawable() {
        return Config.skin.getDrawable("tutorial-arrow-up");
    }

    @Override protected ArrowDirection getDirection() {
        return ArrowDirection.bottom;
    }

    @Override protected Table getMessageTable() {
        Table table = new Table(Config.skin);
        table.align(Align.top);
        Label label = new LocLabel("tutorial-open-dice-window");
        label.setWrap(true);
        label.setAlignment(Align.center);
        table.add(label);
        return table;
    }
}
