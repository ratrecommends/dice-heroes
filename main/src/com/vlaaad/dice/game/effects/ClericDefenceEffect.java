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
import com.vlaaad.dice.game.config.attributes.modifiers.imp.Max;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 18.01.14 by vlaaad
 */
public class ClericDefenceEffect extends CreatureEffect implements IDefenceEffect{

    public ClericDefenceEffect(Ability ability, AttackType attackType, int defenceLevel) {
        super(ability, "cleric-defence", 1);
        this.attackType = attackType;
        this.defenceLevel = defenceLevel;
        this.max = new Max(defenceLevel, -1000);
    }


    public final AttackType attackType;
    public final int defenceLevel;
    private final Max max;

    @Override public void apply(Creature creature) {
        Attribute<Integer> attribute = Attribute.defenceFor(attackType);
        creature.addModifier(attribute, max);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.removeModifier(Attribute.defenceFor(attackType), max);
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/cleric-defence-" + defenceLevel;
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-cleric-defence-" + defenceLevel;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-cleric-defence-" + defenceLevel;
    }

    @Override public String toString() {
        return "cleric-defence-" + attackType + "-" + defenceLevel;
    }

    @Override public int getDefenceLevel() {
        return defenceLevel;
    }

    @Override public AttackType getDefenceType() {
        return attackType;
    }
}
