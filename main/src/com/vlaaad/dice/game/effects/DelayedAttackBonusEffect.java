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
import com.vlaaad.dice.game.config.attributes.modifiers.imp.AddUntilMax;
import com.vlaaad.dice.game.config.attributes.modifiers.imp.Max;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 22.01.14 by vlaaad
 */
public class DelayedAttackBonusEffect extends CreatureEffect {
    private final AddUntilMax max;
    private final AttackType type;
    private final int level;

    public DelayedAttackBonusEffect(Ability ability, AttackType type, int level) {
        super(ability, ability.name + "-effect", 0);
        this.type = type;
        this.level = level;
        this.max = new AddUntilMax(1, level, -1000);
    }

    @Override public int getTurnCount() { return 1; }

    @Override public boolean shouldEnd() { return false; }

    @Override public void apply(Creature creature) {
        creature.addModifier(Attribute.attackFor(type), max);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.removeModifier(Attribute.attackFor(type), max);
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/area-attack-" + level;
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-area-attack-" + level;
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-area-attack-" + level;
    }
}
