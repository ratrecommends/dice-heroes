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

package com.vlaaad.dice.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.gdx.State;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.common.util.ArrayHelper;
import com.vlaaad.common.util.Function;
import com.vlaaad.dice.DiceHeroes;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.tutorial.DiceTutorial;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.*;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.PlayerRelation;
import com.vlaaad.dice.game.world.players.util.PlayerColors;
import com.vlaaad.dice.game.world.view.visualizers.objects.Death;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.windows.PauseWindow;

/**
 * Created 06.10.13 by vlaaad
 */
public class PvePlayState extends State {

    public static final Color BACKGROUND = new Color(0.588235294f, 0.788235294f, 0.8f, 1f);

    private final DiceHeroes diceHeroes;
    private final Callback callback;
    public final World world;
    private final PauseWindow pauseWindow = new PauseWindow();
    private final PauseWindow.Params params = new PauseWindow.Params(new PauseWindow.Callback() {
        @Override public void onRestart() {
            callback.onRestart();
        }

        @Override public void onCancel() {
            callback.onCancel();
        }
    });
    private GameState state;
    private final Color color;

    public PvePlayState(DiceHeroes diceHeroes, Player viewer, ObjectMap<Fraction, Player> players, PlayerColors playerColors, LevelDescription levelDescription, GameState state, Callback callback) {
        super();
        this.diceHeroes = diceHeroes;
        this.callback = callback;
        this.world = new World(viewer, players, playerColors, levelDescription, stage);
        this.state = state;
        this.color = levelDescription.backgroundColor == null ? BACKGROUND : levelDescription.backgroundColor;
    }

    private void setState(GameState state) {
        this.state = state;
        switch (state) {
            case PLACE:
                world.addController(PveLoadLevelController.class);
                world.addController(SpawnController.class);
                world.dispatcher.add(SpawnController.START, new EventListener<Void>() {
                    @Override public void handle(EventType<Void> type, Void aVoid) {
                        world.removeController(SpawnController.class);
                        setState(GameState.PLAY);
                    }
                });
                break;
            case PLAY:
                world.addController(PveBehaviourController.class, BehaviourController.class);
                world.addController(RandomController.class);
                world.addController(UiController.class);
                world.addController(RandomController.class);
                world.addController(RoundController.class);
                world.dispatcher.add(RoundController.WIN, new EventListener<LevelResult>() {
                    @Override public void handle(EventType<LevelResult> type, LevelResult levelResult) {
                        callback.onWin(levelResult);
                    }
                });
                world.dispatcher.add(RoundController.LOSE, new EventListener<LevelResult>() {
                    @Override public void handle(EventType<LevelResult> type, LevelResult levelResult) {
                        callback.onLose(levelResult);
                    }
                });
                break;
            default:
                throw new IllegalStateException("unknown state: " + state);
        }
    }

    @Override protected void init() {
        world.addController(ViewController.class);
        world.addController(CreatureInfoController.class);
        world.init();
        if (world.level.name.equals("final-boss") && !world.viewer.tutorialProvider.isBossTutorialCompleted()) {
            final Tutorial.TutorialResources resources = Tutorial.resources()
                .with("world", world)
                .with("app", diceHeroes)
                .with("stage", world.stage)
                .with("tutorial-provider", world.viewer.tutorialProvider)
                .with("playState", PvePlayState.this);
            new Tutorial(resources, DiceTutorial.finalBossTasks()).start();
//            stage.addCaptureListener(new InputListener() {
//                @Override public boolean keyTyped(InputEvent event, char character) {
//                    if (character == 'd') {
//                        world.getController(ViewController.class).visualize(ArrayHelper.map(ArrayHelper.from(world.getController(RoundController.class).getAlive(PlayerRelation.any)), new Function<Creature, Death>() {
//                            @Override public Death apply(Creature creature) {
//                                return new Death(creature, creature);
//                            }
//                        }));
//                    }
//                    return super.keyTyped(event, character);
//                }
//            });
        }
        setState(state);
        SoundManager.instance.playMusicBeautifully("ambient-battle", stage);
    }

    @Override protected Color getBackgroundColor() {
        return color;
    }

    @Override protected void resume(boolean isStateChange) {
    }

    @Override protected void pause(boolean isStateChange, Stage stage) {
    }

    @Override protected void dispose(boolean isStateChange, Stage stage) {
        if (stage != null)
            SoundManager.instance.stopMusicBeautifully("ambient-battle", stage);
        else
            SoundManager.instance.stopMusic("ambient-battle");
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                PvePlayState.this.stage.dispose();
            }
        });
    }

    @Override protected void onBackPressed() {
        if (state != GameState.PLAY) {
            callback.onCancel();
        } else {
            pauseWindow.show(params);
        }
    }

    @Override protected boolean disposeOnSwitch() {
        return true;
    }

    public interface Callback {
        void onWin(LevelResult levelResult);

        void onLose(LevelResult levelResult);

        void onCancel();

        void onRestart();
    }

    public enum GameState {
        PLACE, PLAY
    }
}
