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

package com.vlaaad.dice.game.util;

import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 16.10.13 by vlaaad
 */
public class ExpHelper {

    public static final int MIN_EXP = 1;

    public static int expForKill(Creature creature, Creature target) {
        float creatureCost = getTotalCost(creature);
        float targetCost = getTotalCost(target);
        return result(ln(targetCost * 1.3f - creatureCost));
    }

    /**
     * defenceLevel >= attackLevel
     */
    public static int expForDefence(Creature attacker, Creature defenced) {
        float attackerCost = getTotalCost(attacker);
        float defencedCost = getTotalCost(defenced);
        return result(ln(attackerCost / 1.3f - defencedCost * 1.7f));
    }

    public static int getTotalCost(Creature target) {
        int result = 0;
        for (Ability ability : target) {
            result += ability.cost;
        }
        return result;
    }

    private static int result(int value) {
        return Math.max(value, MIN_EXP);
    }

    private static int ln(float value) {
        return (int) Math.log(Math.max(value, 1));
    }
}
