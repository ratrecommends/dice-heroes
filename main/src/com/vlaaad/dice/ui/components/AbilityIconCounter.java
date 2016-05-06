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

package com.vlaaad.dice.ui.components;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;

/**
 * Created 28.10.13 by vlaaad
 */
public class AbilityIconCounter extends WidgetGroup {

    public final Image image;
    private final Label counter;
    private int count;

    public AbilityIconCounter(Ability ability, int value) {
        if (ability == null) {
            ability = Config.abilities.get("skip-turn");
        }
        image = new Image(Config.skin.getDrawable("ability/" + ability.name + "-icon"));
        if (ability.cost <0) image.setColor(AbilityIcon.unique);
        image.setScaling(Scaling.none);
        image.setAlign(Align.left | Align.top);
        image.moveBy(0, 1);

        counter = new Label("", Config.skin, "default", "inventory-counter");
        counter.setAlignment(Align.right | Align.bottom);
        setCount(value);

        addActor(image);
        addActor(counter);

        setSize(image.getPrefWidth(), image.getPrefHeight());
    }

    @Override public float getPrefWidth() { return image.getPrefWidth(); }

    @Override public float getPrefHeight() { return image.getPrefHeight(); }

    @Override public void layout() {
        image.setSize(getWidth(), getHeight());
        image.invalidate();
        image.validate();

        counter.setSize(getWidth(), getHeight());
        counter.invalidate();
        counter.validate();
    }

    public void setCount(int count) {
        this.count = count;
        if (count != 0) {
            counter.setText(String.valueOf(count));
        } else {
            counter.setText("");
        }
    }
}
