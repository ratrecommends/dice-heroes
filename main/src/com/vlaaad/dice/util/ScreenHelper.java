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

package com.vlaaad.dice.util;

/**
 * Created 03.03.14 by vlaaad
 */
public class ScreenHelper {

    private static WidthScale ws(int width, int scale) {
        return new WidthScale(width, scale);
    }

    public static class WidthScale {
        public final int width;
        public final int scale;

        private WidthScale(int width, int scale) {
            this.width = width;
            this.scale = scale;
        }
    }

    private static final WidthScale[] scales = {ws(160, 1), ws(320, 2), ws(480, 3), ws(640, 4), ws(800, 5), ws(960, 6), ws(1120, 7), ws(1280, 8)};
    public static WidthScale min = scales[0];
    public static WidthScale max = scales[scales.length - 1];

    public static float scaleFor(float screenWidth, boolean isDesktop) {
        if (isDesktop)
            return 2;
        for (int i = scales.length - 1; i >= 0; i--) {
            WidthScale scale = scales[i];
            if (screenWidth >= scale.width)
                return scale.scale;
        }
        return 1;
    }
}
