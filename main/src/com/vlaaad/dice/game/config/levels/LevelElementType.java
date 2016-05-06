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

package com.vlaaad.dice.game.config.levels;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.objects.Obstacle;
import com.vlaaad.dice.game.objects.StepDetector;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.players.Fraction;

import static com.vlaaad.dice.game.config.levels.Decoder.*;

/**
 * Created 06.10.13 by vlaaad
 */
public final class LevelElementType<T> {

    private static final ObjectMap<String, LevelElementType> values = new ObjectMap<String, LevelElementType>();

    @SuppressWarnings("unchecked")
    public static <T> LevelElementType<T> valueOf(String type) {
        return values.get(type);
    }

    public static final LevelElementType<Fraction> spawn = new LevelElementType<Fraction>("spawn", FRACTION);
    public static final LevelElementType<Obstacle> obstacle = new LevelElementType<Obstacle>("obstacle", OBSTACLE);
    public static final LevelElementType<StepDetector> stepDetector = new LevelElementType<StepDetector>("step-detector", STEP_DETECTOR);
    public static final LevelElementType<String> tile = new LevelElementType<String>("tile", STRING);
    public static final LevelElementType<Die> enemy = new LevelElementType<Die>("enemy", DIE);
    public static final LevelElementType<String> cover = new LevelElementType<String>("cover", STRING);

    private final String name;
    public final Decoder<T> decoder;

    public LevelElementType(String name, Decoder<T> decoder) {
        this.name = name;
        this.decoder = decoder;
        values.put(name, this);
    }

    @Override public String toString() {
        return name;
    }
}
