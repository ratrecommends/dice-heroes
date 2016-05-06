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
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.MoveResult;
import com.vlaaad.dice.game.actions.results.imp.RollResult;
import com.vlaaad.dice.game.config.CreatureActionFactory;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.effects.ShortOfBreathEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.controllers.BehaviourController;
import com.vlaaad.dice.game.world.controllers.ViewController;

import java.util.Map;

/**
 * Created 24.11.13 by vlaaad
 */
public class Speed extends CreatureAction {
    private CreatureAction action;
    private boolean showTiles;
    private int shortOfBreathTime;

    public Speed(Ability owner) {
        super(owner);
    }

    @Override protected void doInit(Object setup) {
        if (setup != null) {
            Map data = (Map) setup;
            String action = MapHelper.get(data, "action");
            if (action != null) {
                this.action = CreatureActionFactory.create(action, MapHelper.get(data, "setup"), owner);
            }
            showTiles = MapHelper.get(data, "tiles", Boolean.TRUE);
            shortOfBreathTime = MapHelper.get(data, "wheeze", Numbers.ZERO).intValue();
        } else {
            showTiles = true;
        }
    }

    @Override public boolean canBeApplied(Creature creature, Thesaurus.LocalizationData reasonData) {
        boolean result = !creature.get(Attribute.shortOfBreath);
        if (!result) {
            reasonData.key = "die-is-short-of-breath";
            reasonData.params = new Thesaurus.Params();
            reasonData.params.put("die", creature.description.nameLocKey());
        }
        return result;
    }

    @Override public IFuture<IActionResult> apply(final Creature creature, final World world) {
        final Future<IActionResult> future = new Future<IActionResult>();
        if (showTiles && creature.player == world.viewer) {
            Array<Grid2D.Coordinate> coordinates = new Array<Grid2D.Coordinate>();
            MoveResult.fillAvailableCoordinates(coordinates, creature);
            world.getController(ViewController.class).showSelection(Speed.this, coordinates, "selection/move");
        }
        world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.COORDINATE, new AbilityCoordinatesParams(creature, owner, MoveResult.getAvailableCoordinates(creature)))
            .addListener(new IFutureListener<Grid2D.Coordinate>() {
                @Override public void onHappened(Grid2D.Coordinate vector2) {
                    if (showTiles && creature.player == world.viewer) {
                        world.getController(ViewController.class).removeSelection(Speed.this);
                    }
                    if (vector2.x() == creature.getX() && vector2.y() == creature.getY()) {
                        rollOrExecute(creature, world, future);
                    } else {
                        final MoveResult result = new MoveResult(creature, (int) vector2.x(), (int) vector2.y());
                        world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
                            @Override public void onHappened(Void aVoid) {
                                result.apply(world);
                                rollOrExecute(creature, world, future);
                            }
                        });
                    }
                }
            });
        return future;
    }

    private void rollOrExecute(final Creature creature, final World world, final Future<IActionResult> future) {
        if (shortOfBreathTime > 0) {
            creature.addEffect(new ShortOfBreathEffect(owner, shortOfBreathTime));
        }
        if (action != null) {
            action.apply(creature, world).addListener(new IFutureListener<IActionResult>() {
                @Override public void onHappened(IActionResult result) {
                    future.happen(result);
                }
            });
            return;
        }
        final Ability ability = creature.rollAbility();
        final RollResult roll = new RollResult(creature, ability);
        Logger.log("speed-roll for " + creature + ": " + ability);
        //visualize roll
        world.getController(ViewController.class).visualize(roll).addListener(new IFutureListener<Void>() {
            @Override public void onHappened(Void aVoid) {
                roll.apply(world);
                //prepare ability action
                ability.action.apply(creature, world).addListener(new IFutureListener<IActionResult>() {
                    @Override public void onHappened(final IActionResult result) {
                        //visualize ability action
                        future.happen(result);
                    }
                });
            }
        });
    }
}
