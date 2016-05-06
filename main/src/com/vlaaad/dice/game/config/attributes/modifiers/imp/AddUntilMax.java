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
 * Created 04.05.14 by vlaaad
 */
public class AddUntilMax extends AttributeModifier<Integer> {
    private final int toAdd;
    private final int max;
    private final int priority;

    public AddUntilMax(int toAdd, int max, int priority) {
        this.toAdd = toAdd;
        this.max = max;
        this.priority = priority;
    }

    @Override public Integer apply(Integer initialValue) {
        if (initialValue > max)
            return initialValue;
        int result = initialValue + toAdd;
        return result > max ? max : result;
    }

    @Override public int getPriority() {
        return priority;
    }
}
