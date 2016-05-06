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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.ui.windows.StoreWindow;

/**
 * Created 29.10.13 by vlaaad
 */
public class StoreIcon extends Widget {

    private final Drawable drawable;
    private final Ability ability;
    private final Die die;
    private Drawable highlight;

    public StoreIcon(final Ability ability, final Die die, final UserData userData, final Callback callback) {
        if (!ability.requirement.canBeSatisfied(die))
            throw new IllegalStateException("requirement can't be satisfied, why do you even need store icon?");
        this.ability = ability;
        this.die = die;
        drawable = Config.skin.getDrawable("ability/" + ability.name + "-icon");
        if (ability.cost < 0)
            setColor(AbilityIcon.unique);
        setSize(drawable.getMinWidth(), drawable.getMinHeight());
        if (StoreWindow.canBeBought(ability, die, userData))
            highlight = Config.skin.getDrawable("ui/dice-window/store-selection-available");

        if (!ability.requirement.isSatisfied(die) || die.getTotalCount(ability) >= 6) {
            getColor().a = 0.3f;
        }
        addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                new StoreWindow().show(new StoreWindow.Params(ability, die, userData, new StoreWindow.Callback() {
                    @Override public void onBuy() {
                        callback.onBuy();
                    }
                }));
            }
        });
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        drawable.draw(
            batch,
            (int) (getX() + getWidth() / 2 - drawable.getMinWidth() / 2),
            (int) (getY() + getHeight() / 2 - drawable.getMinHeight() / 2),
            drawable.getMinWidth(),
            drawable.getMinHeight()
        );
        if (highlight != null) {
            highlight.draw(
                batch,
                getX() + getWidth() / 2 - highlight.getMinWidth() / 2,
                getY(),
                highlight.getMinWidth(),
                highlight.getMinHeight()
            );
        }
    }

    @Override public float getPrefWidth() { return drawable.getMinWidth(); }

    @Override public float getPrefHeight() { return drawable.getMinHeight(); }

    public static interface Callback {
        public void onBuy();
    }
}
