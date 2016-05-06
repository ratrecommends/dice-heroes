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

package com.vlaaad.dice.game.tutorial.ui.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.dice.Config;

/**
 * Created 07.12.13 by vlaaad
 */
public class TutorialUpArrow extends Actor {
    private final Drawable arrow = Config.skin.getDrawable("tutorial-up-arrow");
    private final Drawable dot = Config.skin.getDrawable("tutorial-up-arrow-dot");

    private float stateTime;

    public TutorialUpArrow() {
    }

    @Override public void act(float delta) {
        stateTime += delta;
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        float top = getY() + getHeight();
        float center = getX() + getWidth() / 2;

        batch.setColor(1, 1, 1, parentAlpha * getAlphaForY(top - arrow.getMinHeight() + arrow.getMinHeight() / 2));
        arrow.draw(batch, center - arrow.getMinWidth(), top - arrow.getMinHeight(), arrow.getMinWidth(), arrow.getMinHeight());
        for (float y = top - arrow.getMinHeight() - dot.getMinHeight(); y >= getY(); y -= dot.getMinHeight()) {
            batch.setColor(1, 1, 1, parentAlpha * getAlphaForY(y));
            dot.draw(batch, center - dot.getMinWidth(), y, dot.getMinWidth(), dot.getMinHeight());
        }
    }

    private float getAlphaForY(float y) {
        return Math.max(MathUtils.sinDeg(-stateTime * 180 + y * 5), 0);
    }


}
