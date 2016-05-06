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

package com.vlaaad.dice.game.config;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.requirements.DieRequirement;
import com.vlaaad.dice.game.requirements.imp.AllOf;
import com.vlaaad.dice.game.requirements.imp.LevelRequirement;
import com.vlaaad.dice.game.requirements.imp.ProfessionRequirement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public class CreatureRequirementFactory {
    private static final ObjectMap<String, Class<? extends DieRequirement>> types = new ObjectMap<String, Class<? extends DieRequirement>>();

    static {
        types.put("profession", ProfessionRequirement.class);
        types.put("level", LevelRequirement.class);
    }

    public static DieRequirement create(String name, Object setup) {
        if (!types.containsKey(name))
            throw new IllegalArgumentException("unknown requirement type: " + name);
        try {
            return types.get(name).newInstance().init(setup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CreatureRequirementFactory() {
    }

    public static DieRequirement parse(Map req) {
        if (req == null) return DieRequirement.ANY;
        if (req.size() > 1) {
            return new AllOf(req);
        } else if (req.size() == 1) {
            Object key = req.keySet().iterator().next();
            return CreatureRequirementFactory.create(key.toString(), req.get(key));
        } else {
            return DieRequirement.ANY;
        }

    }

    public static Object serialize(DieRequirement requirement) {
        if (requirement == DieRequirement.ANY)
            return null;
        Map<String, Object> res = new HashMap<String, Object>();
        if (requirement instanceof AllOf) {
            for (DieRequirement inner : ((AllOf) requirement).requirements) {
                put(res, inner);
            }
        } else {
            put(res, requirement);
        }
        return res;
    }

    private static void put(Map<String, Object> res, DieRequirement requirement) {
        final String key = types.findKey(requirement.getClass(), true);
        res.put(key, requirement.getConfig());
    }
}
