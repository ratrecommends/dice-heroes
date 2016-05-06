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

import com.badlogic.gdx.utils.*;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.Tuple2;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.imp.Potion;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.MoveResult;
import com.vlaaad.dice.game.actions.results.imp.RollResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.attributes.modifiers.imp.SetFlag;
import com.vlaaad.dice.game.effects.HiddenEffect;
import com.vlaaad.dice.game.effects.ModifierEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.PlayerRelation;

/**
 * Created 07.10.13 by vlaaad
 */
public class RoundController extends WorldController {

    public static final EventType<LevelResult> WIN = new EventType<LevelResult>();
    public static final EventType<LevelResult> LOSE = new EventType<LevelResult>();
    public static final EventType<Creature> TURN_STARTED = new EventType<Creature>();
    public static final EventType<Creature> TURN_ENDED = new EventType<Creature>();
    public static final EventType<RoundController> PRE_NEXT_TURN_START = new EventType<RoundController>();
    public static final EventType<RoundController> PRE_START = new EventType<RoundController>();

    public final Array<Creature> queue = new Array<Creature>();
    public final Array<Creature> killed = new Array<Creature>();
    public int idx = -1;
    private Creature currentCreature;
    private boolean paused;
    private boolean turnInProgress;

    public RoundController(World world) {
        super(world);
    }

    public boolean isTurnInProgress() {
        return turnInProgress;
    }

    @Override protected void start() {
        world.dispatcher.add(World.KILL, new EventListener<Creature>() {
            @Override public void handle(EventType<Creature> type, Creature creature) {
                kill(creature);
            }
        });
        for (Creature creature : world.byType(Creature.class)) {
            if (!queue.contains(creature, true))
                queue.add(creature);
        }
        world.dispatcher.add(World.ADD_WORLD_OBJECT, new EventListener<WorldObject>() {
            @Override public void handle(EventType<WorldObject> type, WorldObject object) {
                if (object instanceof Creature) {
                    Creature c = (Creature) object;
                    if (!queue.contains(c, true))
                        queue.insert(idx + 1, (Creature) object);
                    killed.removeValue(c, true);
                }
            }
        });
        queue.shrink();
        queue.sort(Ability.INITIATIVE_COMPARATOR);
        world.dispatcher.dispatch(PRE_START, this);
        nextTurn();
    }

    public void pause() {
        if (paused)
            return;
        paused = true;
    }

    public void resume() {
        if (!paused)
            return;
        paused = false;
        if (!turnInProgress)
            nextTurn();
    }

    private void kill(Creature creature) {
        int i = queue.indexOf(creature, true);
        if (i == -1)
            throw new IllegalStateException("removed creature that is not in queue!");
        queue.removeValue(creature, true);
        if (idx >= i) {
            idx--;
        }
        killed.add(creature);
    }

