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
import com.vlaaad.dice.DiceHeroes;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.RoundController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.states.PvePlayState;

import java.util.Comparator;

/**
 * Created 15.11.13 by vlaaad
 */
public class InitPlayState extends TutorialTask {
    @Override public void start(Callback callback) {
        DiceHeroes diceHeroes = resources.get("app");
        final PvePlayState playState = (PvePlayState) diceHeroes.getState();
        resources.put("stage", playState.stage);
        resources.put("playState", playState);
        playState.world.dispatcher.add(RoundController.PRE_START, new EventListener<RoundController>() {
            @Override public void handle(EventType<RoundController> type, RoundController controller) {
                controller.queue.sort(new Comparator<Creature>() {
                    @Override public int compare(Creature o1, Creature o2) {
                        int v1 = o1.player == playState.world.viewer ? -1 : 1;
                        int v2 = o2.player == playState.world.viewer ? 1 : -1;
                        return v1 + v2;
                    }
                });
            }
        });
        callback.taskEnded();
    }
}
