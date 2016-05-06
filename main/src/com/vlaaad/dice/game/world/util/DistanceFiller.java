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

package com.vlaaad.dice.game.world.util;

import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.World;

/**
 * Created 19.11.13 by vlaaad
 */
public class DistanceFiller {

    private static final ObjectSet<Coordinate> tmp = new ObjectSet<Coordinate>();
    private static final ObjectSet<Coordinate> tmp2 = new ObjectSet<Coordinate>();
    private static final ObjectSet<Coordinate> tmp3 = new ObjectSet<Coordinate>();
    private static final Result result = new Result();

    public static int getDistanceToNearCreatureOfRelation(World world, int x, int y, Creature creature, Creature.CreatureRelation relation) {
        if (!world.inBounds(x, y))
            return -1;
        ObjectSet<Coordinate> checked = tmp;
        ObjectSet<Coordinate> toCheck = tmp2;
        addNeighbours(world, x, y, checked, toCheck);
        return recursive(world, creature, relation, checked, toCheck, 1).distance;
    }


    public static Result getInfoOfNearCreatureOfRelation(World world, int x, int y, Creature creature, Creature.CreatureRelation relation) {
        if (!world.inBounds(x, y))
            return null;
        ObjectSet<Coordinate> checked = tmp;
        ObjectSet<Coordinate> toCheck = tmp2;
        addNeighbours(world, x, y, checked, toCheck);
        recursive(world, creature, relation, checked, toCheck, 1);
        return result;
    }

    public static Coordinate getCoordinateOfNearCreatureOfRelation(World world, int x, int y, Creature creature, Creature.CreatureRelation relation) {
        if (!world.inBounds(x, y))
            return null;
        ObjectSet<Coordinate> checked = tmp;
        ObjectSet<Coordinate> toCheck = tmp2;
        addNeighbours(world, x, y, checked, toCheck);
        recursive(world, creature, relation, checked, toCheck, 1);
        return result.distance == -1 ? null : Coordinate.obtain(result.position.x, result.position.y);
    }


    public static int getDistance(Creature from, Creature to) {
        int x = from.getX();
        int y = from.getY();
        World world = from.world;
        if (!world.inBounds(x, y))
            return -1;
        ObjectSet<Coordinate> checked = tmp;
        ObjectSet<Coordinate> toCheck = tmp2;
        addNeighbours(world, x, y, checked, toCheck);
        return recursive(world, to, checked, toCheck, 1).distance;
    }

    private static Result recursive(World world, Creature creature , Creature.CreatureRelation relation, ObjectSet<Coordinate> checked, ObjectSet<Coordinate> toCheck, int depth) {
        if (toCheck.size == 0) {
            cleanUp();

            return result.set(-1, -1, -1);
        }
        for (Coordinate coordinate : toCheck) {
            checked.add(coordinate);
            WorldObject object = world.get(coordinate.x, coordinate.y);
            if (object instanceof Creature) {
                Creature check = (Creature) object;
                if (creature.inRelation(relation, check)) {
                    cleanUp();
                    return result.set(depth, coordinate.x, coordinate.y);
                }
            }
        }
        ObjectSet<Coordinate> toFill = tmp3;
        toFill.clear();
        toFill.addAll(toCheck);
        toCheck.clear();

        for (Coordinate coordinate : toFill) {
            addNeighbours(world, coordinate.x, coordinate.y, checked, toCheck);
        }
        return recursive(world, creature, relation, checked, toCheck, depth + 1);
    }

    private static void cleanUp() {
        Coordinate.freeAll(tmp);
        Coordinate.freeAll(tmp2);
        Coordinate.freeAll(tmp3);
        tmp.clear();
        tmp2.clear();
        tmp3.clear();

    }

    private static void addNeighbours(World world, int x, int y, ObjectSet<Coordinate> checked, ObjectSet<Coordinate> toCheck) {
        addNeighbour(world, x - 1, y - 1, checked, toCheck);
        addNeighbour(world, x - 1, y, checked, toCheck);
        addNeighbour(world, x - 1, y + 1, checked, toCheck);

        addNeighbour(world, x, y - 1, checked, toCheck);
        addNeighbour(world, x, y + 1, checked, toCheck);

        addNeighbour(world, x + 1, y - 1, checked, toCheck);
        addNeighbour(world, x + 1, y, checked, toCheck);
        addNeighbour(world, x + 1, y + 1, checked, toCheck);
    }

    private static void addNeighbour(World world, int x, int y, ObjectSet<Coordinate> checked, ObjectSet<Coordinate> toCheck) {
        if (!world.inBounds(x, y) || !world.level.exists(LevelElementType.tile, x, y))
            return;
        Coordinate coordinate = Coordinate.obtain(x, y);
        if (checked.contains(coordinate)) {
            coordinate.free();
            return;
        }
        toCheck.add(coordinate);

    }

    private static Result recursive(World world, Creature target, ObjectSet<Coordinate> checked, ObjectSet<Coordinate> toCheck, int depth) {
        if (toCheck.size == 0) {
            cleanUp();

            return result.set(-1, -1, -1);
        }
        for (Coordinate coordinate : toCheck) {
            checked.add(coordinate);
            WorldObject object = world.get(coordinate.x, coordinate.y);
            if (object instanceof Creature) {
                Creature creature = (Creature) object;
                if (creature == target) {
                    cleanUp();
                    return result.set(depth, coordinate.x, coordinate.y);
                }
            }
        }
        ObjectSet<Coordinate> toFill = tmp3;
        toFill.clear();
        toFill.addAll(toCheck);
        toCheck.clear();

        for (Coordinate coordinate : toFill) {
            addNeighbours(world, coordinate.x, coordinate.y, checked, toCheck);
        }
        return recursive(world, target, checked, toCheck, depth + 1);
    }

    public static class Result {
        public int distance;
        public final Coordinate position = Coordinate.obtain(0, 0);

        public Result set(int distance, int x, int y) {
            this.distance = distance;
            this.position.set(x, y);
            return this;
        }
    }

}
