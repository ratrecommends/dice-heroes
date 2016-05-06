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

import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.Behaviour;
import com.vlaaad.dice.game.world.behaviours.BehaviourBuilder;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.processors.ai.*;
import com.vlaaad.dice.game.world.behaviours.processors.random.RandomAbilityProcessor;
import com.vlaaad.dice.game.world.behaviours.processors.random.RandomCoordinateProcessor;
import com.vlaaad.dice.game.world.behaviours.processors.random.RandomCreatureProcessor;
import com.vlaaad.dice.game.world.behaviours.processors.user.*;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;

/**
 * Created 29.07.14 by vlaaad
 */
public class PveBehaviourController extends BehaviourController {
    public PveBehaviourController(World world) {
        super(world);
    }
    @Override protected void initBehaviours() {
        registerBehaviour(PlayerHelper.protagonist, new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new UserTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new UserAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new UserAbilityResurrectProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new UserAbilityCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.ABILITY, new UserAbilityAbilityProcessor());
            }
        });

        registerBehaviour(PlayerHelper.antagonist, new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiDefaultTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiDefaultAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiAttackBehaviour());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiDefaultAbilityCoordinateProcessor());
            }
        });

        registerBehaviour(Config.professions.get("warrior"), new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiWarriorTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new SpeedDefenceProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new BerserkProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiDefaultAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiAttackBehaviour());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiWarriorAbilityCoordinateProcessor());
            }
        });

        registerBehaviour(Config.professions.get("archer"), new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiArcherTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new TransformToObstacleProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new PoisonDartProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiDefaultAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiAttackBehaviour());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiShotBehaviour());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiArcherAbilityCoordinateProcessor());
            }
        });

        registerBehaviour(Config.professions.get("mage"), new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiMageTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new ConcentrationProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new SummonProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiMostCostlyAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiAttackBehaviour());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiFireballProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiMageAbilityCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiFirestormCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.ABILITY, new MostCostlyAbilityProcessor());
            }
        });

        registerBehaviour(Config.professions.get("cleric"), new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiClericTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new TeleportProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new ResurrectionProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiMostCostlyAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiClericDefenceProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiClericAbilityCoordinateProcessor());
            }
        });

        registerBehaviour(Config.professions.get("shaman"), new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiShamanTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiShamanHeavyDefenceProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiShamanEnthrallmentProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiMostCostlyAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiShamansAbilityCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new StaffOfTeleportationCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new StaffOfTeleportationCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new PoisonousDustProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new EeryMaskProcessor());
            }
        });

        registerBehaviour(Config.professions.get("boss"), new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new RandomCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new RandomCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.ABILITY, new RandomAbilityProcessor());
                behaviour.registerProcessor(BehaviourRequest.TURN, new BossTurnProcessor());
            }
        });

        registerBehaviour(Config.professions.get("dragon"), new BehaviourBuilder() {
            @Override protected void setup(Behaviour behaviour) {
                behaviour.registerProcessor(BehaviourRequest.TURN, new AiDragonTurnProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiMostCostlyAbilityCreatureProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiAttackBehaviour());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiWarriorAbilityCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiFirestormCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new AiIceStormCoordinateProcessor());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiShotBehaviour());
                behaviour.registerProcessor(BehaviourRequest.CREATURE, new AiFireballProcessor());
                behaviour.registerProcessor(BehaviourRequest.COORDINATE, new PoisonousDustProcessor());
            }
        });
    }
    @Override protected Behaviour getBehaviour(Creature creature) {
        if (creature.player.fraction == PlayerHelper.protagonist) {
            return behaviours.get(PlayerHelper.protagonist);
        } else if (creature.player.fraction == PlayerHelper.antagonist) {
            Behaviour behaviour = behaviours.get(creature.profession);
            if (behaviour == null) {
                behaviour = behaviours.get(PlayerHelper.antagonist);
            }
            return behaviour;
        }
        throw new IllegalStateException("unknown fraction: " + creature.player.fraction);
    }
}
