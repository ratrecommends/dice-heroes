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
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.effects.DecreaseAttackAndDefenceEffect;
import com.vlaaad.dice.game.effects.EndlessWrapper;
import com.vlaaad.dice.game.effects.RemoveEffectsEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 09.05.14 by vlaaad
 */
public class DecreaseAttackAndDefenceResult implements IActionResult{
    public final Ability ability;
    public final Creature creature;
    public final Array<Creature> targets;
    public final int value;
    public final int min;
    public final int turns;

    public final ObjectMap<Creature, CreatureEffect> effects = new ObjectMap<Creature, CreatureEffect>();
    private final RemoveEffectsEffect casterEffect;

    public DecreaseAttackAndDefenceResult(Ability ability, Creature creature, Array<Creature> targets, int value, int min, int turns) {
        this.ability = ability;
        this.creature = creature;
        this.targets = targets;
        this.value = value;
        this.min = min;
        this.turns = turns;

        for (Creature c : targets) {
            CreatureEffect effect = new EndlessWrapper(new DecreaseAttackAndDefenceEffect(ability, value, min, turns, "eery-mask"));
            effects.put(c, effect);
        }
        casterEffect = new RemoveEffectsEffect(effects);
    }

    @Override public void apply(World world) {
        for (Creature creature : effects.keys()) {
            creature.addEffect(effects.get(creature));
        }
        creature.addEffect(casterEffect);
    }
}
