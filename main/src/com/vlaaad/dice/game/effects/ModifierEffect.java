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
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.attributes.modifiers.AttributeModifier;
import com.vlaaad.dice.game.config.attributes.modifiers.imp.Set;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 28.01.14 by vlaaad
 */
public class ModifierEffect<T> extends CreatureEffect {

    private final Attribute<T> attribute;
    private final AttributeModifier<T> modifier;
    private final String displayedId;
    private final boolean removeOnDeath;

    public ModifierEffect(Ability owner, Attribute<T> attribute, AttributeModifier<T> modifier, int turns, String group, String displayedId) {
        this(owner, attribute, modifier, turns, group, displayedId, true);
    }

    public ModifierEffect(Ability ability, Attribute<T> attribute, AttributeModifier<T> modifier, int turns, String group, String displayedId, boolean removeOnDeath) {
        super(ability, group, turns);
        this.attribute = attribute;
        this.modifier = modifier;
        this.displayedId = displayedId;
        this.removeOnDeath = removeOnDeath;
    }

    @Override public void apply(Creature creature) {
        creature.addModifier(attribute, modifier);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.removeModifier(attribute, modifier);
        return null;
    }

    @Override public boolean isRemovedOnDeath() {
        return removeOnDeath;
    }

    @Override public String getIconName() {
        return "effect-icon/" + displayedId;
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-" + displayedId;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-" + displayedId;
    }
}
