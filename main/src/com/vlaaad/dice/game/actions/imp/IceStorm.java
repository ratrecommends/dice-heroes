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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.util.Function;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.IceStormResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.World;

import java.util.Map;

public class IceStorm extends CreatureAction {

    private static final Vector2 tmp = new Vector2();

    private float radius;
    private int turns;
    private int epicenterTurns;

    public IceStorm(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        Map data = (Map) setup;
        radius = MapHelper.get(data, "radius", Numbers.ONE).floatValue();
        turns = MapHelper.get(data, "turns", Numbers.ONE).intValue();
        epicenterTurns = MapHelper.get(data, "epicenterTurns", Numbers.ONE).intValue();
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, World world) {
        int level = 5;
        Vector2 position = tmp.set(creature.getX(), creature.getY());
        Array<Grid2D.Coordinate> available = new Array<Grid2D.Coordinate>();
        for (int i = creature.getX() - level; i <= creature.getX() + level; i++) {
            for (int j = creature.getY() - level; j <= creature.getY() + level; j++) {
                if (position.dst(i, j) <= level && world.level.exists(LevelElementType.tile, i, j)) {
                    available.add(new Grid2D.Coordinate(i, j));
                }
            }
        }
        return withCoordinate(creature, available, new Function<Grid2D.Coordinate, IFuture<? extends IActionResult>>() {
            @Override public IFuture<? extends IActionResult> apply(Grid2D.Coordinate coordinate) {
                return Future.completed(calcResult(creature, coordinate));
            }
        });
    }

    private IActionResult calcResult(Creature creature, Grid2D.Coordinate cell) {
        Vector2 position = tmp.set(cell.x(), cell.y());
        ObjectIntMap<Creature> targets = new ObjectIntMap<Creature>();
        for (int i = cell.x() - MathUtils.ceil(radius); i <= cell.x() + radius; i++) {
            for (int j = cell.y() - MathUtils.ceil(radius); j <= cell.y() + radius; j++) {
                if (position.dst(i, j) <= radius) {
                    WorldObject object = creature.world.get(i, j);
                    if (object instanceof Creature
                        && ((Creature) object).get(Attribute.canBeSelected)
                        && !((Creature) object).get(Attribute.frozen)) {
                        targets.put((Creature) object, i == cell.x() && j == cell.y() ? epicenterTurns : turns);
                    }
                }
            }
        }

        return new IceStormResult(owner, creature, cell, targets);
    }
}
