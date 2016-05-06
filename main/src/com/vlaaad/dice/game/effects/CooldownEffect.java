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
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 02.02.14 by vlaaad
 */
public class CooldownEffect extends CreatureEffect {
    private final String abilityName;

    public CooldownEffect(String abilityName, int turnCount, Ability ability) {
        super(ability, "cooldown-for-" + abilityName, turnCount);
        this.abilityName = abilityName;
    }

    @Override public void apply(Creature creature) {
        creature.set(Attribute.cooldownFor(abilityName), this);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.set(Attribute.cooldownFor(abilityName), null);
        return null;
    }

    @Override public boolean isRemovedOnDeath() {
        return false;
    }

    @Override public String getIconName() {
        return "effect-icon/cooldown-" + abilityName;
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-cooldown-" + abilityName;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-cooldown-" + abilityName;
    }
}
