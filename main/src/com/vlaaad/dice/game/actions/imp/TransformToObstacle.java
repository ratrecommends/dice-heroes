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
import com.vlaaad.dice.game.actions.results.imp.TransformToObstacleResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

/**
 * Created 02.02.14 by vlaaad
 */
public class TransformToObstacle extends CreatureAction {

    private int turnCount;
    private String obstacle;

    public TransformToObstacle(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        //{name: archer-bush, turns: 4}
        Map data = (Map) setup;
        turnCount = MapHelper.get(data, "turns", Numbers.ONE).intValue();
        obstacle = MapHelper.get(data, "name", "bush");
    }

    @Override public IFuture<IActionResult> apply(Creature creature, World world) {
        return Future.completed(createResult(creature));
    }

    private IActionResult createResult(Creature creature) {
        return new TransformToObstacleResult(creature, creature, owner, obstacle, turnCount);
    }
}
