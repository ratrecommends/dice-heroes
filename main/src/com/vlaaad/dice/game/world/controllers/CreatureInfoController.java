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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.ui.windows.CreatureInfoWindow;

/**
 * Created 12.10.13 by vlaaad
 */
public class CreatureInfoController extends WorldController {

    private final CreatureInfoWindow creatureInfoWindow = new CreatureInfoWindow();

    public CreatureInfoController(World world) {
        super(world);
    }

    @Override protected void start() {
        world.getController(ViewController.class).root.addListener(listener);
    }

    @Override protected void stop() {
        world.getController(ViewController.class).root.removeListener(listener);
    }

    private final EventListener listener = new ActorGestureListener(20, 0.4f, 0.5f, 0.15f) {
        public boolean windowShownAfterLongPress;

        @Override public boolean longPress(Actor actor, float x, float y) {
            Vector2 stageCoordinates = world.getController(ViewController.class).root.localToStageCoordinates(new Vector2(x, y));
            windowShownAfterLongPress = show(stageCoordinates.x, stageCoordinates.y, true);
            return windowShownAfterLongPress;
        }

        @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (windowShownAfterLongPress) {
                event.cancel();
                windowShownAfterLongPress = false;
            }
        }

        private boolean show(float stageX, float stageY, boolean isLongPress) {
            Vector2 c = world.getController(ViewController.class).stageToWorldCoordinates(stageX, stageY);
            WorldObject object = world.get(c.x, c.y);
            if (object instanceof Creature) {
                Creature creature = (Creature) object;
                if (!isLongPress && world.getController(RoundController.class).getCurrentCreature() == creature) {
                    return false;
                }
                showCreatureWindow(creature);
                return true;
            }
            return false;
        }
    };

    private void showCreatureWindow(Creature creature) {
        creatureInfoWindow.show(new CreatureInfoWindow.Params(creature, world));
    }
}
