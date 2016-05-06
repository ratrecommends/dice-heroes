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
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.RoundController;
import com.vlaaad.dice.states.PvePlayState;

/**
 * Created 17.11.13 by vlaaad
 */
public class SetCurrentRolled extends TutorialTask {
    private final String abilityName;

    public SetCurrentRolled(String abilityName) {
        super();
        this.abilityName = abilityName;
    }

    @Override public void start(Callback callback) {
        PvePlayState playState = resources.get("playState");
        Creature current = playState.world.getController(RoundController.class).getCurrentCreature();
        Ability ability = Config.abilities.get(abilityName);
        int idx = current.abilities.indexOf(ability, true);
        if (idx == -1)
            throw new IllegalStateException("no such ability in current creature");
        for (int i = 0; i < current.probabilities.size; i++) {
            current.probabilities.set(i, idx == i ? 1 : 0);
        }
        callback.taskEnded();
    }
}
