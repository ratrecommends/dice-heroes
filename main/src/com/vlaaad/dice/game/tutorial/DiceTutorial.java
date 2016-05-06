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

package com.vlaaad.dice.game.tutorial;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.tutorial.RestrictKeyPresses;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.common.tutorial.tasks.*;
import com.vlaaad.dice.game.actions.imp.TransformToObstacle;
import com.vlaaad.dice.game.effects.TransformToObstacleEffect;
import com.vlaaad.dice.game.tutorial.tasks.*;
import com.vlaaad.dice.game.tutorial.ui.windows.TutorialMessageWindow;
import com.vlaaad.dice.game.world.view.visualizers.actions.AddEffectVisualizer;
import com.vlaaad.dice.states.GameMapState;
import com.vlaaad.dice.states.PvePlayState;
import com.vlaaad.dice.ui.windows.*;

/**
 * Created 07.11.13 by vlaaad
 */
@SuppressWarnings("unchecked")
public class DiceTutorial {

    public static Array<TutorialTask> mainTutorialTasks() {
        return Tutorial.tasks()
            .with(new KeepScreenOn(true))
            .with(new WaitSetAppState("app", GameMapState.class))
                //prepare
            .with(new InitMapStateTask())
            .with(new RestrictKeyPresses(Keys.BACK))
            .with(new RestrictKeyPresses(Keys.MENU))
            .with(new RestrictKeyPresses(Keys.ESCAPE))
                //intro windows
            .with(new ShowWindowTask<String>(new TutorialMessageWindow("tutorial-pic-1"), "tutorial-intro-1"))
            .with(new ShowWindowTask<String>(new TutorialMessageWindow(), "tutorial-intro-2"))
            .with(new ShowWindowWithDieTask("pudi", "tutorial-pudi-introduction"))

                //prepare for inventory stuff
            .with(new RestrictCloseWindows())

            .with(new ShowTutorialMessage("tutorial-open-dice-window"))
            .with(new ShowTutorialArrowOnDiceWindowButton())
            .with(new ForceClickDiceWindow())
            .with(new HideTutorialArrow())
            .with(new HideTutorialMessage())

            .with(new RestrictTouchInput())
            .with(new WaitWindowShown(DiceWindow.class))
            .with(new AllowTouchInput())
            .with(new RestrictOpenWindows())
                //dice window walk through
            .with(new ShowTutorialMessage("tutorial-dice-window"))
            .with(new ShowTutorialArrowOnDiePane("pudi"))
            .with(new ForceClickDiePane("pudi"))
            .with(new HideTutorialMessage())
            .with(new HideTutorialArrow())
            .with(new RestrictTouchInput())
            .with(new WaitDiePaneMaximized())
            .with(new AllowTouchInput())
                //about net
            .with(new ShowTutorialMessage("tutorial-net", false, true))
//            .with(new ShowTutorialArrowOnDieNet("pudi"))
            .with(new ForceClickStage())
            .with(new HideTutorialArrow())
                //open store
            .with(new ShowTutorialMessage("tutorial-open-store"))
            .with(new ShowTutorialArrowOnStoreTab("pudi"))
            .with(new ForceClickStoreTab("pudi"))
            .with(new WaitNextLoopIteration())
                //store tab
            .with(new AllowOpenWindows(StoreWindow.class))
            .with(new ShowTutorialMessage("tutorial-click-defence"))
            .with(new ShowTutorialArrowOnStoreIcon("pudi", "default-defence"))
            .with(new ForceClickStoreIcon("pudi", "default-defence"))

            .with(new HideTutorialMessage())
            .with(new HideTutorialArrow())
            .with(new WaitWindowShown(StoreWindow.class))
                //store window
            .with(new ForceClickBuyAbilityButton())
            .with(new AllowCloseWindows(StoreWindow.class))
            .with(new WaitWindowHidden(StoreWindow.class))
            .with(new WaitNextLoopIteration())
                //drag from inventory to net
            .with(new ShowTutorialMessage("tutorial-place-defence-on-net"))
            .with(new ShowDragAbilityAnimation("pudi", "drag-ability"))
            .with(new ForceDragAbilityOnFreeSlot("pudi", "default-defence"))
            .with(new HideActor("drag-ability"))
                //about skill
            .with(new ShowTutorialArrowOnDieSkills("pudi"))
            .with(new ShowTutorialMessage("tutorial-skill-values", false, true))
            .with(new ForceClickStage())
            .with(new HideTutorialArrow())
            .with(new HideTutorialMessage())
                // exit dice window
            .with(new ShowTutorialMessage("tutorial-close-dice-window"))
            .with(new AllowCloseWindows(DiceWindow.class))
            .with(new AllowKeyPresses(Keys.BACK))
            .with(new AllowKeyPresses(Keys.ESCAPE))
            .with(new FirstCompleted(
                Tutorial.tasks().with(new WaitKeyUp(Keys.BACK)),
                Tutorial.tasks().with(new WaitKeyUp(Keys.ESCAPE)),
                Tutorial.tasks().with(new ForceClickCurrentWindowBackground())
            ))
            .with(new RestrictKeyPresses(Keys.ESCAPE))
            .with(new RestrictKeyPresses(Keys.BACK))
            .with(new HideTutorialMessage())
            .with(new WaitWindowHidden(DiceWindow.class))
                //start mission
            .with(new ShowTutorialMessage("tutorial-open-level"))
            .with(new ShowTutorialArrowOnLevelIcon("tutorial"))
            .with(new ForceClickLevelIcon("tutorial"))
            .with(new HideTutorialMessage(true))
            .with(new HideTutorialArrow())
                //go to play state
                //we ended with map state, so release all stuff inflicting stage, because stage will change,
                //but resources are the same
            .with(new AllowOpenWindows())
            .with(new AllowCloseWindows())
            .with(new AllowKeyPresses(Keys.BACK))
            .with(new AllowKeyPresses(Keys.ESCAPE))
            .with(new AllowKeyPresses(Keys.MENU))
            .with(new WaitSetAppState("app", PvePlayState.class))
                //init play state
            .with(new InitPlayState())
            .with(new RestrictOpenWindows())
            .with(new RestrictCloseWindows())
            .with(new RestrictKeyPresses(Keys.BACK))
            .with(new RestrictKeyPresses(Keys.MENU))
            .with(new RestrictKeyPresses(Keys.ESCAPE))
                //place pudi
            .with(new ShowTutorialMessage("tutorial-place-pudi", true))
            .with(new ShowDragDieAnimation("drag-die"))
            .with(new ForceDragDieOnSpawn("pudi"))
            .with(new HideActor("drag-die"))
                //start fight
            .with(new ShowTutorialMessage("tutorial-press-start", false))
            .with(new ShowTutorialArrowOnStartButton())
            .with(new ForceClickStartButton())
                //press bjum to watch info
            .with(new WaitNextLoopIteration())
            .with(new HideUiController())
            .with(new ShowTutorialMessage("tutorial-press-bjum", true))
            .with(new ShowTutorialArrowOnCreature("bjum"))
            .with(new AllowOpenWindows(CreatureInfoWindow.class))
            .with(new AllowTouchDownOnlyToDie("bjum"))
            .with(new WaitWindowShow(CreatureInfoWindow.class))
            .with(new RestrictTouchInput())
//            .with(new ForceClickCreature("bjum"))
                //open creature info window
            .with(new HideTutorialArrow())
            .with(new HideTutorialMessage())
            .with(new WaitWindowShown(CreatureInfoWindow.class))
            .with(new AllowTouchInput())
            .with(new AllowTouchDown())
            .with(new RestrictOpenWindows())
                //about creature info window
            .with(new ShowTutorialMessage("tutorial-creature-info-window-1", true, true))
            .with(new ForceClickStage())
            .with(new ShowTutorialMessage("tutorial-creature-info-window-2", true, true))
            .with(new ForceClickStage())
            .with(new ShowTutorialMessage("tutorial-creature-info-window-3", true))
                //exit info window
            .with(new AllowCloseWindows(CreatureInfoWindow.class))
            .with(new AllowKeyPresses(Keys.BACK))
            .with(new AllowKeyPresses(Keys.ESCAPE))
            .with(new FirstCompleted(
                Tutorial.tasks().with(new WaitKeyUp(Keys.BACK)),
                Tutorial.tasks().with(new WaitKeyUp(Keys.ESCAPE)),
                Tutorial.tasks().with(new WaitClickCurrentWindowBackground())
            ))
            .with(new RestrictTouchInput())
            .with(new RestrictKeyPresses(Keys.ESCAPE))
            .with(new RestrictKeyPresses(Keys.BACK))
            .with(new HideTutorialMessage())
            .with(new WaitWindowHidden(CreatureInfoWindow.class))
            .with(new AllowTouchInput())
            .with(new RestrictCloseWindows())
                //about move
            .with(new ShowTutorialMessage("tutorial-move", true, true))
            .with(new ShowMoveSelection("tutorial-move-highlight"))
            .with(new ForceClickStage())
                //move & defence
            .with(new ShowTutorialMessage("tutorial-roll", true))
            .with(new HideSelection("tutorial-move-highlight"))
            .with(new ShowTileSelection(2, 2, "move-target"))
            .with(new SetCurrentRolled("default-defence"))
            .with(new PauseRoundController())
            .with(new ForceClickTile(2, 2))
            .with(new ShowTutorialMessage("tutorial-roll-confirm", true))
            .with(new ForceClickTile(2, 2))
            .with(new HideSelection("move-target"))
            .with(new WaitTurnEnded())
                //about what's rolled
            .with(new ShowTutorialMessage("tutorial-what-is-rolled", true, true))
            .with(new ForceClickStage())
//            .with(new ShowTutorialMessage("tutorial-long-press-tip", true, true))
//            .with(new ForceClickStage())
            .with(new RestrictTouchInput())
            .with(new HideTutorialMessage())
                //enemy turn
            .with(new SetNextRolled("default-attack"))
            .with(new ResumeRoundController())
            .with(new WaitTurnEnded())
                //exp gained
            .with(new ShowTutorialMessage("tutorial-exp-gained", true, true))
            .with(new ForceClickStage())
                //pudi's turn
            .with(new ShowTutorialMessage("tutorial-roll-again", true))
            .with(new SetCurrentRolled("default-attack"))
            .with(new ShowTileSelection(2, 2, "move-target"))
            .with(new AllowOpenWindows(CreatureInfoWindow.class))
            .with(new AllowCloseWindows(CreatureInfoWindow.class))
            .with(new AllowTouchInput())
            .with(new NonBlocking(new Looped(
                Tutorial.tasks()
                    .with(new WaitWindowShown(CreatureInfoWindow.class))
                    .with(new PauseListeners())
                    .with(new AllowKeyPresses(Keys.ESCAPE))
                    .with(new AllowKeyPresses(Keys.BACK))
                    .with(new FirstCompleted(
                        Tutorial.tasks().with(new WaitKeyUp(Keys.BACK)),
                        Tutorial.tasks().with(new WaitKeyUp(Keys.ESCAPE)),
                        Tutorial.tasks().with(new WaitClickCurrentWindowBackground())
                    ))
                    .with(new ResumeListeners())
                    .with(new RestrictKeyPresses(Keys.BACK))
                    .with(new RestrictKeyPresses(Keys.ESCAPE))
            ).withName("window-loop")))
            .with(new FirstCompleted(
                Tutorial.tasks().with(new Looped(Tutorial.tasks()
                    .with(new ForceClickTile(2, 2)))),
                Tutorial.tasks().with(new WaitTurnEnded())
            ))
            .with(new CancelTutorial("window-loop", true))
            .with(new HideSelection("move-target"))
            .with(new HideTutorialMessage())
                //kill, win, open reward window
            .with(new AllowOpenWindows(RewardWindow.class))
            .with(new WaitWindowShown(RewardWindow.class))
            .with(new ShowTutorialMessage("tutorial-win", true))
            .with(new AllowCloseWindows())
            .with(new AllowOpenWindows())
            .with(new AllowKeyPresses(Keys.BACK))
            .with(new AllowKeyPresses(Keys.ESCAPE))
            .with(new EndTutorial())

            .with(new WaitSetAppState("app", GameMapState.class))
            .with(new InitMapStateTask())
            .with(new RestrictKeyPresses(Keys.BACK))
            .with(new RestrictKeyPresses(Keys.ESCAPE))
            .with(new ShowWindowTask<String>(new TutorialMessageWindow(), "tutorial-ending"))

            .with(new ShowTutorialMessage("tutorial-end-dice-window"))
            .with(new ShowTutorialArrowOnDiceWindowButton())
            .with(new ForceClickDiceWindow())
            .with(new HideTutorialArrow())
            .with(new HideTutorialMessage())
            .with(new AllowKeyPresses(Keys.BACK))
            .with(new AllowKeyPresses(Keys.ESCAPE))
            .with(new KeepScreenOn(false))
            ;
    }

