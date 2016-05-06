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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Ref;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.actions.results.imp.*;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.view.TargetVisualizer.TargetChecker;
import com.vlaaad.dice.game.world.view.visualizers.*;
import com.vlaaad.dice.game.world.view.visualizers.actions.*;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.game.world.view.visualizers.objects.Defence;
import com.vlaaad.dice.game.world.view.visualizers.objects.DroppedItem;
import com.vlaaad.dice.managers.SoundManager;

import static com.vlaaad.dice.game.world.view.AbilityVisualizer.*;

/**
 * Created 08.10.13 by vlaaad
 */
public class ResultVisualizer {
    public static final EventType<Creature> VISUALIZE_DEFENCE = new EventType<Creature>();
    public static final EventType<Creature> VISUALIZE_ATTACK = new EventType<Creature>();

    public final ViewController viewController;

    private final ObjectMap<Class, IVisualizer> resultVisualizers = new ObjectMap<Class, IVisualizer>();

    @SuppressWarnings("unchecked")
    public ResultVisualizer(final ViewController viewController) {
        this.viewController = viewController;

        // ---------------------- results ---------------------- //
        registerActionVisualizer(MoveResult.class, new IVisualizer<MoveResult>() {
            @Override public IFuture<Void> visualize(MoveResult moveResult) {
                final Future<Void> future = new Future<Void>();
                final WorldObjectView view = viewController.getView(moveResult.creature);
                viewController.scroller.centerOn(moveResult.x, moveResult.y);
                SoundManager.instance.playSound("creature-move");
                view.addAction(Actions.sequence(
                    Actions.moveTo(moveResult.x * ViewController.CELL_SIZE, moveResult.y * ViewController.CELL_SIZE, 0.1f),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            future.happen();
                        }
                    })
                ));
                return future;
            }
        });

        registerActionVisualizer(RollResult.class, new IVisualizer<RollResult>() {
            @Override public IFuture<Void> visualize(final RollResult result) {
                final Future<Void> future = new Future<Void>();
                final WorldObjectView view = viewController.getView(result.creature);
                SoundManager.instance.playSound("creature-roll");
                view.addAction(Actions.sequence(
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            view.play("roll");
                        }
                    }),
                    Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.2f, Interpolation.swingOut),
                        Actions.moveBy(0, result.creature.get(Attribute.canMove) ? 8 : 2, 0.2f, Interpolation.swingOut)
                    ),
                    Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.2f, Interpolation.elastic),
                        Actions.moveBy(0, result.creature.get(Attribute.canMove) ? -8 : -2, 0.2f, Interpolation.elastic)
                    ),
                    Actions.run(new Runnable() {
                        @Override public void run() {
                            view.play(result.ability.name);
                            viewController.resort();
                            future.happen();
                        }
                    })
                ));
                return future;
            }
        });
        registerActionVisualizer(FirestormResult.class, new FirestormVisualizer(this));
        registerActionVisualizer(AttackResult.class,
            AbilityVisualizer
                .withDefault(new AttackVisualizer(this))
                .with("double-defensive-attack", new DoubleDefensiveAttackVisualizer(this))
        );
        registerActionVisualizer(ShotResult.class, new ShotVisualizer(this));
        registerActionVisualizer(ClericDefenceResult.class, new ClericDefenceVisualizer(this));

        registerActionVisualizer(CleaveResult.class, new CleaveVisualizer(this));
        registerActionVisualizer(FireballResult.class, new ParticleAttackVisualizer<FireballResult>(this));
        registerActionVisualizer(FreezeResult.class, new FreezeResultVisualizer(this));
        registerActionVisualizer(ChainLightningResult.class, new ChainLightningVisualizer(this));
        registerActionVisualizer(AreaOfAttackResult.class, new AreaOfAttackVisualizer(this));
        registerActionVisualizer(AreaOfDefenceResult.class, new AreaOfDefenceVisualizer(this));
        registerActionVisualizer(BerserkAttackResult.class, new BerserkAttackVisualizer(this));
        registerActionVisualizer(PoisonShotResult.class, AbilityVisualizer
            .withDefault(new PoisonDartVisualizer(this))
            .with("poison-shot", new ParticlePoisonShotVisualizer(this))
        );
        registerActionVisualizer(DeathResult.class, new DeathResultVisualizer(this));
        registerActionVisualizer(TransformToObstacleResult.class,
            AbilityVisualizer
                .withDefault(new TransformToObstacleVisualizer(this))
                .with("petrification", SeqVisualizer.make(
                    CommonShotVisualizer.make(this),
                    new TransformToObstacleVisualizer(this)
                ))
        );

        registerActionVisualizer(TransformFromObstacleResult.class, new TransformFromObstacleVisualizer(this));
        registerActionVisualizer(TeleportResult.class, new TeleportVisualizer(this));
        registerActionVisualizer(ResurrectResult.class, new ResurrectVisualizer(this));
        registerActionVisualizer(SummonResult.class,
            AbilityVisualizer.withDefault(new SummonVisualizer(this))
                .with(AbilityChecker.make(
                    AbilityChecker.startsWith("boss-summon"),
                    new ResurrectLikeSummonVisualizer(this)
                ))
        );
        registerActionVisualizer(AntidoteResult.class, new AntidoteVisualizer(this));
        registerActionVisualizer(PotionResult.class, new PotionVisualizer(this));
        registerActionVisualizer(RangedDamageResult.class, new RangedDamageVisualizer(this));
        registerActionVisualizer(ViscosityResult.class, AbilityVisualizer
            .withDefault(new ViscosityVisualizer(this))
            .with("viscosity", SeqVisualizer.make(
                CommonShotVisualizer.make(this),
                new ViscosityVisualizer(this)
            ))
        );
        registerActionVisualizer(StupefactionResult.class, new StupefactionVisualizer(this));
        registerActionVisualizer(TurnEndedResult.class, new BeamVisualizer(this, new Color(0, 0, 0, 0.6f), "bad-luck"));
        registerActionVisualizer(ExtraTurnResult.class, new BeamVisualizer(this, new Color(1, 1, 1, 0.9f), "good-luck"));
        registerActionVisualizer(RestrictResurrectResult.class, new AnimationFadeVisualizer<RestrictResurrectResult>(this));
        registerActionVisualizer(RestrictUseAbilitiesResult.class, new RestrictUseAbilitiesVisualizer(this));
        registerActionVisualizer(PoisonAreaResult.class, new PoisonAreaVisualizer(this));
        registerActionVisualizer(TeleportTargetResult.class, new TeleportTargetResultVisualizer(this));
        registerActionVisualizer(DecreaseAttackAndDefenceResult.class, new DecreaseAttackAndDefenceVisualizer(this));
        registerActionVisualizer(ShamanDefenceResult.class, new ShamanDefenceVisualizer(this));
        registerActionVisualizer(EnthrallmentResult.class, new EnthrallmentVisualizer(this));
        registerActionVisualizer(IceStormResult.class, new IceStormVisualizer(this));

        register(DroppedItem.class, new DropVisualizer(this));
        register(Death.class, TargetVisualizer
            .withDefault(new DeathVisualizer(this))
            .with(TargetChecker.withProfession(
                "boss",
                SeqVisualizer.make(new DeathVisualizer(this), new BossDeathVisualizer(this))
            ))
        );

        registerActionVisualizer(SequenceResult.class, new IVisualizer<SequenceResult>() {
            @Override public IFuture<Void> visualize(SequenceResult result) {
                final Future<Void> future = new Future<Void>();
                visualize(future, result.results, 0);
                return future;
            }

            private void visualize(final Future<Void> future, final Array<IActionResult> results, final int idx) {
                if (idx >= results.size) {
                    future.happen();
                    return;
                }
                ResultVisualizer.this.visualize(results.get(idx)).addListener(new IFutureListener<Void>() {
                    @Override public void onHappened(Void aVoid) {
                        visualize(future, results, idx + 1);
                    }
                });
            }
        });
        register(Array.class, new IVisualizer<Array>() {
            @Override public IFuture<Void> visualize(Array array) {
                if (array.size == 0) {
                    return Future.completed();
                }
                final Future<Void> future = new Future<Void>();
                visualize(future, array, 0);
                return future;
            }

            private void visualize(final Future<Void> future, final Array array, final int i) {
                if (i >= array.size) {
                    future.happen();
                } else {
                    ResultVisualizer.this.visualize(array.get(i)).addListener(new IFutureListener<Void>() {
                        @Override public void onHappened(Void aVoid) {
                            visualize(future, array, i + 1);
                        }
                    });
                }
            }
        });
        register(Defence.class, new DefenceVisualizer(this));

        registerActionVisualizer(ParallelResult.class, new IVisualizer<ParallelResult>() {
            @Override public IFuture<Void> visualize(ParallelResult result) {
                final Ref<Integer> count = new Ref<Integer>(result.results.size);
                final Future<Void> future = new Future<Void>();
                for (IActionResult child : result.results) {
                    ResultVisualizer.this.visualize(child).addListener(new IFutureListener<Void>() {
                        @Override public void onHappened(Void aVoid) {
                            count.set(count.get() - 1);
                            if (count.get() == 0)
                                future.happen();
                        }
                    });
                }
                return future;
            }
        });
        registerActionVisualizer(AddEffect.class, new AddEffectVisualizer(this));

        registerActionVisualizer(GiveExpResult.class, new GiveExpVisualizer(this));
    }

    @SuppressWarnings("unchecked")
    public IFuture<Void> visualize(Object something) {
        Class type = something.getClass();
        while (type != null) {
            IVisualizer visualizer = resultVisualizers.get(type);
            if (visualizer != null) {
                final Future<Void> future = new Future<Void>();
                visualizer.visualize(something).addListener(new IFutureListener() {
                    @Override public void onHappened(Object o) {
                        viewController.resort();
                        future.happen();
                    }
                });
                return future;
            }
            type = type.getSuperclass();
        }
        return Future.completed();
    }

    public boolean canVisualize(Object something) {
        Class type = something.getClass();
        while (type != null) {
            if (resultVisualizers.get(type) != null)
                return true;
            type = type.getSuperclass();
        }
        return false;
    }

    private <T> void register(Class<T> type, IVisualizer<T> visualizer) {
        resultVisualizers.put(type, visualizer);
    }

    private <T extends IActionResult> void registerActionVisualizer(Class<T> type, IVisualizer<? super T> visualizer) {
        resultVisualizers.put(type, visualizer);
    }

    @SuppressWarnings("unchecked")
    public <T> IVisualizer<T> getVisualizer(Class<T> type) {
        return resultVisualizers.get(type);
    }
}
