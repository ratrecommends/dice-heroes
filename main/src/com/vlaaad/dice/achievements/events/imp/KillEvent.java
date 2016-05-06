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

package com.vlaaad.dice.achievements.events.imp;

import com.vlaaad.dice.achievements.events.Event;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 19.05.14 by vlaaad
 */
public class KillEvent extends Event {
    private Creature killer;
    private Creature[] killed;

    public KillEvent killed(Creature[] killed) {
        this.killed = killed;
        return this;
    }

    public KillEvent killer(Creature killer) {
        this.killer = killer;
        return this;
    }

    @Override public void reset() {
        super.reset();
        killed = null;
        killer = null;
    }

    public Creature[] killed() {
        return killed;
    }

    public Creature killer() {
        return killer;
    }
}
