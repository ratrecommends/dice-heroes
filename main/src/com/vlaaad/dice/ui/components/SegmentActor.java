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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.vlaaad.dice.Config;

/**
 * Created 12.01.14 by vlaaad
 */
public class SegmentActor extends Actor {

    private final Vector2 to = new Vector2();
    private final TextureRegion region;
    private final float len;
    private final float angle;

    public SegmentActor(Vector2 vector, String regionName) {
        to.set(vector);
        len = to.len();
        angle = to.angle();
        this.region = Config.skin.getRegion(regionName);
    }

    @Override public void draw(Batch batch, float parentAlpha) {
//        batch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        batch.draw(region,
            getX(), getY(),                         //position
            0, region.getRegionHeight() / 2f,       //origins
            len, region.getRegionHeight(),          //size
            1, 1,                                   //scale
            angle                                   //rotation
        );

//        batch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }
}
