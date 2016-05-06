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

package com.vlaaad.dice.game.tutorial.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.vlaaad.common.tutorial.tasks.ShowActor;
import com.vlaaad.dice.game.tutorial.ui.components.TutorialUpArrow;
import com.vlaaad.dice.game.world.controllers.SpawnController;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.states.PvePlayState;

/**
 * Created 16.11.13 by vlaaad
 */
public class ShowDragDieAnimation extends ShowActor {
    public ShowDragDieAnimation(String resourceName) {
        super(resourceName);
    }

    @Override protected Group getTarget() {
        PvePlayState playState = resources.get("playState");
        return playState.world.getController(SpawnController.class).startButton.getParent();
    }

    @Override protected Actor getActorToShow() {
        PvePlayState playState = resources.get("playState");
        Stage stage = resources.get("stage");
        float x = stage.getWidth() / 2 - 10;
        float y = 36;

        Vector2 stagePosition = getTarget().localToStageCoordinates(new Vector2(x, y));
        Vector2 stageTarget = playState.world.getController(ViewController.class).getScreenRectangle(2, 1).getCenter(new Vector2());

        TutorialUpArrow arrow = new TutorialUpArrow();
        arrow.setPosition(x, y);
        arrow.setHeight(stageTarget.y - stagePosition.y);
        return arrow;
    }
}
