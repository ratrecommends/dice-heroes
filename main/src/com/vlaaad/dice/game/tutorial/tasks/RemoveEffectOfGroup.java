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
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.world.World;

public class RemoveEffectOfGroup extends TutorialTask {
    private final String creatureName;
    private final String effectGroup;

    public RemoveEffectOfGroup(String creatureName, String effectGroup) {
        this.creatureName = creatureName;
        this.effectGroup = effectGroup;
    }

    @Override public void start(final Callback callback) {
        final World world = resources.get("world");
        final IFuture<Void> f = world.getCreatureByName(creatureName).removeEffect(effectGroup);
        if (f != null) {
            f.addListener(new IFutureListener<Void>() {
                @Override public void onHappened(Void aVoid) {
                    callback.taskEnded();
                }
            });
        } else {
            callback.taskEnded();
        }
    }
}
