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

import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.ClericDefenceEffect;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.effects.EndlessWrapper;
import com.vlaaad.dice.game.effects.RemoveEffectEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 18.01.14 by vlaaad
 */
public class ClericDefenceResult extends AddEffect {
    public final Ability ability;
    public final Creature caster;
    public final Creature target;
    public final ClericDefenceEffect effect;

    public ClericDefenceResult(Ability ability, Creature caster, Creature target, ClericDefenceEffect effect) {
        super(ability, target, effect);
        this.ability = ability;
        this.caster = caster;
        this.target = target;
        this.effect = effect;
    }

    @Override public void apply(World world) {
        CreatureEffect wrapper = new EndlessWrapper(effect);
        target.addEffect(wrapper);
        caster.addEffect(new RemoveEffectEffect(target, wrapper));
    }
}