    private void nextTurn() {
        world.dispatcher.dispatch(PRE_NEXT_TURN_START, this);
        if (paused) {
            return;
        }
        if (checkEnd()) {
            return;
        }
        idx++;
        if (idx >= queue.size) {
            idx = 0;
        }
        final Creature creature = queue.get(idx);
        currentCreature = creature;
        if (creature.skipsTurn()) {
            creature.updateEffects(false).addListener(new IFutureListener<Void>() {
                @Override public void onHappened(Void aVoid) {
                    if (creature.skipsTurn()) {
                        nextTurn();
                    } else {
//                        creature.set(Attribute.actionPoints, 1);
                        processTurn(creature);
                    }
                }
            });
        } else {
            creature.updateEffects(true).addListener(new IFutureListener<Void>() {
                @Override public void onHappened(Void aVoid) {
//                    creature.set(Attribute.actionPoints, 1);
                    processTurn(creature);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void processTurn(final Creature creature) {
        if (checkEnd()) {
            return;
        }
        if (creature.get(Attribute.actionPoints) <= 0) {
            creature.set(Attribute.actionPoints, creature.get(Attribute.defaultActionPoints));
            nextTurn();
            return;
        }
        if (creature.world == null) {
            nextTurn();
            return;
        }
        turnInProgress = true;
        world.dispatcher.dispatch(TURN_STARTED, creature);
        world.getController(BehaviourController.class)
            .get(creature)
            .request(BehaviourRequest.TURN, new TurnParams(creature, MoveResult.getAvailableCoordinates(creature), creature.getAvailableAbilities()))
            .addListener(new IFutureListener<TurnResponse>() {
                @Override public void onHappened(TurnResponse response) {
                    if (response.action == TurnResponse.TurnAction.MOVE) {
                        Grid2D.Coordinate vector2 = (Grid2D.Coordinate) response.data;
                        if (vector2.x() == creature.getX() && vector2.y() == creature.getY()) {
                            roll(creature);
                        } else {
                            final MoveResult result = new MoveResult(creature, vector2.x(), vector2.y());
                            world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
                                @Override public void onHappened(Void aVoid) {
                                    result.apply(world);
                                    roll(creature);
                                }
                            });
                        }
                    } else if (response.action == TurnResponse.TurnAction.PROFESSION_ABILITY) {
                        useAbility(creature, (Ability) response.data);
                    } else if (response.action == TurnResponse.TurnAction.POTION) {
                        Tuple2<Ability, Potion.ActionType> tuple = (Tuple2<Ability, Potion.ActionType>) response.data;
                        usePotion(creature, tuple._1, tuple._2);
                    } else if (response.action == TurnResponse.TurnAction.SKIP) {
                        world.dispatcher.dispatch(TURN_ENDED, currentCreature);
                        currentCreature = null;
                        turnInProgress = false;
                        creature.set(Attribute.actionPoints, creature.get(Attribute.actionPoints) - 1);
                        processTurn(creature);
                    } else {
                        throw new IllegalStateException("unknown action: " + response.action);
                    }
                }
            });
    }

    private void usePotion(final Creature creature, final Ability potion, Potion.ActionType action) {
        Logger.debug(creature + " uses potion " + potion.name);
        creature.set(Attribute.potionAction, action);
        potion.action.apply(creature, world).addListener(new IFutureListener<IActionResult>() {
            @Override public void onHappened(final IActionResult result) {
                //visualize ability action
                world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
                    @Override public void onHappened(Void aVoid) {
                        creature.addEffect(new HiddenEffect(new ModifierEffect<Boolean>(
                            null, Attribute.canUsePotions, new SetFlag(false), 1, "use-potions", null
                        )));
                        world.onUsePotion(creature.player, potion);
                        result.apply(world);
                        processTurn(creature);
                    }
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private boolean checkEnd() {
        if (queue.size == 0) {
            world.dispatcher.dispatch(LOSE, gatherResults());
            return true;
        }
        ObjectSet<Player> dead = Pools.obtain(ObjectSet.class);
        for (Player player : world.players.values()) {
            boolean isDead = true;
            for (Creature creature : player.creatures) {
                if (!creature.isKilled()) {
                    isDead = false;
                    break;
                }
            }
            if (isDead) {
                dead.add(player);
            }
        }
        Player winner = null;
        for (ObjectMap.Entry<Fraction, Player> e : world.players.entries()) {
            Player player = e.value;
            boolean allEnemiesAreDead = true;
            for (Player check : world.players.values()) {
                if (player.inRelation(check, PlayerRelation.enemy) && !dead.contains(check)) {
                    allEnemiesAreDead = false;
                    break;
                }
            }
            if (allEnemiesAreDead) {
                winner = player;
                break;
            }
        }
        dead.clear();
        Pools.free(dead);
        if (winner == null)
            return false;
        if (winner.inRelation(world.viewer, PlayerRelation.allyOrSelf)) {
            world.dispatcher.dispatch(WIN, gatherResults());
        } else {
            world.dispatcher.dispatch(LOSE, gatherResults());
        }
        return true;
    }

    private LevelResult gatherResults() {
        return createResult(world);
    }

    public static LevelResult createResult(World world) {
        ObjectIntMap<Die> addedExp = new ObjectIntMap<Die>();
        for (Creature creature : world.viewer.creatures) {
            addedExp.put(creature.description, creature.gainedExp);
        }
        return new LevelResult(addedExp, world.players, world.viewer);
    }

    private void roll(final Creature creature) {

        final Ability ability = creature.rollAbility();
        final RollResult roll = new RollResult(creature, ability);
//        Logger.log("roll for " + creature + ": " + ability);
        //visualize roll
        world.getController(ViewController.class).visualize(roll).addListener(new IFutureListener<Void>() {
            @Override public void onHappened(Void aVoid) {
                roll.apply(world);
                useAbility(creature, ability);
            }
        });
    }

    private void useAbility(final Creature creature, final Ability ability) {
        Logger.debug(creature + " uses " + ability.name);
        ability.action.apply(creature, world).addListener(new IFutureListener<IActionResult>() {
            @Override public void onHappened(final IActionResult result) {
                //visualize ability action
                world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
                    @Override public void onHappened(Void aVoid) {
                        result.apply(world);
                        world.dispatcher.dispatch(TURN_ENDED, currentCreature);
                        currentCreature = null;
                        turnInProgress = false;
                        creature.set(Attribute.actionPoints, creature.get(Attribute.actionPoints) - 1);
                        processTurn(creature);
                    }
                });
            }
        });
    }

    @Override protected void stop() {

    }

    public Creature getCurrentCreature() {
        return currentCreature;
    }

    public Creature getNextCreature() {
        int nextIndex = idx + 1;
        if (nextIndex >= queue.size) {
            nextIndex = 0;
        }
        return queue.get(nextIndex);
    }

    public Iterable<Creature> getAlive(final PlayerRelation relation) {
        return queue.select(new Predicate<Creature>() {
            @Override public boolean evaluate(Creature arg0) {
                return arg0.initialPlayer.inRelation(world.viewer, relation);
            }
        });
    }
}
