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

import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.filters.AllOfFilter;
import com.vlaaad.common.util.filters.Filter;
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.events.imp.KillEvent;
import com.vlaaad.dice.game.objects.Creature;

import java.util.Map;

/**
 * Created 19.05.14 by vlaaad
 */
public class Killed extends AchievementCondition<KillEvent> {

    private final AllOfFilter<Creature[]> filter = new AllOfFilter<Creature[]>();

    public Killed() {
        super(KillEvent.class);
    }

    @Override public void setup(Object params) {
        Map map = (Map) params;
        final int more = MapHelper.get(map, "more", Numbers.MINUS_ONE).intValue();
        if (more != -1) {
            filter.add(new Filter<Creature[]>() {
                @Override public boolean accept(Creature[] creatures) {
                    return creatures.length > more;
                }
            });
        }
    }

    @Override protected boolean satisfied(KillEvent event) {
        return filter.accept(event.killed());
    }
}
