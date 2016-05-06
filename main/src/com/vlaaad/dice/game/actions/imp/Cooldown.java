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
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.effects.CooldownEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

/**
 * Created 28.01.14 by vlaaad
 */
public class Cooldown extends CreatureAction {
    private String abilityName;
    private int turnCount;

    public Cooldown(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map data = (Map) setup;
        abilityName = MapHelper.get(data, "ability");
        turnCount = MapHelper.get(data, "turns", Numbers.ONE).intValue();
    }

    @Override public IFuture<IActionResult> apply(Creature creature, World world) {
        return Future.completed(createResult(creature));
    }

    @Override public void fillDescriptionParams(Thesaurus.Params params, Creature creature) {
        params.with("cooldown", String.valueOf(turnCount));
    }

    private IActionResult createResult(Creature creature) {
        return new AddEffect(
            owner,
            creature,
            new CooldownEffect(abilityName, turnCount, owner)
        );
    }
}
