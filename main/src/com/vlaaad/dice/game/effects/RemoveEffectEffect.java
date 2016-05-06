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

package com.vlaaad.dice.game.effects;

import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 23.01.14 by vlaaad
 */
public class RemoveEffectEffect extends CreatureEffect {

    private final Creature creature;
    private final CreatureEffect effect;

    public RemoveEffectEffect(Creature creature, CreatureEffect effect) {
        super(null, "remove-effects-" + System.currentTimeMillis() + "-" + Math.random(), 1);
        this.creature = creature;
        this.effect = effect;
    }

    @Override public void apply(Creature creature) {
    }

    @Override public IFuture<Void> remove(Creature creature) {
        return this.creature.removeEffect(effect);
    }

    @Override public String getIconName() {
        throw new IllegalStateException("is hidden");
    }

    @Override public String getUiIconName() {
        throw new IllegalStateException("is hidden");
    }

    @Override public String locDescKey() {
        throw new IllegalStateException("is hidden");
    }

    @Override public boolean isHidden() {
        return true;
    }

    @Override public EffectType getType() {
        return EffectType.util;
    }
}
