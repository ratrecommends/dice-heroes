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

package com.vlaaad.dice.achievements.conditions.imp;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.events.imp.EndLevelEvent;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Creature;

import java.util.Map;

/**
 * Created 18.05.14 by vlaaad
 */
public class MaxUsedDice extends AchievementCondition<EndLevelEvent> {

    private final ObjectIntMap<ProfessionDescription> maxes = new ObjectIntMap<ProfessionDescription>();
    private static final ObjectIntMap<ProfessionDescription> tmp = new ObjectIntMap<ProfessionDescription>();
    private int maxCount = -1;

    public MaxUsedDice() {
        super(EndLevelEvent.class);
    }

    @Override public void setup(Object params) {
        if (params instanceof Number) {
            maxCount = ((Number) params).intValue();
        } else {
            Map map = (Map) params;
            for (String profession : MapHelper.keys(map)) {
                maxes.put(Config.professions.get(profession), MapHelper.get(map, profession, Numbers.ZERO).intValue());
            }
        }
    }

    @Override protected boolean satisfied(EndLevelEvent event) {
        ObjectSet<Creature> creatures = event.result().viewer.creatures;
        if (maxCount != -1) {
            return creatures.size <= maxCount;
        }
        ObjectIntMap<ProfessionDescription> used = tmp;
        for (Creature c : creatures) {
            used.getAndIncrement(c.profession, 0, 1);
        }
        for (ObjectIntMap.Entry<ProfessionDescription> entry : used.entries()) {
            ProfessionDescription profession = entry.key;
            int count = entry.value;
            int maxCount = maxes.get(profession, 0);
            if (count > maxCount) {
                tmp.clear();
                return false;
            }
        }
        tmp.clear();
        return true;
    }
}
