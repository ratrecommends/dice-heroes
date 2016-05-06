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

import com.vlaaad.dice.game.world.players.Fraction;

/**
 * Created 07.10.13 by vlaaad
 */
public class SpawnPoint {
    public final int x;
    public final int y;
    public final Fraction fraction;

    public SpawnPoint(int x, int y, Fraction fraction) {
        this.x = x;
        this.y = y;
        this.fraction = fraction;
    }
}