    public static Array<TutorialTask> initiativeTutorialTasks() {
        return Tutorial.tasks()
            .with(new WaitNextLoopIteration())
            .with(new ShowTutorialMessage("tutorial-initiative", true, true))
            .with(new ShowTutorialArrowOnSpawnPanel())
            .with(new ForceClickStage())
            .with(new HideTutorialArrow())
            .with(new HideTutorialMessage())
            .with(new WaitPlayerTurnStarted())
            .with(new ShowTutorialMessage("tutorial-turn-order-window", false, true))
            .with(new WaitNextLoopIteration())
            .with(new ShowTutorialArrowOnTurnOrderButton())
            .with(new ForceClickStage())
            .with(new HideTutorialArrow())
            .with(new HideTutorialMessage())
            .with(new EndInitiativeTutorial())
            ;
    }

    public static Array<TutorialTask> professionAbilitiesTasks() {
        return tasks(
            new ShowTutorialMessage("tutorial-profession-abilities", false, true),
            new WaitNextLoopIteration(),
            new ShowTutorialArrowOnProfessionAbilities(),
            new ForceClickStage(),
            new HideTutorialArrow(),
            new HideTutorialMessage(),
            new EndProfessionAbilitiesTutorial()
        );
    }

    public static Array<TutorialTask> potionsTasks() {
        return tasks(
            new ShowTutorialMessage("tutorial-potions-available"),
            new ShowTutorialArrowOnPotionsWindowButton(),
            new ForceClickPotionsButton(),
            new HideTutorialArrow(),
            new HideTutorialMessage(),
            new RestrictKeyPresses(Keys.BACK),
            new RestrictKeyPresses(Keys.MENU),
            new RestrictKeyPresses(Keys.ESCAPE),
            new RestrictCloseWindows(),
            new WaitNextLoopIteration(),
            new RestrictOpenWindows(),
            new RestrictTouchInput(),
            new WaitWindowShown(PotionsWindow.class),
            new AllowTouchInput(),
            new ShowTutorialMessage("tutorial-potions-list", false, true),
            new ShowTutorialArrowOnPotionsList(),
            new ForceClickStage(),
            new ShowTutorialMessage("tutorial-potions-description", true, true),
            new ShowTutorialArrowOnPotionDescription(),
            new ForceClickStage(),
            new RestrictTouchInput(),
            new HideTutorialArrow(),
            new HideTutorialMessage(),
            new ScrollPotionsWindowToIngredients(),
            new AllowTouchInput(),
            new ShowTutorialMessage("tutorial-potions-ingredients", true, true),
            new ShowTutorialArrowOnIngredients(),
            new ForceClickStage(),
            new ShowTutorialArrowOnCraftPane(),
            new ShowTutorialMessage("tutorial-potions-brewing", true, true),
            new ForceClickStage(),
            new RestrictTouchInput(),
            new HideTutorialArrow(),
            new HideTutorialMessage(),
            new ScrollPotionsWindowToPotionIngredients(),
            new AllowTouchInput(),
            new ShowPotionWithAnyIngredients(),
            new ShowTutorialMessage("tutorial-potions-any-ingredients", true, true),
            new ShowTutorialArrowOnPotionIngredients(),
            new ForceClickStage(),
            new AllowCloseWindows(),
            new AllowOpenWindows(),
            new AllowKeyPresses(Keys.BACK),
            new AllowKeyPresses(Keys.MENU),
            new AllowKeyPresses(Keys.ESCAPE),
            new HideTutorialMessage(),
            new HideTutorialArrow(),
            new EndMapPotionsTutorial()
        );
    }

