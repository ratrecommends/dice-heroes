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

import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.FreezeEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

public class IceStormResult implements IActionResult {
    public final Ability ability;
    public final Creature creature;
    public final Grid2D.Coordinate coordinate;
    public final ObjectIntMap<Creature> targets;

    public IceStormResult(Ability ability, Creature creature, Grid2D.Coordinate coordinate, ObjectIntMap<Creature> targets) {
        this.ability = ability;
        this.creature = creature;
        this.coordinate = coordinate;
        this.targets = targets;
    }

    @Override public void apply(World world) {
        for (ObjectIntMap.Entry<Creature> e : targets.entries()) {
            e.key.addEffect(new FreezeEffect(ability, e.value));
        }
    }
}
