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

package com.vlaaad.dice.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.GdxHelper;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;

/**
 * Created 26.07.14 by vlaaad
 */
public class BlockingWindow extends GameWindow<IFuture<?>> implements IFutureListener<Object> {

    private static final Array<Color> colors = Array.with(
        Color.valueOf("a87ba7"), Color.valueOf("eb686c"), Color.valueOf("cce4a2"), Color.valueOf("62799c"), Color.valueOf("d7ece5")
    );

    private static final float disappearTime = 0.8f;
    private static final float colorTime = disappearTime /2f/** (float) colors.size * (float) colors.size*/;
    private static final float total = 8f;
    private static final float size = 6;


    @Override protected void initialize() {
        table.defaults().pad(2);
        Table content = new Table();
        content.defaults().pad(2);
        content.add(new Blink(0));
        content.add(new Blink(1));
        content.add(new Blink(2));
        content.row();
        content.add(new Blink(7));
        content.add();
        content.add(new Blink(3));
        content.row();
        content.add(new Blink(6));
        content.add(new Blink(5));
        content.add(new Blink(4));

        table.add(content);
    }

    @Override protected void doShow(IFuture<?> future) {
        future.addListener(this);
    }

    @Override public boolean handleBackPressed() {
        return true;
    }

    @Override protected boolean canBeClosed() {
        return false;
    }

    @Override public void onHappened(Object o) {
        hide();
    }

    private static class Blink extends Actor {

        private static final Color tmp = new Color(Color.WHITE);

        private final TextureRegion region;
        private float scaleOffset;
        private float colorOffset;

        public Blink(float index) {
            this.colorOffset = 1 - index / total;
            this.scaleOffset = 1 - index / total;
            region = Config.skin.getRegion("particle-white-pixel");
            setSize(size, size);
            setOrigin(size * 0.5f, size * 0.5f);
        }

        @Override public void act(float delta) {
            super.act(delta);
            colorOffset += delta / colorTime;
            float colorListBlend = colorOffset /** (float) colors.size*/;
            int from = MathUtils.floor(colorListBlend);
            int to = MathUtils.ceil(colorListBlend);
            Color fromColor = colors.get(from % colors.size);
            Color toColor = colors.get(to % colors.size);
            setColor(blend(fromColor, toColor, colorListBlend - from));

            scaleOffset += delta / disappearTime;
            setScale(1 - scaleOffset % 1f);
        }
        private Color blend(Color from, Color to, float alpha) {
            tmp.r = Interpolation.linear.apply(from.r, to.r, alpha);
            tmp.g = Interpolation.linear.apply(from.g, to.g, alpha);
            tmp.b = Interpolation.linear.apply(from.b, to.b, alpha);
            return tmp;
        }

        @Override public void draw(Batch batch, float parentAlpha) {
            GdxHelper.setBatchColor(batch, getColor(), parentAlpha);
            batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), 0);
        }
    }
}
