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

import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.user.UserData;

import java.util.HashMap;

/**
 * Created 14.10.13 by vlaaad
 */
public class ItemReward extends Reward {
    public Item item;
    public int count;

    @Override protected void init(HashMap<String, Object> data) {
        item = Config.items.get((String) data.get("item"));
        count = MapHelper.get(data, "count", Numbers.ONE).intValue();
    }

    @Override public RewardResult apply(UserData userData) {
        userData.setItemCount(item, userData.getItemCount(item) + count);
        return new RewardResult.AddedItems(item, count);
    }

}
