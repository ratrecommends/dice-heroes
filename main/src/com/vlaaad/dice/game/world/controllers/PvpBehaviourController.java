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

import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.Behaviour;
import com.vlaaad.dice.game.world.behaviours.BehaviourBuilder;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityAbilityParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.processors.pvp.PvpOtherProcessor;
import com.vlaaad.dice.game.world.behaviours.processors.pvp.PvpUserWrapper;
import com.vlaaad.dice.game.world.behaviours.processors.user.*;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.states.PvpPlayState;

/**
 * Created 29.07.14 by vlaaad
 */
public class PvpBehaviourController extends BehaviourController {

    private final PvpPlayState state;
    private Player me;
    private Object others = new Object();

    public PvpBehaviourController(World world, PvpPlayState state) {
        super(world);
        this.state = state;
    }

    @Override protected void initBehaviours() {
        me = state.participantsToPlayers.get(state.session.getMe());
        registerBehaviour(me, new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(
                    BehaviourRequest.TURN,
                    new PvpUserWrapper<TurnResponse, TurnParams>(new UserTurnProcessor(), state)
                );
                behaviour.registerProcessor(
                    BehaviourRequest.CREATURE,
                    new PvpUserWrapper<Creature, AbilityCreatureParams>(new UserAbilityCreatureProcessor(), state)
                );
                behaviour.registerProcessor(
                    BehaviourRequest.CREATURE,
                    new PvpUserWrapper<Creature, AbilityCreatureParams>(new UserAbilityResurrectProcessor(), state)
                );
                behaviour.registerProcessor(
                    BehaviourRequest.COORDINATE,
                    new PvpUserWrapper<Grid2D.Coordinate, AbilityCoordinatesParams>(new UserAbilityCoordinateProcessor(), state)
                );
                behaviour.registerProcessor(
                    BehaviourRequest.ABILITY,
                    new PvpUserWrapper<Ability, AbilityAbilityParams>(new UserAbilityAbilityProcessor(), state)
                );
            }
        });

        registerBehaviour(others, new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(
                    BehaviourRequest.TURN,
                    new PvpOtherProcessor<TurnResponse, TurnParams>(state)
                );
                behaviour.registerProcessor(BehaviourRequest.CREATURE,
                    new PvpOtherProcessor<Creature, AbilityCreatureParams>(state)
                );
                behaviour.registerProcessor(BehaviourRequest.COORDINATE,
                    new PvpOtherProcessor<Grid2D.Coordinate, AbilityCoordinatesParams>(state)
                );
                behaviour.registerProcessor(
                    BehaviourRequest.ABILITY,
                    new PvpOtherProcessor<Ability, AbilityAbilityParams>(state)
                );
            }
        });
    }

    @Override protected Behaviour getBehaviour(Creature creature) {
        if(creature.player == me)
            return behaviours.get(me);
        return behaviours.get(others);
    }
}
