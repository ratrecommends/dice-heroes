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

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityAbilityParams;

/**
 * Created 12.02.14 by vlaaad
 */
public class MostCostlyAbilityProcessor extends RequestProcessor<Ability, AbilityAbilityParams> {
    @Override public int preProcess(AbilityAbilityParams params) {
        return 1;
    }

    @Override public IFuture<Ability> process(AbilityAbilityParams params) {
        Ability result = params.availableAbilities.first();
        for (int i = 1; i < params.availableAbilities.size; i++) {
            Ability check = params.availableAbilities.get(i);
            if (check.cost > result.cost)
                result = check;
        }
        return Future.completed(result);
    }
}
