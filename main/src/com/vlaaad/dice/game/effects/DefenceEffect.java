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
import com.vlaaad.dice.game.config.attributes.modifiers.imp.Add;
import com.vlaaad.dice.game.config.attributes.modifiers.imp.Max;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 08.10.13 by vlaaad
 */
public class DefenceEffect extends CreatureEffect implements IDefenceEffect {

    public final AttackType type;
    public final int defenceLevel;
    private final AttributeModifier<Integer> modifier;

    public DefenceEffect(Ability owner, AttackType type, int defenceLevel, int turnCount, String effectGroup, boolean additive) {
        super(owner, effectGroup, turnCount);
        this.type = type;
        this.defenceLevel = defenceLevel;
        this.modifier = additive ? new Add(defenceLevel) : new Max(defenceLevel, -1000);
    }

    @Override public void apply(Creature creature) {
        Attribute<Integer> attribute = Attribute.valueOf(type + Attribute.DEFENCE_SUFFIX);
        creature.addModifier(attribute, modifier);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.removeModifier(Attribute.<Integer>valueOf(type + Attribute.DEFENCE_SUFFIX), modifier);
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/defence-" + type.toString() + "-" + defenceLevel;
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-defence-" + type.toString() + "-" + defenceLevel;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-defence-" + type.toString() + "-" + defenceLevel;
    }

    @Override public String toString() {
        return "defence-" + type + "-" + defenceLevel;
    }

    @Override public int getDefenceLevel() {
        return defenceLevel;
    }

    @Override public AttackType getDefenceType() {
        return type;
    }
}
