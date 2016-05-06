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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.dice.game.objects.StepDetector;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.managers.SoundManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class StepDetectorSubView implements SubView, IStateDispatcher.Listener<Boolean> {
    private final Tile off;
    private final Tile offGlow;

    private final Tile on;
    private final Tile onGlow;
    private final Group group;
    private final Group offGroup = new Group();
    private final Group onGroup = new Group();

    private final StepDetector stepDetector;

    public StepDetectorSubView(final StepDetector stepDetector) {
        this.stepDetector = stepDetector;

        off = new Tile("step-detector/" + stepDetector.worldObjectName + "-off");
        offGlow = new Tile("step-detector/" + stepDetector.worldObjectName + "-glow");

        on = new Tile("step-detector/" + stepDetector.worldObjectName + "-on");
        onGlow = new Tile("step-detector/" + stepDetector.worldObjectName + "-on-glow");

        group = new Group() {
            @Override protected void setStage(Stage stage) {
                if (getStage() == null && stage != null) {
                    stepDetector.activeState.addListener(StepDetectorSubView.this, false);
                } else if (getStage() != null && stage == null) {
                    stepDetector.activeState.removeListener(StepDetectorSubView.this);
                }
                super.setStage(stage);
                final boolean active = stepDetector.activeState.getState();
                onGroup.getColor().a = active ? 1 : 0;
                offGroup.getColor().a = active ? 0 : 1;
            }
        };
        group.setTransform(false);

        group.addActor(offGroup);
        group.addActor(onGroup);

        initGlow(off, offGlow, offGroup);
        initGlow(on, onGlow, onGroup);
    }

    private void initGlow(Tile a, Tile b, Group group) {
        move(a);
        move(b);
        a.addAction(forever(sequence(alpha(0, 1), alpha(1, 1))));
        b.getColor().a = 0f;
        b.addAction(forever(sequence(alpha(1, 1), alpha(0, 1))));
        group.addActor(a);
        group.addActor(b);
        group.setTransform(false);
    }

    private static void move(Tile tile) {
        tile.setPosition(ViewController.CELL_SIZE * 0.5f, ViewController.CELL_SIZE * 0.5f, Align.center);
    }

    @Override public int getPriority() {
        return 0;
    }

    @Override public void play(String animationName) {
    }

    @Override public Actor getActor() {
        return group;
    }

    @Override public void onChangedState(Boolean active) {
        Group appearing = active ? onGroup : offGroup;
        Group disappearing = active ? offGroup : onGroup;
        appearing.clearActions();
        appearing.addAction(alpha(1, 0.5f));
        disappearing.clearActions();
        disappearing.addAction(alpha(0, 0.5f));
        if (active) {
            SoundManager.instance.playMusicAsSound("step-detector-activation");
        }
    }
}
