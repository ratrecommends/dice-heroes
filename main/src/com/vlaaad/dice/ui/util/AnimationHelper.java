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

package com.vlaaad.dice.ui.util;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Created 02.01.14 by vlaaad
 */
public class AnimationHelper {
    public static void animateCounter(final Label label, final int from, final int to) {
        label.addAction(new TemporalAction(MathUtils.clamp(Math.abs(to - from) / 30f, 0.5f, 1.5f), new Interpolation.ExpOut(2, 3)) {
            @Override protected void update(float percent) {
                label.setText(String.valueOf((int) (from + (to - from) * percent)));
            }
        });
    }
}
