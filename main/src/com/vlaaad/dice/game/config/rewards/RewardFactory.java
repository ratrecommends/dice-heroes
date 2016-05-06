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

package com.vlaaad.dice.game.config.rewards;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.config.rewards.imp.AbilityReward;
import com.vlaaad.dice.game.config.rewards.imp.DieReward;
import com.vlaaad.dice.game.config.rewards.imp.ItemReward;

import java.util.HashMap;

/**
 * Created 14.10.13 by vlaaad
 */
public class RewardFactory {
    private static ObjectMap<String, Class<? extends Reward>> types = new ObjectMap<String, Class<? extends Reward>>();

    static {
        types.put("item", ItemReward.class);
        types.put("die", DieReward.class);
        types.put("ability", AbilityReward.class);
    }

    public static Reward create(HashMap<String, Object> rewardData) {
        try {
            return types.get((String) rewardData.get("type")).newInstance().doInit(rewardData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RewardFactory() {
    }
}
