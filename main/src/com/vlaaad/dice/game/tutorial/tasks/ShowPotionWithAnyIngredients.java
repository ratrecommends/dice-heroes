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

package com.vlaaad.dice.game.tutorial.tasks;

import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.states.GameMapState;

/**
 * Created 31.03.14 by vlaaad
 */
public class ShowPotionWithAnyIngredients extends TutorialTask {
    @Override public void start(Callback callback) {
        GameMapState state = resources.get("state");
        Ability potion = null;
        for (Ability a : Config.abilities.byType(Ability.Type.potion)) {
            for (Item item : a.ingredients.keys()) {
                if (item.type == Item.Type.anyIngredient) {
                    potion = a;
                    break;
                }
            }
        }
        if (potion == null)
            throw new IllegalStateException("there is potions with any ingredients!");
        state.potionsWindow.switchTo(potion);
        callback.taskEnded();
    }
}
