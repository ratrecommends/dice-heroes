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

package com.vlaaad.dice.game.world.view.visualizers;

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.results.IAbilityOwner;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.effects.IDefenceEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.view.*;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;

public class DefenceVisualizer implements IVisualizer<Defence> {

    private final AbilityVisualizer<DefenceEffectAction> abilityVisualizer;

    public DefenceVisualizer(final ResultVisualizer visualizer) {
        abilityVisualizer = AbilityVisualizer
            .withDefault(new DefenceEffectActionVisualizer(visualizer))
            .with("super-elven-defence", new AnimationFadeVisualizer<DefenceEffectAction>(visualizer));
    }

    public static class DefenceEffectAction implements IAbilityOwner, ITargetOwner {
        public final Defence defence;
        public final IDefenceEffect effect;

        private DefenceEffectAction(Defence defence, IDefenceEffect effect) {
            this.defence = defence;
            this.effect = effect;
        }

        @Override public Ability getAbility() {
            return effect.getAbility();
        }

        @Override public Creature getTarget() {
            return defence.target;
        }
    }

    @Override public IFuture<Void> visualize(Defence defence) {
        Creature target = defence.target;
        AttackType type = defence.type;
        IDefenceEffect defenceEffect = null;
        for (CreatureEffect effect : target.effects) {
            if (effect.is(IDefenceEffect.class)) {
                IDefenceEffect d = effect.as(IDefenceEffect.class);
                if (type != d.getDefenceType())
                    continue;
                if (defenceEffect == null || d.getDefenceLevel() > defenceEffect.getDefenceLevel()) {
                    defenceEffect = d;
                }
            }
        }

        if (defenceEffect == null)
            return Future.completed();

        return abilityVisualizer.visualize(new DefenceEffectAction(defence, defenceEffect));
    }

}
