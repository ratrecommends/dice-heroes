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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.vlaaad.common.gdx.scene2d.AnimatedActor;
import com.vlaaad.common.tutorial.tasks.ShowActor;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.states.GameMapState;

/**
 * Created 16.11.13 by vlaaad
 */
public class ShowDragAbilityAnimation extends ShowActor {

    private final String dieName;

    public ShowDragAbilityAnimation(String dieName, String targetResourceName) {
        super(targetResourceName);
        this.dieName = dieName;
    }

    @Override protected Group getTarget() {
        UserData userData = resources.get("userData");
        Die die = userData.findDieByName(dieName);

        GameMapState mapState = resources.get("map");
        return mapState.diceWindow.getPane(die).params;
    }

    @Override protected Actor getActorToShow() {
        AnimatedActor actor = new AnimatedActor(0.1f, Config.findRegions("tutorial-drag-ability"));
        actor.setPosition(38, 48);
        return actor;
    }
}
