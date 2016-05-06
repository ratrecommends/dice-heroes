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

package com.vlaaad.common.util;

import com.badlogic.gdx.math.MathUtils;

/**
 * Created 20.11.13 by vlaaad
 */
public class MathHelper {
    public static int sign(float value) {
        if (value < 0)
            return -1;
        if (value > 0)
            return 1;
        return 0;
    }

    public static int sign(int value) {
        if (value < 0)
            return -1;
        if (value > 0)
            return 1;
        return 0;
    }

    public static float snapToNearest(float value, float snap) {
        if (snap == 0)
            return value;
        return MathUtils.round(value / snap) * snap;
    }

    public static float snapToHighest(float value, float snap) {
        if (snap == 0)
            return value;
        return MathUtils.ceil(value / snap) * snap;
    }

    public static float snapToLowest(float value, float snap) {
        if (snap == 0)
            return value;
        return MathUtils.floor(value / snap) * snap;
    }

    /**
     * @return offset point in [0, snap)
     */
    public static float snapOffset(float snap, float offset) {
        if (offset == 0) {
            return 0;
        } else {
            float result = offset % snap;
            return result < 0 ? result + snap : result;
        }
    }

    private MathHelper() {}

    public static boolean equal(float a, float b, float epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    public static int max(int v1, int v2, int v3) {
        return Math.max(v1, Math.max(v2, v3));
    }

    public static int min(int v1, int v2, int v3) {
        return Math.min(v1, Math.min(v2, v3));
    }
}
