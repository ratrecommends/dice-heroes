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
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.vlaaad.common.tutorial.TutorialTask;

/**
 * Created 17.11.13 by vlaaad
 */
public abstract class ForceClickRectangle extends TutorialTask {
    private EventListener listener;

    @Override public void start(final Callback callback) {
        final Rectangle target = getTarget();
        final Stage stage = resources.get("stage");
        listener = new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (resources.get(PauseListeners.KEY, Boolean.FALSE))
                    return false;
                if (!target.contains(event.getStageX(), event.getStageY())) {
                    event.cancel();
                    return false;
                }
                return true;
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (event.isCancelled() || resources.get(PauseListeners.KEY, Boolean.FALSE))
                    return;
                if (target.contains(event.getStageX(), event.getStageY())) {
                    stage.removeCaptureListener(this);
                    callback.taskEnded();
                    return;
                }
                event.cancel();
            }
        };
        stage.addCaptureListener(listener);
    }

    @Override public void cancel() {
        final Stage stage = resources.get("stage");
        stage.removeCaptureListener(listener);
    }

    protected abstract Rectangle getTarget();
}
