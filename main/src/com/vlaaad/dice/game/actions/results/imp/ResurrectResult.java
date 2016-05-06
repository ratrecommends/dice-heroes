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

import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 06.02.14 by vlaaad
 */
public class ResurrectResult implements ITargetOwner, IActionResult {
    public final Ability ability;
    public final Creature creature;
    public final Creature resurrected;
    public final Grid2D.Coordinate coordinate;

    public ResurrectResult(Ability ability, Creature creature, Creature resurrected, Grid2D.Coordinate coordinate) {
        super();
        this.ability = ability;
        this.creature = creature;
        this.resurrected = resurrected;
        this.coordinate = coordinate;
    }

    @Override public void apply(World world) {
        resurrected.onResurrected();
        creature.world.add(coordinate.x(), coordinate.y(), resurrected);
    }

    @Override public Creature getTarget() {
        return resurrected;
    }
}
