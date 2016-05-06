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

package com.vlaaad.dice.game.config.attributes.modifiers.imp;

import com.vlaaad.dice.game.config.attributes.modifiers.AttributeModifier;

/**
 * Created 09.05.14 by vlaaad
 */
public class SubtractUntilMin extends AttributeModifier<Integer> {
    private final int value;
    private final int min;
    private final int priority;

    public SubtractUntilMin(int value, int min, int priority) {super();
        this.value = value;
        this.min = min;
        this.priority = priority;
    }

    @Override public Integer apply(Integer initialValue) {
        if (initialValue < min)
            return initialValue;
        int result = initialValue - value;
        return result < min ? min : result;
    }

    @Override public int getPriority() {
        return priority;
    }
}
