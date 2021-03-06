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

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.PoisonEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 20.04.14 by vlaaad
 */
public class PoisonAreaResult implements IActionResult {
    public final Ability ability;
    public final Creature creature;
    public final Grid2D.Coordinate coordinate;
    public final Array<Creature> targets;
    public final int turns;

    public PoisonAreaResult(Ability ability, Creature creature, Grid2D.Coordinate coordinate, Array<Creature> targets, int turns) {
        this.ability = ability;
        this.creature = creature;
        this.coordinate = coordinate;
        this.targets = targets;
        this.turns = turns;
    }

    @Override public void apply(World world) {
        for (Creature target : targets) {
            target.addEffect(new PoisonEffect(creature, ability, turns));
        }
    }
}
