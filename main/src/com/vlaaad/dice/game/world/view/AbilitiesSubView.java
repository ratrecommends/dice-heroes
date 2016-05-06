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

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.AnimationDrawable;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.world.controllers.ViewController;

/**
 * Created 08.10.13 by vlaaad
 */
public class AbilitiesSubView implements SubView {

    private final Array<String> frames;
    private final Image image = new Image();
    private final TextureRegionDrawable idleDrawable;
    private final AnimationDrawable animationDrawable;

    private int priority;

    public AbilitiesSubView(String main, Array<String> frames) {
        this.frames = frames;

        Array<TextureRegion> regions = new Array<TextureRegion>();
        for (String frame : frames) {
            regions.add(Config.skin.getRegion("ability/" + frame));
        }

        idleDrawable = new TextureRegionDrawable(Config.skin.getRegion("ability/" + main));
        regions.shuffle();
        animationDrawable = new AnimationDrawable(regions);
        image.setSize(ViewController.CELL_SIZE, ViewController.CELL_SIZE);
    }

    @Override public int getPriority() {
        return priority;
    }

    @Override public void play(String animationName) {
        int idx = frames.indexOf(animationName, false);
        if (idx != -1) {
            image.setDrawable(Config.skin.getDrawable("ability/" + animationName));
            return;
        }
        if (animationName.equals("idle")) {
            image.setDrawable(idleDrawable);
        }
        if (animationName.equals("roll")) {
            image.setDrawable(animationDrawable);
        }

    }

    @Override public Actor getActor() {
        return image;
    }
}
