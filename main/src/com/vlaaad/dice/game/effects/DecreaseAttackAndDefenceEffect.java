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
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.attributes.modifiers.AttributeModifier;
import com.vlaaad.dice.game.config.attributes.modifiers.imp.SubtractUntilMin;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 09.05.14 by vlaaad
 */
public class DecreaseAttackAndDefenceEffect extends CreatureEffect {
    private final String view;
    private final AttributeModifier<Integer> modifier;

    public DecreaseAttackAndDefenceEffect(Ability ability, int value, int min, int turns, String view) {
        super(ability, "decrease-attack-and-defence", turns);
        this.view = view;
        this.modifier = new SubtractUntilMin(value, min, -2000);
    }

    @Override public void apply(Creature creature) {
        creature.addModifier(Attribute.attackFor(AttackType.weapon), modifier);
        creature.addModifier(Attribute.defenceFor(AttackType.weapon), modifier);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.removeModifier(Attribute.attackFor(AttackType.weapon), modifier);
        creature.removeModifier(Attribute.defenceFor(AttackType.weapon), modifier);
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/" + view;
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-" + view;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-" + view;
    }
}
