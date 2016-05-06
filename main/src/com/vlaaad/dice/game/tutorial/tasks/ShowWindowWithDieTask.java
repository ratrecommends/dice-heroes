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

import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.common.ui.WindowListener;
import com.vlaaad.common.ui.WindowManager;
import com.vlaaad.dice.game.tutorial.ui.windows.DieMessageWindow;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;

/**
 * Created 11.11.13 by vlaaad
 */
public class ShowWindowWithDieTask extends TutorialTask {

    private final String dieName;
    private final String locKey;

    public ShowWindowWithDieTask(String dieName, String locKey) {
        super();
        this.dieName = dieName;
        this.locKey = locKey;
    }

    @Override public void start(final Callback callback) {
        final DieMessageWindow window = new DieMessageWindow();
        UserData data = resources.get("userData");
        Die die = data.findDieByName(dieName);
        window.addListener(new WindowListener() {
            @Override protected void hidden(WindowEvent event) {
                window.removeListener(this);
                callback.taskEnded();
            }
        });
        WindowManager.instance.show(window, new DieMessageWindow.Params(die, locKey));

    }
}
