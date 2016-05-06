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
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.ViewController;

public class ApplyAbility extends TutorialTask {
    private final String creatureName;
    private final Ability ability;

    public ApplyAbility(String creatureName, String abilityName) {
        this.creatureName = creatureName;
        this.ability = Config.abilities.get(abilityName);
    }

    @Override public void start(final Callback callback) {
        final World world = resources.get("world");
        final Creature creature = world.getCreatureByName(creatureName);
        ability.action.apply(creature, world).addListener(new IFutureListener<IActionResult>() {
            @Override public void onHappened(final IActionResult result) {
                world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
                    @Override public void onHappened(Void aVoid) {
                        result.apply(world);
                        callback.taskEnded();
                    }
                });
            }
        });
    }
}
