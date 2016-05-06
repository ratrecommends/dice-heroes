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

package com.vlaaad.dice.game.actions.results.imp;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.ITargetOwner;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;

/**
 * Created 08.10.13 by vlaaad
 */
public class MoveResult implements ITargetOwner, IActionResult {

    public final Creature creature;
    public final int x;
    public final int y;

    public MoveResult(Creature creature, int x, int y) {
        this.creature = creature;
        this.x = x;
        this.y = y;
    }

    @Override public void apply(World world) {
        world.move(creature, x, y);
    }

    public static Array<Grid2D.Coordinate> fillAvailableCoordinates(Array<Grid2D.Coordinate> coordinates, Creature creature) {
        World world = creature.world;
        if (world == null)
            return coordinates;
        if (!creature.get(Attribute.canMove)) {
            coordinates.add(new Grid2D.Coordinate(creature.getX(), creature.getY()));
            return coordinates;
        }
        int r = MathUtils.ceil(creature.description.profession.moveRadius);
        float r2 = creature.description.profession.moveRadius * creature.description.profession.moveRadius;
        for (int x = creature.getX() - r; x <= creature.getX() + r; x++) {
            for (int y = creature.getY() - r; y <= creature.getY() + r; y++) {
                if ((x == creature.getX() && y == creature.getY() || world.canStepTo(x, y))
                    && tmp.set(x, y).dst2(creature.getX(), creature.getY()) <= r2) {
                    coordinates.add(new Grid2D.Coordinate(x, y));
                }
            }
        }
        return coordinates;
    }

    public static Array<Grid2D.Coordinate> getAvailableCoordinates(Creature creature) {
        return fillAvailableCoordinates(new Array<Grid2D.Coordinate>(), creature);
    }

    private static final Vector2 tmp = new Vector2();

    @Override public Creature getTarget() {
        return creature;
    }
}
