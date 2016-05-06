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

package com.vlaaad.common.tutorial.tasks;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.vlaaad.common.tutorial.TutorialTask;

/**
 * Created 15.11.13 by vlaaad
 */
public abstract class ForceDragActor extends TutorialTask {
    @Override public final void start(final Callback callback) {
        final Actor actor = getActor();
        final Rectangle[] targets = getTargets();
        final Stage stage = actor.getStage();
        if (actor.getStage() == null)
            throw new IllegalStateException("actor is not on stage!");
        stage.addCaptureListener(new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!event.getTarget().isDescendantOf(actor)) {
                    event.cancel();
                    return false;
                }
                return true;
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                for (Rectangle target : targets) {
                    if (target.contains(event.getStageX(), event.getStageY())) {
                        stage.removeCaptureListener(this);
                        callback.taskEnded();
                        return;
                    }
                }
                event.cancel();
            }
        });
    }

    protected abstract Actor getActor();

    protected abstract Rectangle[] getTargets();
}
