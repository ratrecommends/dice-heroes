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

package com.vlaaad.dice.game.config.attributes.modifiers;

import java.util.Comparator;

/**
 * Created 08.10.13 by vlaaad
 */
public abstract class AttributeModifier<T> {
    public static final Comparator<AttributeModifier> COMPARATOR = new Comparator<AttributeModifier>() {
        @Override public int compare(AttributeModifier o1, AttributeModifier o2) {
            return o2.getPriority() - o1.getPriority();
        }
    };

    public int getPriority() {
        return 0;
    }

    public abstract T apply(T initialValue);
}
