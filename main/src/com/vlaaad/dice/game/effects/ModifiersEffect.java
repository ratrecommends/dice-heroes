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
import com.vlaaad.dice.game.config.attributes.modifiers.AttributeModifier;
import com.vlaaad.dice.game.objects.Creature;

import java.util.HashMap;
import java.util.Map;

public class ModifiersEffect extends CreatureEffect {

    private final Map<Attribute, AttributeModifier> data;

    public ModifiersEffect(Ability owner, Map<Attribute, AttributeModifier> data, int turns) {
        super(owner, owner.name, turns);
        this.data = data;
    }

    @Override public void apply(Creature creature) {
        for (Attribute attribute : data.keySet()) {
            creature.addModifier(attribute, data.get(attribute));
        }
    }

    @Override public IFuture<Void> remove(Creature creature) {
        for (Attribute attribute : data.keySet()) {
            creature.removeModifier(attribute, data.get(attribute));
        }
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/" + ability.name;
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-" + ability.name;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-" + ability.name;
    }
}
