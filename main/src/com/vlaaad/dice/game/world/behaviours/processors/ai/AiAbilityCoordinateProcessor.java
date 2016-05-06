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

package com.vlaaad.dice.game.world.behaviours.processors.ai;

import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;

/**
 * Created 27.05.14 by vlaaad
 */
public abstract class AiAbilityCoordinateProcessor extends RequestProcessor<Grid2D.Coordinate, AbilityCoordinatesParams> {

    private final String abilityName;

    public AiAbilityCoordinateProcessor(String abilityName) {
        this.abilityName = abilityName;
    }

    @Override public int preProcess(AbilityCoordinatesParams params) {
        if (!params.ability.name.equals(abilityName))
            return -1;
        return priority();
    }

    protected int priority() {
        return 2;
    }

    @Override public IFuture<Grid2D.Coordinate> process(AbilityCoordinatesParams params) {
        return Future.completed(getResult(params));
    }

    protected abstract Grid2D.Coordinate getResult(AbilityCoordinatesParams params);
}
