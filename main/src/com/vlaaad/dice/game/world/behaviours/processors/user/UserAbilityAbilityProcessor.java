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

package com.vlaaad.dice.game.world.behaviours.processors.user;

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityAbilityParams;
import com.vlaaad.dice.ui.windows.SelectAbilityWindow;

/**
 * Created 04.02.14 by vlaaad
 */
public class UserAbilityAbilityProcessor extends RequestProcessor<Ability, AbilityAbilityParams> {
    private SelectAbilityWindow window;
    @Override public int preProcess(AbilityAbilityParams params) {
        return 1;
    }

    @Override public IFuture<Ability> process(AbilityAbilityParams params) {
        final Future<Ability> future = new Future<Ability>();
        window = new SelectAbilityWindow();
        window.show(new SelectAbilityWindow.Params(params.ability, params.creature, params.availableAbilities, new SelectAbilityWindow.Callback() {
            @Override public void onSelected(Ability ability) {
                if (window != null) {
                    window = null;
                    future.happen(ability);
                }
            }
        }));
        return future;
    }

    @Override public void cancel() {
        if (window != null) {
            window.hide();
            window = null;
        }
    }
}
