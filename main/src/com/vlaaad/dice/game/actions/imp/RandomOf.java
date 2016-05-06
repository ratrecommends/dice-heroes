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

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.CreatureActionFactory;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.RandomController;

import java.util.List;
import java.util.Map;

/**
 * Created 17.03.14 by vlaaad
 */
public class RandomOf extends CreatureAction {
    private final Array<CreatureAction> actions = new Array<CreatureAction>();

    public RandomOf(Ability owner) {
        super(owner);
    }

    @SuppressWarnings("unchecked")
    @Override protected void doInit(Object setup) {
        List<Map> actions = (List<Map>) setup;
        for (Map map : actions) {
            this.actions.add(CreatureActionFactory.createFromActionSetup(map, owner));
        }
    }

    @Override public IFuture<? extends IActionResult> apply(Creature creature, World world) {
        return world.getController(RandomController.class).random(actions).apply(creature, world);
    }
}
