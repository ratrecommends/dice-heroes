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

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.GdxHelper;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;

/**
 * Created 04.11.13 by vlaaad
 */
public class AnimationSubView implements SubView {

    private final Array<? extends TextureRegion> regions;
    public int priority = -5;

    private final Actor actor = new Actor() {
        public float stateTime;
        private boolean completed;

        @Override public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= animation.getAnimationDuration() && !completed) {
                fire(new AnimationListener.AnimationEvent());
                completed = true;
            }
        }

        @Override public void draw(Batch batch, float parentAlpha) {
            if (regions.size <= 0)
                return;
            GdxHelper.setBatchColor(batch, getColor(), parentAlpha);
            TextureRegion region = animation.getKeyFrame(stateTime);
            float rotation = getRotation();
            if (rotation != 0) {
                batch.draw(
                    region,
                    getX(),
                    getY(),
                    getOriginX(),
                    getOriginY(),
                    region.getRegionWidth(),
                    region.getRegionHeight(),
                    1,
                    1,
                    rotation
                );
            } else {
                batch.draw(region, getX(), getY(), region.getRegionWidth(), region.getRegionHeight());
            }
        }
    };

    public final Animation animation;

    public AnimationSubView(float frameDuration, Array<? extends TextureRegion> regions, Animation.PlayMode playType) {
        this.animation = new Animation(frameDuration, regions, playType);
        this.regions = regions;
        if (regions.size > 0) {
            TextureRegion region = regions.first();
            actor.setSize(region.getRegionWidth(), region.getRegionHeight());
        }
    }

    @Override public int getPriority() {
        return priority;
    }

    @Override public void play(String animationName) {
    }

    @Override public Actor getActor() {
        return actor;
    }

}
