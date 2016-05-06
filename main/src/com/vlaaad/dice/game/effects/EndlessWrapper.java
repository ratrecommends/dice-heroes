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
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 23.01.14 by vlaaad
 */
public class EndlessWrapper extends CreatureEffect {
    private final CreatureEffect effect;

    public EndlessWrapper(CreatureEffect effect) {
        super(effect.ability, effect.effectGroup, 0);
        this.effect = effect;
    }

    @Override public int getTurnCount() { return 1; }

    @Override public boolean shouldEnd() { return false; }

    @Override public void apply(Creature creature) {
        effect.apply(creature);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        return effect.remove(creature);
    }

    @Override public boolean is(Class<? extends ICreatureEffect> type) {
        return effect.is(type);
    }

    @Override public <T extends ICreatureEffect> T as(Class<T> type) {
        return effect.as(type);
    }

    @Override public EffectType getType() {
        return effect.getType();
    }

    @Override public boolean eq(ICreatureEffect effect) {
        return this.effect.eq(effect);
    }

    @Override public String getIconName() {
        return effect.getIconName();
    }

    @Override public String getUiIconName() {
        return effect.getUiIconName();
    }

    @Override public String locDescKey() {
        return effect.locDescKey();
    }
}
