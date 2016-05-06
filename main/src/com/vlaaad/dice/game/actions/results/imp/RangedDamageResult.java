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

package com.vlaaad.dice.game.actions.results.imp;

import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 14.03.14 by vlaaad
 */
public class RangedDamageResult implements ITargetOwner, IActionResult {
    public final boolean success;
    public final Creature creature;
    public final Creature target;
    public final int level;
    public final Ability ability;

    public RangedDamageResult(boolean success, Creature creature, Creature target, int level, Ability ability) {
        this.success = success;
        this.creature = creature;
        this.target = target;
        this.level = level;
        this.ability = ability;
    }


    @Override public void apply(World world) {
        if (success) {
            world.kill(creature, target);
        }
    }

    @Override public Creature getTarget() {
        return target;
    }
}
