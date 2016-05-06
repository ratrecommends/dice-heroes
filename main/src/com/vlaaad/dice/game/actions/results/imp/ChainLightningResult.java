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
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 12.01.14 by vlaaad
 */
public class ChainLightningResult implements ITargetOwner, IActionResult {
    public final Creature caster;
    public final Ability ability;
    public final Array<Creature> chain;
    public final Array<Creature> killed;
    public final ObjectIntMap<Creature> addedExp;

    public ChainLightningResult(Creature caster, Ability ability, Array<Creature> chain, Array<Creature> killed, ObjectIntMap<Creature> addedExp) {
        super();
        this.caster = caster;
        this.ability = ability;
        this.chain = chain;
        this.killed = killed;
        this.addedExp = addedExp;
    }

    @Override public void apply(World world) {
        for (Creature creature : addedExp.keys()) {
            creature.addExp(addedExp.get(creature, 0));
        }
        Creature[] arr = killed.toArray(Creature.class);
        world.kill(caster, arr);
    }

    @Override public Creature getTarget() {
        return chain.first();
    }
}
