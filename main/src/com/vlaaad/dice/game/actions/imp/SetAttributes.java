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

package com.vlaaad.dice.game.actions.imp;

import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.AddEffect;
import com.vlaaad.dice.game.actions.results.imp.SequenceResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.attributes.modifiers.AttributeModifier;
import com.vlaaad.dice.game.config.attributes.modifiers.imp.Set;
import com.vlaaad.dice.game.effects.ModifierEffect;
import com.vlaaad.dice.game.effects.ModifiersEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

import java.util.HashMap;
import java.util.Map;

public class SetAttributes extends CreatureAction {

    private int turns;
    private HashMap<Attribute, AttributeModifier> attrs;

    public SetAttributes(Ability owner) {
        super(owner);
    }

    @SuppressWarnings("unchecked")
    @Override protected void doInit(Object setup) {
        Map map = (Map) setup;
        attrs = new HashMap<Attribute, AttributeModifier>();
        Map data = MapHelper.get(map, "attributes");
        for (String attrName : MapHelper.keys(data)) {
            attrs.put(Attribute.valueOf(attrName), new Set(data.get(attrName), 0));
        }
        turns = MapHelper.get(map, "turns", Numbers.ONE).intValue();
    }

    @SuppressWarnings("unchecked")
    @Override public IFuture<? extends IActionResult> apply(Creature creature, World world) {
        return Future.completed(new AddEffect(owner, creature, new ModifiersEffect(owner, attrs, turns)));
    }
}
