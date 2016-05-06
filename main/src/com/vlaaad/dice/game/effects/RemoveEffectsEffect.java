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

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 22.01.14 by vlaaad
 */
public class RemoveEffectsEffect extends CreatureEffect {
    private final ObjectMap<Creature, CreatureEffect> effects;

    public RemoveEffectsEffect(ObjectMap<Creature, CreatureEffect> effects) {
        super(null, "remove-effects-" + System.currentTimeMillis() + "-" + Math.random(), 1);
        this.effects = effects;
    }

    @Override public void apply(Creature creature) {
    }

    @Override public IFuture<Void> remove(Creature creature) {
        for (Creature key : effects.keys()) {
            key.removeEffect(effects.get(key));
        }
        return null;
    }

    @Override public boolean isHidden() {
        return true;
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

    @Override public EffectType getType() {
        return EffectType.util;
    }
}
