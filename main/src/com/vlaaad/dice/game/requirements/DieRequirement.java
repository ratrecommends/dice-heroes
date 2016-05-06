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

package com.vlaaad.dice.game.requirements;

import com.vlaaad.dice.game.user.Die;

/**
 * Created 06.10.13 by vlaaad
 */
public abstract class DieRequirement {
    public static final DieRequirement ANY = new DieRequirement() {
        @Override protected void doInit(Object setup) { }

        @Override public boolean isSatisfied(Die die) { return true; }

        @Override public boolean canBeSatisfied(Die die) { return true; }

        @Override public String toString() { return "any"; }

        @Override public String describe(Die die) { return ""; }
    };

    private Object config;

    public final DieRequirement init(Object setup) {
        config = setup;
        doInit(setup);
        return this;
    }

    public final Object getConfig() {
        return config;
    }

    protected abstract void doInit(Object setup);

    public abstract boolean isSatisfied(Die die);

    public abstract boolean canBeSatisfied(Die die);

    @Override public abstract String toString();

    public abstract String describe(Die die);
}
