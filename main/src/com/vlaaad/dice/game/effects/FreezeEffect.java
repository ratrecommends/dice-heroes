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
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.attributes.modifiers.imp.Set;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 10.01.14 by vlaaad
 */
public class FreezeEffect extends CreatureEffect {

    private final Set<Boolean> set;
    private final int turns;

    public FreezeEffect(Ability ability, int turnCount) {
        super(ability, "freeze", turnCount);
        turns = turnCount;
        set = new Set<Boolean>(Boolean.TRUE, 0);
    }

    @Override public void apply(Creature creature) {
        creature.addModifier(Attribute.frozen, set);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.removeModifier(Attribute.frozen, set);
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/freeze";
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-freeze";
    }

    @Override public EffectType getType() {
        return EffectType.util;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-freeze";
    }
}
