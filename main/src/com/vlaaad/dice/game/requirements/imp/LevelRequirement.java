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

package com.vlaaad.dice.game.requirements.imp;

import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.requirements.DieRequirement;
import com.vlaaad.dice.game.user.Die;

/**
 * Created 16.10.13 by vlaaad
 */
public class LevelRequirement extends DieRequirement {
    private int level;

    public LevelRequirement withLevel(int value){
        level = value;
        return this;
    }

    @Override protected void doInit(Object setup) {
        level = ((Number) setup).intValue();
    }

    @Override public boolean isSatisfied(Die die) {
        return die.getCurrentLevel() >= level;
    }

    @Override public boolean canBeSatisfied(Die die) {
        return true;
    }

    @Override public String toString() {
        return "level: " + level;
    }

    @Override public String describe(Die die) {
        return Config.thesaurus.localize("requirement-level", Thesaurus.params()
            .with("name", die.nameLocKey())
            .with("level", String.valueOf(level))
        );
    }
}