    public static Array<TutorialTask> playPotionsTasks() {
        return tasks(
            new RestrictKeyPresses(Keys.BACK),
            new RestrictKeyPresses(Keys.MENU),
            new RestrictKeyPresses(Keys.ESCAPE),
            new ShowTutorialMessage("tutorial-potions-play-action", false, true),
            new WaitNextLoopIteration(),
            new ShowTutorialArrowOnPotionPlayButton(),
            new ForceClickStage(),
            new HideTutorialArrow(),
            new ShowTutorialMessage("tutorial-potions-play-one-potion-per-turn", false, true),
            new ForceClickStage(),
            new HideTutorialMessage(),
            new AllowKeyPresses(Keys.BACK),
            new AllowKeyPresses(Keys.MENU),
            new AllowKeyPresses(Keys.ESCAPE),
            new EndPlayPotionsTutorial()
        );
    }

    public static Array<TutorialTask> spawnScrollingTasks() {
        return tasks(
            new RestrictKeyPresses(Keys.BACK),
            new RestrictKeyPresses(Keys.MENU),
            new RestrictKeyPresses(Keys.ESCAPE),
            new ShowTutorialMessage("tutorial-spawn-swipe", true, true),
            new ShowTutorialArrowOnSpawnPanel(),
            new ForceClickStage(),
            new HideTutorialArrow(),
            new HideTutorialMessage(),
            new AllowKeyPresses(Keys.BACK),
            new AllowKeyPresses(Keys.MENU),
            new AllowKeyPresses(Keys.ESCAPE),
            new CompleteSpawnSwipeTutorial()
        );
    }

