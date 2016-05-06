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
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.FreezeEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 15.01.14 by vlaaad
 */
public class FirestormResult implements IActionResult {
    public final Creature caster;
    public final Ability ability;
    public final Array<Creature> underAttack;
    public final Array<Creature> killed;
    public final ObjectIntMap<Creature> addedExp;
    public final Grid2D.Coordinate cell;

    public FirestormResult(Creature caster, Ability ability, Array<Creature> underAttack, Array<Creature> killed, ObjectIntMap<Creature> addedExp, Grid2D.Coordinate cell) {
        super();
        this.caster = caster;
        this.ability = ability;
        this.underAttack = underAttack;
        this.killed = killed;
        this.addedExp = addedExp;
        this.cell = cell;
    }

    @Override public void apply(World world) {
        for (Creature creature : addedExp.keys()) {
            creature.addExp(addedExp.get(creature, 0));
        }
        for (Creature creature : underAttack) {
            creature.removeEffect(FreezeEffect.class);
        }
        Creature[] dead = killed.toArray(Creature.class);
        world.kill(caster, dead);
    }
}
