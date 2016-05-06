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

import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.CreatureRequirementFactory;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Obstacle;
import com.vlaaad.dice.game.objects.StepDetector;
import com.vlaaad.dice.game.requirements.DieRequirement;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public interface Decoder<T> {

    Decoder<String> STRING = new Decoder<String>() {
        @Override public String decode(Object o) {
            return (String) o;
        }

        @Override public Object encode(String s) {
            return s;
        }
    };

    Decoder<Void> VOID = new Decoder<Void>() {
        @Override public Void decode(Object o) {
            return null;
        }

        @Override public Object encode(Void aVoid) {
            return null;
        }
    };

    Decoder<Fraction> FRACTION = new Decoder<Fraction>() {
        @Override public Fraction decode(Object o) {
            return o == null ? PlayerHelper.protagonist : Fraction.valueOf((String) o);
        }

        @Override public Object encode(Fraction fraction) {
            return fraction.name;
        }
    };

    Decoder<Die> DIE = new Decoder<Die>() {
        @Override public Die decode(Object o) {
            return Die.fromMap((Map) o);
        }

        @Override public Object encode(Die die) {
            return Die.toMap(die, Die.ToMapParams.EDITOR);
        }
    };

    Decoder<Obstacle> OBSTACLE = new Decoder<Obstacle>() {
        @Override public Obstacle decode(Object o) {
            return new Obstacle((String) o);
        }

        @Override public Object encode(Obstacle obstacle) {
            return obstacle.worldObjectName;
        }
    };

    Decoder<StepDetector> STEP_DETECTOR = new Decoder<StepDetector>() {
        @Override public StepDetector decode(Object o) {
            if (o instanceof String)
                return new StepDetector(o.toString(), DieRequirement.ANY);
            Map config = (Map) o;
            return new StepDetector(
                MapHelper.get(config, "name", "default"),
                CreatureRequirementFactory.parse(MapHelper.get(config, "requirement", Collections.emptyMap()))
            );
        }

        @Override public Object encode(StepDetector stepDetector) {
            Map<String, Object> res = new HashMap<String, Object>();
            res.put("name", stepDetector.worldObjectName);
            res.put("requirement", CreatureRequirementFactory.serialize(stepDetector.requirement));
            return res;
        }
    };

    T decode(Object o);

    Object encode(T t);
}
