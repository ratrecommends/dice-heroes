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

package com.vlaaad.dice.game.world.controllers;

import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.pvp.util.MersenneTwisterFast;

/**
 * Created 30.07.14 by vlaaad
 */
public class RandomController extends WorldController {

    private final MersenneTwisterFast twister;

    public RandomController(World world) {
        this(world, System.currentTimeMillis());
    }

    public RandomController(World world, long seed) {
        super(world);
        twister = new MersenneTwisterFast(seed);
    }

    @Override protected void start() {}
    @Override protected void stop() {}

    public float random(float range) {
        return twister.nextFloat() * range;
    }
    public <T> T random(Array<T> options) { return options.size == 0 ? null : options.get(twister.nextInt(options.size)); }
    public boolean randomBoolean() {
        return twister.nextBoolean();
    }
    public boolean randomBoolean(float probability) {
        return twister.nextBoolean(probability);
    }
}
