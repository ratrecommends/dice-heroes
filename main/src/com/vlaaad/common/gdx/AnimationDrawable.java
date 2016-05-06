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

package com.vlaaad.common.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Array;

/**
 * Created 08.10.13 by vlaaad
 */
public class AnimationDrawable extends BaseDrawable {
    private static final int fps = 30;

    public final Array<? extends TextureRegion> regions;
    private float stateTime = 0;

    public AnimationDrawable(Array<? extends TextureRegion> regions) {
        this.regions = regions;
        setMinWidth(regions.get(0).getRegionWidth());
        setMinHeight(regions.get(0).getRegionHeight());
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        stateTime += Gdx.graphics.getDeltaTime();
        batch.draw(getKeyFrame(stateTime), x, y, width, height);
    }

    public TextureRegion getKeyFrame(float stateTime) {
        int frameNumber = getKeyFrameIndex(stateTime);
        return regions.get(frameNumber);
    }

    public int getKeyFrameIndex(float stateTime) {
        if (regions.size == 1)
            return 0;
        return ((int) (stateTime * fps)) % regions.size;
    }
}
