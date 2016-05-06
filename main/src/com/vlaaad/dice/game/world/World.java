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

package com.vlaaad.dice.game.world;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.Logger;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.events.imp.KillEvent;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.controllers.RoundController;
import com.vlaaad.dice.game.world.events.EventDispatcher;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.events.MoveEvent;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.PlayerRelation;
import com.vlaaad.dice.game.world.players.util.PlayerColors;

import java.lang.reflect.Constructor;
import java.util.Iterator;

/**
 * Created 06.10.13 by vlaaad
 */
public class World implements Iterable<WorldObject> {


    public static final EventType<WorldObject> ADD_WORLD_OBJECT = new EventType<WorldObject>();
    public static final EventType<WorldObject> REMOVE_WORLD_OBJECT = new EventType<WorldObject>();
    public static final EventType<MoveEvent> MOVE_WORLD_OBJECT = new EventType<MoveEvent>();
    public static final EventType<Creature> KILL = new EventType<Creature>();


    private static final Array<Creature> tmp = new Array<Creature>();

    public final float width;
    public final float height;
    public final PlayerColors playerColors;
    public final LevelDescription level;
    public final Stage stage;
    public final EventDispatcher dispatcher = new EventDispatcher();
    private final Grid2D<WorldObject> grid2D = new Grid2D<WorldObject>();

    private final ObjectMap<Class, WorldController> controllers = new ObjectMap<Class, WorldController>();
    public final Player viewer;
    public final ObjectMap<Fraction, Player> players;
    public final ObjectMap<String, Creature> creaturesById = new ObjectMap<String, Creature>();

    private boolean initialized;

    public World(Player viewer, ObjectMap<Fraction, Player> players, PlayerColors playerColors, LevelDescription level, Stage stage) {
        this.viewer = viewer;
        this.players = players;
        this.playerColors = playerColors;
        this.level = level;
        this.stage = stage;
        width = level.width;
        height = level.height;
    }

    public void add(int x, int y, WorldObject value) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw new IllegalArgumentException("position is out of bounds!");
        if (!value.isPassable()) {
            WorldObject prev = grid2D.put(x, y, value);
            if (prev != null)
                throw new IllegalStateException("there already was object " + prev + " at " + x + ", " + y);
        }
        value.world = this;
        value.onAdded();
        dispatcher.dispatch(ADD_WORLD_OBJECT, value);
        value.afterAdded();
    }

    public void move(WorldObject worldObject, int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw new IllegalArgumentException("position is out of bounds!");
        WorldObject prev = grid2D.remove(worldObject.getX(), worldObject.getY());
        if (prev != worldObject)
            throw new IllegalStateException("wtf! wrong removed object!");
        int prevX = worldObject.getX();
        int prevY = worldObject.getY();
        grid2D.put(x, y, worldObject);
        dispatcher.dispatch(MOVE_WORLD_OBJECT, new MoveEvent(worldObject, prevX, prevY));
    }

    public WorldObject get(int x, int y) {
        return grid2D.get(x, y);
    }

    public boolean canStepTo(int x, int y) {
        return inBounds(x, y) && grid2D.get(x, y) == null && level.exists(LevelElementType.tile, x, y);
    }

    public boolean canStepTo(int x, int y, Creature creature) {
        return (x == creature.getX() && y == creature.getY()) || canStepTo(x, y);
    }

    public boolean canStepTo(Grid2D.Coordinate coordinate, Creature target) {
        return canStepTo(coordinate.x(), coordinate.y(), target);
    }

    public WorldObject remove(int x, int y) {
        WorldObject result = grid2D.remove(x, y);
        if (result != null) {
            result.onRemoved();
            dispatcher.dispatch(REMOVE_WORLD_OBJECT, result);
            result.world = null;
        }
        return result;
    }

    public void kill(Creature killer, Creature... creatures) {
        for (Creature creature : creatures) {
            if (!creature.isKilled())
                tmp.add(creature);
        }
        if (tmp.size == 0)
            return;
        for (Creature killed : tmp) {
            if (killer.player.inRelation(killed.player, PlayerRelation.enemy)) {
                killer.player.earn(killed.drop);
                killed.drop.clear();
            }
        }
        Config.achievements.fire(
            com.vlaaad.dice.achievements.events.EventType.kill,
            Pools.obtain(KillEvent.class).killer(killer).killed(creatures)
        );
        for (Creature creature : tmp) {
            creature.onKilled();
            remove(creature);
            dispatcher.dispatch(KILL, creature);
        }
        tmp.clear();
    }

    public void remove(WorldObject object) {
        WorldObject removed = remove(object.getX(), object.getY());
        if (removed != object)
            throw new IllegalStateException("wtf! removed unexpected object! " + removed + " instead of " + object);
    }

    @Override public Iterator<WorldObject> iterator() {
        return grid2D.values().iterator();
    }

    public void addController(Class<? extends WorldController> type) {
        addController(type, type);
    }

    public <T extends WorldController> void addController(T controller) {
        addController(controller, controller.getClass());
    }

    public <I extends WorldController, T extends I> void addController(Class<T> type, Class<? extends I> as) {
        try {
            Constructor<T> s = type.getConstructor(World.class);
            T controller = s.newInstance(this);
            addController(controller, as);
        } catch (Exception e) {
            Logger.error("failed to instantiate controller of type " + type, e);
        }
    }

    public <I extends WorldController, T extends I> void addController(T controller, Class<? extends I> as) {
        controllers.put(as, controller);
        if (initialized) {
            controller.init();
        }
    }

    public void removeController(Class<? extends WorldController> type) {
        WorldController controller = controllers.remove(type);
        if (controller == null)
            return;
        controller.destroy();
    }

    @SuppressWarnings("unchecked")
    public <T extends WorldController> T getController(Class<T> type) {
        return (T) controllers.get(type);
    }

    public void init() {
        if (initialized)
            throw new IllegalStateException("already initialized");
        initialized = true;
        for (WorldController controller : controllers.values()) {
            controller.init();
        }
    }

    public boolean inBounds(float x, float y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public WorldObject get(float x, float y) {
        return get((int) x, (int) y);
    }

    public void onUsePotion(Player player, Ability potion) {
        player.onUsePotion(potion);
    }

    @SuppressWarnings("unchecked")
    public <T extends Creature> Array<? extends T> byType(Class<T> type) {
        Array<T> res = new Array<T>();
        for (WorldObject o : this) {
            if (type.isInstance(o))
                res.add((T) o);
        }
        return res;
    }

    public void destroy() {
        if (!initialized)
            return;
        for (WorldController c : controllers.values()) {
            c.destroy();
        }
        controllers.clear();
        initialized = false;
    }

    public Creature getCreatureByName(String creatureName) {
        creatureName = creatureName.toLowerCase();
        for (Creature creature : getController(RoundController.class).queue) {
            if (creature.description.name.toLowerCase().equals(creatureName))
                return creature;
        }
        throw new IllegalStateException("not found: " + creatureName);
    }
}
