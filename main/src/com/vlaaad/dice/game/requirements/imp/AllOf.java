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

import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.game.config.CreatureRequirementFactory;
import com.vlaaad.dice.game.requirements.DieRequirement;
import com.vlaaad.dice.game.user.Die;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 16.10.13 by vlaaad
 */
public class AllOf extends DieRequirement {

    public final Array<DieRequirement> requirements;

    public AllOf(Map data) {
        requirements = new Array<DieRequirement>(data.size());
        for (Object requirementName : data.keySet()) {
            requirements.add(CreatureRequirementFactory.create(requirementName.toString(), data.get(requirementName)));
        }
    }

    @Override protected void doInit(Object setup) {
    }

    @Override public boolean isSatisfied(Die die) {
        for (DieRequirement requirement : requirements) {
            if (!requirement.isSatisfied(die))
                return false;
        }
        return true;
    }

    @Override public boolean canBeSatisfied(Die die) {
        for (DieRequirement requirement : requirements) {
            if (!requirement.canBeSatisfied(die))
                return false;
        }
        return true;
    }

    @Override public String toString() {
        return "all of: " + requirements;
    }

    @Override public String describe(Die die) {
        StringBuilder builder = new StringBuilder();
        for (DieRequirement requirement : requirements) {
            if (!requirement.isSatisfied(die))
                builder.append(requirement.describe(die)).append('\n');
        }
        return builder.toString();
    }

}
