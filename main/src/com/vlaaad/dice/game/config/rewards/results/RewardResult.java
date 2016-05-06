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

package com.vlaaad.dice.game.config.rewards.results;

import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.user.Die;

public interface RewardResult {
    class AddedItems implements RewardResult {
        public final Item item;
        public final int addedCount;

        public AddedItems(Item item, int addedCount) {
            this.item = item;
            this.addedCount = addedCount;
        }
    }

    class AddedDie implements RewardResult {
        public final Die die;

        public AddedDie(Die die) {
            this.die = die;
        }
    }

    class AddedAbility implements RewardResult {
        public final Ability ability;
        public final Die die;

        public AddedAbility(Ability ability, Die die) {
            this.ability = ability;
            this.die = die;
        }
    }
}
