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

package com.vlaaad.dice.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.App;
import com.vlaaad.common.gdx.State;
import com.vlaaad.dice.DiceHeroes;
import com.vlaaad.dice.api.services.achievements.IGameAchievements;
import com.vlaaad.dice.achievements.Achievement;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;

/**
 * Created 18.05.14 by vlaaad
 */
public class LocalAchievements implements IGameAchievements, App.AppListener {

    private DiceHeroes diceHeroes;
    private Stage stage;
    private final Array<AchievementView> queue = new Array<AchievementView>();
    private AchievementView current;

    @Override public void start(Iterable<Achievement> allAchievements) {
        diceHeroes = (DiceHeroes) Gdx.app.getApplicationListener();
        diceHeroes.addListener(this);
        enterState(diceHeroes.getState());
    }

    @Override public void unlock(Achievement achievement) {
        AchievementView view = new AchievementView(achievement);
        queue.add(view);
        checkQueue();
    }

    private void checkQueue() {
        if (current != null || queue.size == 0)
            return;
        current = queue.removeIndex(0);
        stage.addActor(current);
        current.setPosition(stage.getWidth() / 2 - current.getWidth() / 2, stage.getHeight() - current.getHeight() - 10);
        current.getColor().a = 0;
        current.addAction(sequence(
            moveBy(0, current.getHeight() + 10),
            parallel(
                alpha(1, 0.5f),
                moveBy(0, -current.getHeight() - 10, 0.5f, Interpolation.swingOut)
            ),
            delay(2.5f),
            alpha(0, 1f),
            run(new Runnable() {
                @Override public void run() {
                    current.remove();
                    current = null;
                    checkQueue();
                }
            })
        ));
    }

    @Override public void showAchievements() {
        new AchievementsWindow().show(diceHeroes.userData);
    }

    @Override public void setCount(Achievement achievement, int count) {
//        System.out.println(achievement.name + ": " + count);
    }

    @Override public void enterState(State state) {
        stage = state.stage;
        if (current != null) {
            stage.addActor(current);
        }
    }

    @Override public void exitState(State state) {
    }
}
