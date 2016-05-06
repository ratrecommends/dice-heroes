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

package com.vlaaad.dice.game.config.rewards.imp;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.ui.windows.StoreWindow;

import java.util.HashMap;
import java.util.List;

public class AbilityReward extends Reward {

    private final Array<Ability> abilities = new Array<Ability>();


    @Override protected void init(HashMap<String, Object> data) {
        List<String> abilityNames = MapHelper.get(data, "abilities");
        for (String ability : abilityNames) {
            abilities.add(Config.abilities.get(ability));
        }
    }

    @Override public RewardResult apply(UserData userData) {
        final RewardResult.AddedAbility result = calcNextReward(userData);
        result.die.inventory.getAndIncrement(result.ability, 0, 1);
        return result;
    }

    private RewardResult.AddedAbility calcNextReward(UserData userData) {
        //shuffle abilities
        abilities.shuffle();
        //sort by least count in dice
        abilities.sort(Ability.countComparator(userData));

        //for every ability:
        final Array<Die> dice = new Array<Die>();
        for (Die die : userData.dice()) {
            if (MapHelper.countPositive(die.inventory) < StoreWindow.INVENTORY_SIZE)
                dice.add(die);
        }
        for (Ability ability : abilities) {
            //shuffle dice
            dice.shuffle();
            //sort dice by suitability for ability: has least abilities of the same type
            dice.sort(Die.abilitiesCountComparator(ability));
            //try to give to die
            for (Die die : dice) {
                if (ability.requirement.isSatisfied(die)) {
                    return new RewardResult.AddedAbility(ability, die);
                }
            }
        }
        throw new IllegalStateException("There is no ability any die can use!");
    }
}
