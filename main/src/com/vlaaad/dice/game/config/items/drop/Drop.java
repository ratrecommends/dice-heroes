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

package com.vlaaad.dice.game.config.items.drop;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.dice.game.config.items.Item;

/**
 * Created 06.03.14 by vlaaad
 */
public class Drop {
    private final Array<Roll> rolls;

    public Drop(Array<Roll> rolls) {
        this.rolls = rolls;
    }

    public Drop() {
        rolls = new Array<Roll>();
    }

    public void add(Roll roll) {
        rolls.add(roll);
    }

    public ObjectIntMap<Item> roll() {
        ObjectIntMap<Item> res = new ObjectIntMap<Item>();
        for (Roll roll : rolls) {
            for (RangeItemCount rangeItemCount : roll.roll()) {
                res.getAndIncrement(rangeItemCount.item, 0, rangeItemCount.count);
            }
        }
        return res;
    }

    @Override public String toString() {
        return rolls.toString("\n");
    }
}
