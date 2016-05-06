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

package com.vlaaad.common.gdx.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;

/**
 * Created 22.10.13 by vlaaad
 */
public class AnimatedActor extends Actor {
    private Animation.PlayMode type = Animation.PlayMode.LOOP;
    private Animation animation;
    private float stateTime;
    private float loopTime;

    public AnimatedActor(Array<TextureAtlas.AtlasRegion> regions) {
        this(1f / 30f, regions);
    }

    public AnimatedActor(float frameDuration, Array<TextureAtlas.AtlasRegion> regions) {
        animation = new Animation(frameDuration, regions, type);

        setSize(regions.first().getRegionWidth(), regions.first().getRegionHeight());
    }

    @Override public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        loopTime += delta;
        if (loopTime > animation.getAnimationDuration()) {
            loopTime -= animation.getAnimationDuration();
            fire(new ChangeListener.ChangeEvent());
        }
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        TextureRegion region = animation.getKeyFrame(stateTime);

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        float scaleX = getScaleX();
        float scaleY = getScaleY();

        float rotation = getRotation();
        if (scaleX == 1 && scaleY == 1 && rotation == 0)
            batch.draw(region, getX(), getY(), getWidth(), getHeight());
        else {
            batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), scaleX, scaleY, rotation);
        }
    }
}
