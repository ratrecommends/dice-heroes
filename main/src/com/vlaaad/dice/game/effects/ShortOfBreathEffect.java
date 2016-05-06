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
import com.vlaaad.dice.game.config.attributes.modifiers.imp.SetFlag;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 28.01.14 by vlaaad
 */
public class ShortOfBreathEffect extends CreatureEffect {

    private SetFlag setFlag = new SetFlag(true);

    public ShortOfBreathEffect(Ability owner, int time) {
        super(owner, "short-of-breath-" + System.currentTimeMillis() + "-" + Math.random(), time);
    }

    @Override public void apply(Creature creature) {
        creature.addModifier(Attribute.shortOfBreath, setFlag);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.removeModifier(Attribute.shortOfBreath, setFlag);
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/short-of-breath";
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-short-of-breath";
    }

    @Override public String locDescKey() {
        return "die-is-short-of-breath";
    }
}
