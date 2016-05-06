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

package com.vlaaad.dice.game.config.attributes;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.actions.AttackType;
import com.vlaaad.dice.game.actions.imp.Potion;
import com.vlaaad.dice.game.effects.CooldownEffect;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created 08.10.13 by vlaaad
 */
public final class Attribute<T> {
    public static final String ATTACK_SUFFIX = "-attack";
    public static final String DEFENCE_SUFFIX = "-defence";

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> valueOf(String name) {
        return attributes.get(name);
    }

    @SuppressWarnings("unchecked")
    private static <T> Attribute<T> getOrCreate(String name, T defaultValue) {
        Attribute attribute = attributes.get(name);
        if (attribute == null) {
            attribute = new Attribute<T>(name, defaultValue);
        }
        return attribute;
    }

    public static Attribute<CooldownEffect> cooldownFor(String abilityName) {
        return getOrCreate("cooldown-" + abilityName, null);
    }

    public static Attribute<Integer> defenceFor(AttackType attackType) {
        return valueOf(attackType + DEFENCE_SUFFIX);
    }

    public static Attribute<Integer> attackFor(AttackType attackType) {
        return valueOf(attackType + ATTACK_SUFFIX);
    }

    private static final ObjectMap<String, Attribute> attributes = new ObjectMap<String, Attribute>();

    static {
        for (AttackType attackType : AttackType.values()) {
            new Attribute<Integer>(attackType + ATTACK_SUFFIX, 0);
            new Attribute<Integer>(attackType + DEFENCE_SUFFIX, 0);
        }
    }

    public static final Attribute<Boolean> frozen = new Attribute<Boolean>("frozen", FALSE);
    public static final Attribute<Boolean> transformedToObstacle = new Attribute<Boolean>("transformedToObstacle", FALSE);
    public static final Attribute<Boolean> shortOfBreath = new Attribute<Boolean>("shortOfBreath", FALSE);
    public static final Attribute<Boolean> canBeResurrected = new Attribute<Boolean>("canBeResurrected", TRUE);
    public static final Attribute<Boolean> poisoned = new Attribute<Boolean>("poisoned", FALSE);
    public static final Attribute<Boolean> canUsePotions = new Attribute<Boolean>("potions", TRUE);
    public static final Attribute<Potion.ActionType> potionAction = new Attribute<Potion.ActionType>("potionAction", null);
    public static final Attribute<Boolean> canMove = new Attribute<Boolean>("canMove", TRUE);
    public static final Attribute<Boolean> canBeSelected = new Attribute<Boolean>("canBeSelected", TRUE);
    public static final Attribute<Integer> actionPoints = new Attribute<Integer>("action-points", 1);
    public static final Attribute<Integer> defaultActionPoints = new Attribute<Integer>("default-action-points", 1);
    public static final Attribute<Boolean> canUseProfessionAbilities = new Attribute<Boolean>("can-use-profession-abilities", TRUE);
    public static final Attribute<Boolean> canBeEnthralled = new Attribute<Boolean>("can-be-enthralled", TRUE);

    public final String name;
    public final T defaultValue;

    public Attribute(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        attributes.put(name, this);
    }

    @Override public String toString() {
        return name;
    }
}
