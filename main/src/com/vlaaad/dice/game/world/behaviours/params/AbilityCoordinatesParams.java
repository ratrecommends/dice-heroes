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

package com.vlaaad.dice.game.world.behaviours.params;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 14.01.14 by vlaaad
 */
public class AbilityCoordinatesParams {
    public final Creature creature;
    public final Ability ability;
    public final Array<Grid2D.Coordinate> coordinates;

    public AbilityCoordinatesParams(Creature creature, Ability ability, Array<Grid2D.Coordinate> availableCoordinates) {
        this.creature = creature;
        this.ability = ability;
        coordinates = availableCoordinates;
    }
}
