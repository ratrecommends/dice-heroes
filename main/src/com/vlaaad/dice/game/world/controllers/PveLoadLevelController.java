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

package com.vlaaad.dice.game.world.controllers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.Logger;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.Obstacle;
import com.vlaaad.dice.game.objects.StepDetector;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.util.ExpHelper;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.game.world.view.SpawnPoint;
import com.vlaaad.dice.game.world.view.TileInfo;

import java.util.Comparator;
import java.util.Map;

/**
 * Created 07.10.13 by vlaaad
 */
public class PveLoadLevelController extends WorldController {

    public static final EventType<TileInfo> LOAD_TILE = new EventType<TileInfo>();
    public static final EventType<SpawnPoint> ADD_SPAWN_POINT = new EventType<SpawnPoint>();
    public static final EventType<Void> LEVEL_LOADED = new EventType<Void>();

    public PveLoadLevelController(World world) {
        super(world);
    }

    @Override protected void start() {
        LevelDescription description = world.level;
        for (Map.Entry<Grid2D.Coordinate, String> entry : description.getElements(LevelElementType.tile)) {
            world.dispatcher.dispatch(LOAD_TILE, new TileInfo(entry.getKey().x(), entry.getKey().y(), entry.getValue()));
        }
        ObjectMap<Grid2D.Coordinate, Creature> creatures = new ObjectMap<Grid2D.Coordinate, Creature>();
        Array<Creature> dropCreatures = new Array<Creature>();
        ObjectMap<Creature, ObjectIntMap<Item>> drop = new ObjectMap<Creature, ObjectIntMap<Item>>();
        Player antagonist = world.players.get(PlayerHelper.antagonist);
        for (Map.Entry<Grid2D.Coordinate, Die> entry : description.getElements(LevelElementType.enemy)) {
            Creature creature = antagonist.addCreature(entry.getValue());
            creatures.put(entry.getKey(), creature);
        }
        for (Creature creature : creatures.values()) {
            dropCreatures.add(creature);
        }
        final ObjectIntMap<Creature> weights = new ObjectIntMap<Creature>();
        int total = 0;
        for (Creature creature : dropCreatures) {
            int cost = ExpHelper.getTotalCost(creature);
            weights.put(creature, cost);
            total += cost;
        }
        dropCreatures.sort(new Comparator<Creature>() {
            @Override public int compare(Creature o1, Creature o2) {
                return weights.get(o2, 0) - weights.get(o1, 0);
            }
        });
        if (dropCreatures.size > 1) {
            dropCreatures.truncate(Math.max(1, dropCreatures.size * 3 / 5));
        }

        for (Creature creature : dropCreatures) {
            drop.put(creature, new ObjectIntMap<Item>());
        }
//        Logger.debug("drop: " + world.level.drop);
        ObjectIntMap<Item> droppedItems = world.level.drop.roll();
//        Logger.debug("rolled: " + droppedItems);
        for (Item item : droppedItems.keys()) {
            int count = droppedItems.get(item, 1);
            float factor = count / (float) total;
            int distributedCount = 0;
            for (Creature creature : dropCreatures) {
                int creatureItemCount = (int) (weights.get(creature, 0) * factor);
                drop.get(creature).put(item, creatureItemCount);
                distributedCount += creatureItemCount;
            }
            if (distributedCount > count)
                throw new IllegalStateException("OMG! distributed " + item + " more than should! drop: " + drop + ", to distribute: " + droppedItems);
            if (distributedCount < count) {
                while (distributedCount < count) {
                    Creature random = dropCreatures.random();
                    drop.get(random).getAndIncrement(item, 0, 1);
//                    Logger.debug("added lost 1 " + item + " to " + random + "!");
                    distributedCount++;
                }
            }
        }
        for (Creature creature : drop.keys()) {
            creature.setDrop(drop.get(creature));
//            Logger.debug("set drop: " + creature + " => " + creature.drop);
        }
        for (Grid2D.Coordinate coordinate : creatures.keys()) {
            world.add(coordinate.x(), coordinate.y(), creatures.get(coordinate));
        }
        for (Map.Entry<Grid2D.Coordinate, Obstacle> entry : description.getElements(LevelElementType.obstacle)) {
            world.add(entry.getKey().x(), entry.getKey().y(), entry.getValue());
        }
        for (Map.Entry<Grid2D.Coordinate, StepDetector> entry : description.getElements(LevelElementType.stepDetector)) {
            world.add(entry.getKey().x(), entry.getKey().y(), entry.getValue());
        }
        for (Map.Entry<Grid2D.Coordinate, Fraction> entry : description.getElements(LevelElementType.spawn)) {
            world.dispatcher.dispatch(ADD_SPAWN_POINT, new SpawnPoint(entry.getKey().x(), entry.getKey().y(), entry.getValue()));
        }
        world.dispatcher.dispatch(LEVEL_LOADED, null);
    }

    @Override protected void stop() {
    }
}
