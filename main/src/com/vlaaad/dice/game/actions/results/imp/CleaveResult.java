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

package com.vlaaad.dice.game.actions.results.imp;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 08.10.13 by vlaaad
 */
public class CleaveResult implements IActionResult {

    private static final Array<Creature> tmp = new Array<Creature>(Creature.class);

    public final Creature creature;
    public final Array<IActionResult> results;
    public final Array<Creature> targets;
    public final AttackType attackType;
    public final int attackLevel;

    public CleaveResult(Creature creature, Array<IActionResult> results, Array<Creature> targets, AttackType attackType, int attackLevel) {
        this.creature = creature;
        this.results = results;
        this.targets = targets;
        this.attackType = attackType;
        this.attackLevel = attackLevel;
    }

    @Override public void apply(World world) {
        for (IActionResult result : results) {
            SequenceResult sequence = (SequenceResult) result;
            AttackResult attackResult = (AttackResult) sequence.results.get(0);
            GiveExpResult giveExpResult = (GiveExpResult) sequence.results.get(1);
            if (attackResult.success)
                tmp.add(attackResult.target);
            giveExpResult.apply(world);
        }
        Creature[] killed = tmp.toArray();
        tmp.clear();
        world.kill(creature, killed);
    }
}
