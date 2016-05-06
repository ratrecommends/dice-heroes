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

package com.vlaaad.dice.achievements.conditions;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.dice.achievements.conditions.imp.*;

import java.util.Map;

/**
 * Created 16.05.14 by vlaaad
 */
public class AchievementConditionFactory {

    private static ObjectMap<String, Class<? extends AchievementCondition>> types = new ObjectMap<String, Class<? extends AchievementCondition>>();

    static {
        types.put("any-of", AnyOf.class);
        types.put("has-die", HasDie.class);
        types.put("obtained-die", ObtainDie.class);
        types.put("has-item", HasItem.class);
        types.put("earned-item", EarnedItem.class);
        types.put("has-potion", HasPotion.class);
        types.put("brewed-potion", BrewedPotion.class);
        types.put("has-every-potion", HasEveryPotion.class);
        types.put("levels-completed", LevelsCompleted.class);
        types.put("all-of", AllOfCondition.class);
        types.put("ended-level", EndedLevel.class);
        types.put("used-potions", UsedPotions.class);
        types.put("max-used-dice", MaxUsedDice.class);
        types.put("no-one-wears-abilities", NoOneWearsAbilities.class);
        types.put("kill", Kill.class);
        types.put("killer", Killer.class);
        types.put("killed", Killed.class);
        types.put("donated", Donated.class);
        types.put("inc", IncrementalCondition.class);
    }

    public static AchievementCondition create(Map map) {
        String typeName = MapHelper.get(map, "condition");
        if (typeName == null) {
            return AchievementCondition.ANY;
        }
        Class<? extends AchievementCondition> type = types.get(typeName);
        Object params = MapHelper.get(map, "params");
        if (type == null) throw new IllegalStateException("nu such condition type: " + typeName);
        AchievementCondition condition = null;
        try {
            condition = type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        condition.setup(params);
        return condition;
    }
}
