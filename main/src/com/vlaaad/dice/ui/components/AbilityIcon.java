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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.common.GdxHelper;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;

/**
 * Created 16.10.13 by vlaaad
 */
public class AbilityIcon extends Widget {

    public static final Color unique = Color.valueOf("facf5e");

    private final Drawable drawable;
    public final Ability ability;

    public AbilityIcon(Ability ability) {
        this.ability = ability;
        if (ability == null) {
            ability = Config.abilities.get("skip-turn");
        }
        drawable = Config.skin.getDrawable("ability/" + ability.name + "-icon");
        setSize(drawable.getMinWidth(), drawable.getMinHeight());
        if (ability.cost < 0) {
            setColor(unique);
        }
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        GdxHelper.setBatchColor(batch, getColor(), parentAlpha);
        drawable.draw(
            batch,
            getX(),
            getY(),
            getWidth(),
            getHeight()
        );
    }

    @Override public float getPrefWidth() {
        return drawable.getMinWidth();
    }

    @Override public float getPrefHeight() {
        return drawable.getMinHeight();
    }
}
