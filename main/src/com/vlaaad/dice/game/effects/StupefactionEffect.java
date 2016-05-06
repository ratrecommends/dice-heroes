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

package com.vlaaad.dice.game.effects;

import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.Behaviour;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.processors.random.RandomAbilityProcessor;
import com.vlaaad.dice.game.world.behaviours.processors.random.RandomCoordinateProcessor;
import com.vlaaad.dice.game.world.behaviours.processors.random.RandomCreatureProcessor;
import com.vlaaad.dice.game.world.behaviours.processors.random.RandomTurnProcessor;
import com.vlaaad.dice.game.world.controllers.BehaviourController;

/**
 * Created 16.03.14 by vlaaad
 */
public class StupefactionEffect extends CreatureEffect {

    private final Behaviour behaviour = new Behaviour();
    private World world;


    public StupefactionEffect(Ability ability, String group, int turns) {
        super(ability, group, turns);
        behaviour.registerProcessor(BehaviourRequest.ABILITY, new RandomAbilityProcessor());
        behaviour.registerProcessor(BehaviourRequest.COORDINATE, new RandomCoordinateProcessor());
        behaviour.registerProcessor(BehaviourRequest.CREATURE, new RandomCreatureProcessor());
        behaviour.registerProcessor(BehaviourRequest.TURN, new RandomTurnProcessor());
    }

    @Override public void apply(Creature creature) {
        world = creature.world;
        creature.world.getController(BehaviourController.class).put(creature, behaviour);
    }

    @Override public IFuture<Void> remove(Creature creature) {
        world.getController(BehaviourController.class).remove(creature);
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/stupefaction";
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-stupefaction";
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-stupefaction";
    }
}
