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

package com.vlaaad.dice.game.config.levels;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.items.drop.Drop;
import com.vlaaad.dice.game.config.items.drop.Range;
import com.vlaaad.dice.game.config.items.drop.RangeItemCount;
import com.vlaaad.dice.game.config.items.drop.Roll;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.RewardFactory;
import com.vlaaad.dice.game.user.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 08.07.14 by vlaaad
 */
public abstract class BaseLevelDescription {

    public final String name;
    public final String parent;
    public final int iconX;
    public final int iconY;
    public final String levelIcon;
    public final boolean useNumberInIcon;
    public final boolean hidden;
    public final Drop drop = new Drop();
    public final Array<Reward> passRewards = new Array<Reward>();
    public final Array<Reward> confirmRewards = new Array<Reward>();
    public final String iconStyle;

    public BaseLevelDescription(Map data) {
        name = MapHelper.get(data, "name");
        hidden = MapHelper.get(data, "hidden", Boolean.FALSE);
        if (hidden) {
            parent = null;
            iconX = 0;
            iconY = 0;
            levelIcon = null;
            useNumberInIcon = false;
            iconStyle = null;
            return;
        }
        parent = MapHelper.get(data, "parent");
        iconX = MapHelper.get(data, "x", Numbers.ZERO).intValue();
        iconY = MapHelper.get(data, "y", Numbers.ZERO).intValue();
        levelIcon = MapHelper.get(data, "level-icon");
        iconStyle = MapHelper.get(data, "icon-style", "");
        useNumberInIcon = levelIcon == null ? MapHelper.get(data, "icon-number", Boolean.TRUE) : false;

        List<Map> drop = MapHelper.get(data, "drop");
        if (drop != null) {
            for (Map rollData : drop) {
                String rangeDescriptor = MapHelper.get(rollData, "roll");

                List<Map<String, Object>> items = MapHelper.get(rollData, "items");
                Range range = rangeDescriptor == null ? null : new Range(rangeDescriptor);
                Array<RangeItemCount> droppedInRoll = new Array<RangeItemCount>();
                for (Map<String, Object> itemMap : items) {
                    for (String itemName : itemMap.keySet()) {
                        Item item = Config.items.get(itemName);
                        Object param = itemMap.get(itemName);
                        RangeItemCount rangeItemCount;
                        if (param instanceof Integer) {
                            rangeItemCount = new RangeItemCount(null, item, (Integer) param);
                        } else if (param instanceof Map) {
                            Map paramMap = (Map) param;
                            Integer count = MapHelper.get(paramMap, "count");
                            String paramRangeDescriptor = MapHelper.get(paramMap, "range");
                            Range paramRange = paramRangeDescriptor == null ? null : new Range(paramRangeDescriptor);
                            rangeItemCount = new RangeItemCount(paramRange, item, count);
                        } else {
                            throw new IllegalStateException("Could not parse range-item-count for " + itemName + ": " + param);
                        }
                        droppedInRoll.add(rangeItemCount);
                    }
                }

                Roll roll = new Roll(range, droppedInRoll);
                this.drop.add(roll);
            }
        }

        HashMap<String, ArrayList<HashMap<String, Object>>> rewards = MapHelper.get(data, "rewards");
        if (rewards != null) {
            ArrayList<HashMap<String, Object>> passRewards = rewards.get("pass");
            if (passRewards != null) {
                for (HashMap<String, Object> rewardData : passRewards) {
                    this.passRewards.add(RewardFactory.create(rewardData));
                }
            }

            ArrayList<HashMap<String, Object>> confirmRewards = rewards.get("confirm");
            if (confirmRewards != null) {
                for (HashMap<String, Object> rewardsData : confirmRewards) {
                    this.confirmRewards.add(RewardFactory.create(rewardsData));
                }
            }
        }
    }


    public boolean canBeStarted(UserData userData) {
        return !hidden && (parent == null || userData.isPassed(Config.levels.get(parent)));
    }

    public BaseLevelDescription getParentLevel() {
        return parent == null ? null : Config.levels.get(parent);
    }

    public int getLevelNumber() {
        int counter = 1;
        BaseLevelDescription check = this;
        while (check.parent != null) {
            counter++;
            check = Config.levels.get(check.parent);
        }
        return counter;
    }

}
