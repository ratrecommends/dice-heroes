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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.dice.Config;

/**
 * Created 16.10.13 by vlaaad
 */
public class ExpBar extends Widget {

    private final Drawable background = Config.skin.getDrawable("ui-exp-progress-background");
    private final Drawable bar = Config.skin.getDrawable("ui-exp-progress");

    private float progress;

    public ExpBar(float progress) {
        this.progress = progress;
    }

    public ExpBar() {
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        int x = (int) getX();
        int y = (int) getY();
        int w = (int) getWidth();
        int h = (int) getHeight();
        background.draw(batch, x, y, w, h);
        bar.draw(batch, x + 2, y + 2, (int) ((w - 4) * progress), h - 4);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        if (progress < 0)
            progress = 0;
        if (progress > 1)
            progress = 1;
        this.progress = progress;
    }

    @Override public float getPrefWidth() {
        return background.getMinWidth();
    }

    @Override public float getPrefHeight() {
        return background.getMinHeight();
    }
}
