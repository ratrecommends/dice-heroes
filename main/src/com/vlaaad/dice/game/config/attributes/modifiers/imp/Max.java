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
 * Created 18.01.14 by vlaaad
 */
public class Max extends AttributeModifier<Integer> {
    private final int value;
    private final int priority;

    public Max(int value, int priority) {
        super();
        this.value = value;
        this.priority = priority;
    }

    @Override public int getPriority() {
        return priority;
    }

    @Override public Integer apply(Integer initialValue) {
        return Math.max(initialValue, value);
    }
}