    public static Array<TutorialTask> finalBossTasks() {
        return tasks(
            new WaitPreStart(),
            new PauseRoundController(),
            new ScrollTo("boss"),
            new ApplyAbility("boss", "invulnerability"),
            new ApplyAbility("boss", "boss-protection"),
            new RestrictKeyPresses(Keys.ESCAPE, Keys.MENU, Keys.BACK),
            new ShowTutorialMessage("tutorial-boss-intro", true, true),
            new ForceClickStage(),
            new HideTutorialMessage(),
            new AllowKeyPresses(Keys.ESCAPE, Keys.MENU, Keys.BACK),
            new ResumeRoundController(),
            new WaitAllStepDetectorsActivate(),
            new WaitPreNextTurnStart(),
            new PauseRoundController(),
            new ScrollTo("boss"),
            new RemoveEffectOfGroup("boss", "invulnerability"),
            new RestrictKeyPresses(Keys.ESCAPE, Keys.MENU, Keys.BACK),
            new Delay(AddEffectVisualizer.DURATION),
            new ShowTutorialMessage("tutorial-boss-vulnerability", true, true),
            new ForceClickStage(),
            new HideTutorialMessage(),
            new AllowKeyPresses(Keys.ESCAPE, Keys.MENU, Keys.BACK),
            new ResumeRoundController(),
            new WaitEndGame(true),
            new WaitSetAppState("app", GameMapState.class),
            new ShowWindowTask<String>(new TutorialMessageWindow("ending-pic", 28, 85), "tutorial-final-ending")
        );
    }


    public static Array<TutorialTask> tasks(TutorialTask... tasks) {
        return Array.with(tasks);
    }
}
