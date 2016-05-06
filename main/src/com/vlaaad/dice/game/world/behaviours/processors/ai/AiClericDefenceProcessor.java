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

package com.vlaaad.dice.game.world.behaviours.processors.ai;

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.imp.ClericDefence;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.behaviours.RequestProcessor;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;

import java.util.Comparator;

/**
 * Created 05.04.14 by vlaaad
 */
public class AiClericDefenceProcessor extends RequestProcessor<Creature, AbilityCreatureParams> implements Comparator<Creature> {
    @Override public int preProcess(AbilityCreatureParams params) {
        if (params.ability.action instanceof ClericDefence)
            return 2;
        return -1;
    }

    @Override public IFuture<Creature> process(AbilityCreatureParams params) {
        ClericDefence defence = (ClericDefence) params.ability.action;
        params.available.sort(this);
        ProfessionDescription profession = Config.professions.get("cleric");
        for (Creature t : params.available) {
            if (t.profession == profession)
                continue;
            int defenceLevel = t.get(Attribute.defenceFor(defence.attackType));
            if (defence.defenceLevel > defenceLevel) {
                return Future.completed(t);
            }
        }
        return null;
    }

    @Override public int compare(Creature o1, Creature o2) {
        return ExpHelper.getTotalCost(o2) - ExpHelper.getTotalCost(o1);
    }
}
