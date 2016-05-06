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
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.CleaveResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

import java.util.HashMap;

/**
 * Created 06.10.13 by vlaaad
 */
public class Cleave extends CreatureAction {
    private AttackType attackType;
    private int attackLevel;

    public Cleave(Ability owner) {
        super(owner);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doInit(Object setup) {
        HashMap<String, Object> data = (HashMap<String, Object>) setup;
        attackType = AttackType.valueOf((String) data.get("type"));
        attackLevel = ((Number) data.get("level")).intValue();
    }

    @Override
    public IFuture<IActionResult> apply(Creature creature, World world) {
        Array<Creature> targets = Attack.findTargets(creature, world);
        if (targets.size == 0)
            return Future.completed(IActionResult.NOTHING);
        Array<IActionResult> results = new Array<IActionResult>();
        for (Creature t : targets) {
            results.add(Attack.calcResult(owner, creature, t, attackType, attackLevel));
        }
        return Future.<IActionResult>completed(new CleaveResult(creature, results, targets, attackType, attackLevel));
    }
}
