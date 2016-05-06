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

package com.vlaaad.dice.game.actions.imp;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.DecreaseAttackAndDefenceResult;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

/**
 * Created 09.05.14 by vlaaad
 */
public class DecreaseAttackAndDefence extends CreatureAction {

    private float radius;
    private int value;
    private int min;
    private Creature.CreatureRelation relation;
    private int turns;

    public DecreaseAttackAndDefence(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map map = (Map) setup;

        // {radius: 3, value: 1, min: 1, relation: enemy, turns: 1}

        radius = MapHelper.get(map, "radius", Numbers.ONE).floatValue();
        value = MapHelper.get(map, "value", Numbers.ONE).intValue();
        min = MapHelper.get(map, "min", Numbers.ONE).intValue();
        relation = Creature.CreatureRelation.valueOf(MapHelper.get(map, "relation", "any"));
        turns = MapHelper.get(map, "turns", Numbers.ONE).intValue();

        setDescriptionParamsMap(map);
    }

    @Override public IFuture<? extends IActionResult> apply(Creature creature, World world) {
        return Future.completed(calcResult(creature, creatures(creature, relation, radius)));
    }

    private IActionResult calcResult(Creature creature, Array<Creature> targets) {
        return new SequenceResult(
            new DecreaseAttackAndDefenceResult(owner, creature, targets, value, min, turns),
            new GiveExpResult(creature, targets.size)
        );
    }
}
