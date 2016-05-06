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

package com.vlaaad.dice.achievements.events;

import com.badlogic.gdx.utils.Pool;
import com.vlaaad.common.gdx.App;
import com.vlaaad.dice.game.user.UserData;

/**
 * Created 15.05.14 by vlaaad
 */
public class Event implements Pool.Poolable {

    private App app;
    private UserData userData;
    private EventType type;

    @Override public void reset() {
        app = null;
        type = null;
        userData = null;
    }

    public App app() { return app; }

    public Event app(App app) {
        this.app = app;
        return this;
    }

    public EventType type() {return type;}

    public Event type(EventType type) {
        this.type = type;
        return this;
    }

    public UserData userData() { return userData; }

    public Event userData(UserData userData) {
        this.userData = userData;
        return this;
    }
}
