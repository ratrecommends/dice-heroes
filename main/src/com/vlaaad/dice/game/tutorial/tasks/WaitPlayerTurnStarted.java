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

package com.vlaaad.dice.game.tutorial.tasks;

import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.RoundController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;

/**
 * Created 15.02.14 by vlaaad
 */
public class WaitPlayerTurnStarted extends TutorialTask {
    private EventListener<Creature> listener;

    @Override public void start(final Callback callback) {
        final World world = resources.get("world");
        listener = new EventListener<Creature>() {
            @Override public void handle(EventType<Creature> type, Creature creature) {
                if (creature.player != world.viewer)
                    return;

                world.dispatcher.remove(RoundController.TURN_STARTED, listener);
                callback.taskEnded();
            }
        };
        world.dispatcher.add(RoundController.TURN_STARTED, listener);
    }

    @Override public void cancel() {
        final World world = resources.get("world");
        world.dispatcher.remove(RoundController.TURN_STARTED, listener);
    }
}
