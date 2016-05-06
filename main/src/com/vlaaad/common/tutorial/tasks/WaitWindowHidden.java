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

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.ui.WindowListener;

/**
 * Created 13.11.13 by vlaaad
 */
public class WaitWindowHidden extends TutorialTask {
    private final Class<? extends GameWindow> windowClass;

    public WaitWindowHidden(Class<? extends GameWindow> windowClass) {
        super();
        this.windowClass = windowClass;
    }

    @Override public void start(final Callback callback) {
        final Stage stage = resources.get("stage");
        stage.addListener(new WindowListener() {
            @Override protected void hidden(WindowEvent event) {
                if (windowClass.isInstance(event.getWindow())) {
                    stage.removeListener(this);
                    callback.taskEnded();
                }
            }
        });
    }
}
