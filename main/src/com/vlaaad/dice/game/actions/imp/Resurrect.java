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

package com.vlaaad.dice.game.actions.imp;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.ResurrectResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;
import com.vlaaad.dice.game.world.controllers.RoundController;

/**
 * Created 06.02.14 by vlaaad
 */
public class Resurrect extends CreatureAction {

    public Resurrect(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {}

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        return super.canBeApplied(creature, reasonData) && hasCreaturesToResurrect(creature, reasonData) && hasPlaceToResurrect(creature, reasonData);
    }

    private boolean hasPlaceToResurrect(Creature creature, Thesaurus.LocalizationData reasonData) {
        if (creature.world == null) {
            reasonData.key = "creature-is-not-on-map";
            reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
            return false;
        }
        int x = creature.getX();
        int y = creature.getY();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (creature.world.canStepTo(i, j))
                    return true;
            }
        }
        reasonData.key = "can-not-place-resurrected";
        reasonData.params = new Thesaurus.Params().with("die", creature.description.nameLocKey());
        return false;
    }

    private boolean hasCreaturesToResurrect(Creature creature, Thesaurus.LocalizationData reasonData) {
        Array<Creature> dead = creature.world.getController(RoundController.class).killed;
        for (Creature c : dead) {
            if (!c.get(Attribute.canBeResurrected))
                continue;
            if (c.player != creature.player)
                continue;
            return true;
        }
        reasonData.key = "nobody-to-resurrect";
        return false;
    }

    @Override public IFuture<? extends IActionResult> apply(final Creature creature, World world) {
        Array<Creature> dead = creature.world.getController(RoundController.class).killed;
        final Array<Creature> availableCreatures = new Array<Creature>();
        for (Creature c : dead) {
            if (!c.get(Attribute.canBeResurrected))
                continue;
            if (c.player != creature.player)
                continue;
            availableCreatures.add(c);
        }
        if (availableCreatures.size == 0)
            return Future.completed(IActionResult.NOTHING);
        final Array<Grid2D.Coordinate> availableCoordinates = new Array<Grid2D.Coordinate>();
        int x = creature.getX();
        int y = creature.getY();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (creature.world.canStepTo(i, j))
                    availableCoordinates.add(new Grid2D.Coordinate(i, j));
            }
        }
        if (availableCoordinates.size == 0)
            return Future.completed(IActionResult.NOTHING);

        if (availableCreatures.size == 1) {
            if (availableCoordinates.size == 1) {
                return Future.completed(calcResult(creature, availableCreatures.first(), availableCoordinates.first()));
            } else {
                final Future<IActionResult> future = new Future<IActionResult>();
                creature.world.getController(BehaviourController.class)
                    .get(creature)
                    .request(BehaviourRequest.COORDINATE, new AbilityCoordinatesParams(creature, owner, availableCoordinates))
                    .addListener(new IFutureListener<Grid2D.Coordinate>() {
                        @Override public void onHappened(Grid2D.Coordinate coordinate) {
                            future.happen(calcResult(creature, availableCreatures.first(), coordinate));
                        }
                    });
                return future;
            }
        } else {
            //a lot of creatures
            if (availableCoordinates.size == 1) {
                //select creature, happen
                final Future<IActionResult> future = new Future<IActionResult>();
                creature.world.getController(BehaviourController.class)
                    .get(creature)
                    .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, availableCreatures))
                    .addListener(new IFutureListener<Creature>() {
                        @Override public void onHappened(Creature selected) {
                            future.happen(calcResult(creature, selected, availableCoordinates.first()));
                        }
                    });
                return future;
            } else {
                // select creature, select coordinate, happen
                final Future<IActionResult> future = new Future<IActionResult>();
                creature.world.getController(BehaviourController.class)
                    .get(creature)
                    .request(BehaviourRequest.CREATURE, new AbilityCreatureParams(creature, owner, availableCreatures))
                    .addListener(new IFutureListener<Creature>() {
                        @Override public void onHappened(Creature selected) {
                            final Creature toResurrect = selected;
                            creature.world.getController(BehaviourController.class)
                                .get(creature)
                                .request(BehaviourRequest.COORDINATE, new AbilityCoordinatesParams(creature, owner, availableCoordinates))
                                .addListener(new IFutureListener<Grid2D.Coordinate>() {
                                    @Override public void onHappened(Grid2D.Coordinate coordinate) {
                                        future.happen(calcResult(creature, toResurrect, coordinate));
                                    }
                                });
                        }
                    });
                return future;
            }
        }
    }

    private IActionResult calcResult(Creature creature, Creature toResurrect, Grid2D.Coordinate coordinate) {
        return new ResurrectResult(owner, creature, toResurrect, coordinate);
    }
}
