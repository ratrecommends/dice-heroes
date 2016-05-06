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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.common.gdx.scene2d.events.AnimationListener;
import com.vlaaad.common.util.MathHelper;
import com.vlaaad.dice.Config;

/**
 * Created 23.11.13 by vlaaad
 */
public class RotateODImagesSubView implements SubView {

    private final Image orto;
    private final Image dia;
    private final int targetIndex;
    private int currentIndex;
    private final Group group = new Group() {
        private float stateTime;

        @Override public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= frameDuration) {
                stateTime -= frameDuration;

                int sign = MathHelper.sign(targetIndex - currentIndex);
                currentIndex += sign;
                updateImage();

                if (currentIndex == targetIndex) {
                    fire(new AnimationListener.AnimationEvent());
                }
            }
        }
    };

    private float frameDuration = 1 / 10f;

    public RotateODImagesSubView(String imageBaseName, int dx, int dy, float frameDuration, int initialIndex) {
        this.frameDuration = frameDuration;
        currentIndex = initialIndex;
        //dx == 1 && dy == 1 -> top right
        orto = new Image(Config.skin, imageBaseName + "-ortogonal");
        dia = new Image(Config.skin, imageBaseName + "-diagonal");
        orto.setOrigin(orto.getWidth() / 2f, orto.getHeight() / 2f);
        dia.setOrigin(dia.getWidth() / 2f, dia.getHeight() / 2f);
        updateImage();
        targetIndex = calcTargetIndex(dx, dy);
    }

    // -2 -1  0
    // -3     1
    //  4  3  2
    private void updateImage() {
        Image toUse;
        Image toRemove;
        if (currentIndex % 2 == 0) { // even
            toUse = dia;
            toRemove = orto;
        } else {
            toUse = orto;
            toRemove = dia;
        }
        toRemove.remove();
        group.addActor(toUse);
        toUse.setRotation(getRotation(currentIndex));
    }

    public static int getRotation(int dx, int dy) {
        return getRotation(calcTargetIndex(dx, dy));
    }

    public static int getRotation(int rotationIndex) {
        if (rotationIndex == 0 || rotationIndex == 1)
            return 0;
        else if (rotationIndex == -1 || rotationIndex == -2)
            return 90;
        else if (rotationIndex == 2 || rotationIndex == 3)
            return -90;
        else
            return 180;
    }

    // (-1, 1) ( 0, 1) ( 1, 1)
    // (-1, 0) ( 0, 0) ( 1, 0)
    // (-1,-1) ( 0,-1) ( 1, -1)

    // -2 -1  0
    // -3     1
    //  4  3  2
    public static int calcTargetIndex(int dx, int dy) {
        if (dx == 1 && dy == 1) {
            return 0;
        } else if (dx == 1 && dy == 0) {
            return 1;
        } else if (dx == 0 && dy == 1) {
            return -1;
        } else if (dx == 1 && dy == -1) {
            return 2;
        } else if (dx == -1 && dy == 1) {
            return -2;
        } else if (dx == 0 && dy == -1) {
            return 3;
        } else if (dx == -1 && dy == 0) {
            return -3;
        } else if (dx == -1 && dy == -1) {
            return 4;
        } else
            throw new IllegalArgumentException(dx + ", " + dy + " is not a valid input for rotation");
    }


    public int getCurrentIndex() { return currentIndex;}

    @Override public Actor getActor() { return group; }

    @Override public int getPriority() { return -5; }

    @Override public void play(String animationName) {}
}
