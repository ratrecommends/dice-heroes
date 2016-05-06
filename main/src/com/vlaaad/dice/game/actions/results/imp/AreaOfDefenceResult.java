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
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.effects.DefenceBonusEffect;
import com.vlaaad.dice.game.effects.EndlessWrapper;
import com.vlaaad.dice.game.effects.RemoveEffectsEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 24.01.14 by vlaaad
 */
public class AreaOfDefenceResult implements IActionResult {

    public final Ability ability;
    public final Creature creature;
    public final Array<Creature> targets;
    public final AttackType attackType;
    public final int level;
    public final ObjectMap<Creature, CreatureEffect> effects;
    public final RemoveEffectsEffect casterEffect;

    public AreaOfDefenceResult(Ability ability, Creature creature, Array<Creature> targets, AttackType attackType, int level) {
        super();
        this.ability = ability;
        this.creature = creature;
        this.targets = targets;
        this.attackType = attackType;
        this.level = level;
        effects = new ObjectMap<Creature, CreatureEffect>();
        for (Creature c : targets) {
            CreatureEffect effect = new EndlessWrapper(new DefenceBonusEffect(ability, attackType, level));
            effects.put(c, effect);
        }
        casterEffect = new RemoveEffectsEffect(effects);
    }

    @Override public void apply(World world) {
        for (Creature c : effects.keys()) {
            c.addEffect(effects.get(c));
        }
        creature.addEffect(casterEffect);
    }
}
