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

package com.vlaaad.dice.game.objects;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.ArrayMap;
import com.vlaaad.common.util.Function;
import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.StateDispatcher;
import com.vlaaad.dice.game.config.CreatureRequirementFactory;
import com.vlaaad.dice.game.requirements.DieRequirement;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.events.MoveEvent;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.util.PlayerColors;
import com.vlaaad.dice.game.world.view.StepDetectorSubView;
import com.vlaaad.dice.game.world.view.SubView;
import com.vlaaad.dice.game.world.view.WorldObjectView;

public class StepDetector extends WorldObject {

    public static final EventType<StepDetector> ACTIVATE = new EventType<StepDetector>();
    public static final EventType<StepDetector> DEACTIVATE = new EventType<StepDetector>();
    private static final Function<ViewController, Group> STEP_DETECTOR_LAYER = new Function<ViewController, Group>() {
        @Override public Group apply(ViewController viewController) {
            return viewController.stepDetectorLayer;
        }
    };

    public final DieRequirement requirement;

    public final StateDispatcher<Boolean> activeState = new StateDispatcher<Boolean>(false);

    public StepDetector(String worldObjectName, DieRequirement requirement) {
        super(worldObjectName);
        this.requirement = requirement;
    }

    @Override public void onAdded() {
        world.dispatcher.add(World.MOVE_WORLD_OBJECT, onMoved);
        world.dispatcher.add(World.ADD_WORLD_OBJECT, onAdded);
        world.dispatcher.add(World.REMOVE_WORLD_OBJECT, onRemoved);
        activeState.setState(isActive());
        if (isActive()) world.dispatcher.dispatch(ACTIVATE, this);
    }

    @Override public void onRemoved() {
        world.dispatcher.remove(World.MOVE_WORLD_OBJECT, onMoved);
        world.dispatcher.remove(World.ADD_WORLD_OBJECT, onAdded);
        world.dispatcher.remove(World.REMOVE_WORLD_OBJECT, onRemoved);
        activeState.setState(false);
        if (isActive()) world.dispatcher.dispatch(DEACTIVATE, this);
    }

    @Override public ArrayMap<Object, SubView> createSubViews(Player viewer, PlayerColors colors) {
        ArrayMap<Object, SubView> result = new ArrayMap<Object, SubView>();
        result.put(this, new StepDetectorSubView(this));
        return result;
    }

    @Override public void initView(WorldObjectView view) {
        view.layerSelector = STEP_DETECTOR_LAYER;
    }

    @Override public boolean isPassable() {
        return true;
    }

    private final EventListener<MoveEvent> onMoved = new EventListener<MoveEvent>() {
        @Override public void handle(EventType<MoveEvent> type, MoveEvent event) {
            if (!(event.object instanceof Creature))
                return;
            final Creature creature = (Creature) event.object;
            if (!requirement.isSatisfied(creature.description))
                return;
            final int x = getX();
            final int y = getY();
            final int cx = creature.getX();
            final int cy = creature.getY();
            final int px = event.prevX;
            final int py = event.prevY;
            if (px == x && py == y && (cx != x || cy != y)) {
                activeState.setState(false);
                world.dispatcher.dispatch(DEACTIVATE, StepDetector.this);
            } else if (cx == x && cy == y && (px != x || py != y)) {
                activeState.setState(true);
                world.dispatcher.dispatch(ACTIVATE, StepDetector.this);
            }
        }
    };
    private final EventListener<WorldObject> onAdded = new EventListener<WorldObject>() {
        @Override public void handle(EventType<WorldObject> type, WorldObject worldObject) {
            if (!(worldObject instanceof Creature))
                return;
            final Creature creature = (Creature) worldObject;
            if (creature.getX() == getX() && creature.getY() == getY() && requirement.isSatisfied(creature.description)) {
                activeState.setState(true);
                world.dispatcher.dispatch(ACTIVATE, StepDetector.this);
            }
        }
    };
    private final EventListener<WorldObject> onRemoved = new EventListener<WorldObject>() {
        @Override public void handle(EventType<WorldObject> type, WorldObject worldObject) {
            if (!(worldObject instanceof Creature))
                return;
            final Creature creature = (Creature) worldObject;
            if (creature.getX() == getX() && creature.getY() == getY() && requirement.isSatisfied(creature.description)) {
                activeState.setState(false);
                world.dispatcher.dispatch(DEACTIVATE, StepDetector.this);
            }
        }
    };

    public boolean isActive() {
        if (world == null) return false;
        final WorldObject worldObject = world.get(getX(), getY());
        if (!(worldObject instanceof Creature)) return false;
        final Creature creature = (Creature) worldObject;
        return requirement.isSatisfied(creature.description);
    }

}
