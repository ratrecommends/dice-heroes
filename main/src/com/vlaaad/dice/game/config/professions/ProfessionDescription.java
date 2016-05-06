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

package com.vlaaad.dice.game.config.professions;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.Option;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.user.Die;

import java.util.Collections;
import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public class ProfessionDescription {

    private static final Die tmp = new Die();

    public final String name;
    public final String id;
    public final int defaultActionPoints;
    public final float moveRadius;
    private final IntArray expForLevels = new IntArray();
    private final IntArray skillForLevels = new IntArray();
    private final Array<Ability> abilities = new Array<Ability>();
    public final boolean ignoreRequirements;
    public final Option<Ability> applyOnCreate;
    public final ObjectMap<Attribute, Object> attributes;

    public ProfessionDescription(Map data) {
        this.name = MapHelper.get(data, "name");
        String id = MapHelper.get(data, "id");
        if (id == null) throw new IllegalStateException("profession " + name + " does not have any id!");
        this.id = id;
        this.applyOnCreate = Config.abilities.optional(MapHelper.get(data, "apply-on-create", "undefined-ability"));
        this.defaultActionPoints = MapHelper.get(data, "ap", Numbers.ONE).intValue();
        this.moveRadius = MapHelper.get(data, "move-radius", Numbers.ONE_AND_A_HALF).floatValue();
        this.ignoreRequirements = MapHelper.get(data, "ignore-requirements", Boolean.FALSE);
        expForLevels.add(0);
        for (Integer exp : MapHelper.<Iterable<Integer>>get(data, "levels")) {
            expForLevels.add(exp);
        }
        skillForLevels.add(0);
        for (Integer skill : MapHelper.<Iterable<Integer>>get(data, "skills")) {
            skillForLevels.add(skill);
        }
        tmp.profession = this;
        for (Ability ability : Config.abilities.byType(Ability.Type.profession)) {
            if (ability.requirement.canBeSatisfied(tmp)) {
                abilities.add(ability);
            }
        }
        abilities.sort(Ability.COST_COMPARATOR);
        this.attributes = new ObjectMap<Attribute, Object>();
        Map<String, Object> attributes = MapHelper.get(data, "attributes", Collections.<String, Object>emptyMap());
        for (String key : attributes.keySet()) {
            this.attributes.put(Attribute.valueOf(key), attributes.get(key));
        }
    }

    public int getSkillForLevel(int level) {
        if (level < 0)
            return 0;
        if (level >= skillForLevels.size)
            return skillForLevels.peek();
        return skillForLevels.get(level);
    }

    public Array<Ability> getAvailableAbilities() {
        return new Array<Ability>(abilities);
    }

    public Array<Ability> getAvailableAbilities(int level) {
        tmp.profession = this;
        tmp.exp = getExpForLevel(level);
        Array<Ability> result = new Array<Ability>(0);
        for (Ability ability : abilities) {
            if (ability.requirement.isSatisfied(tmp)) {
                result.add(ability);
            }
        }
        return result;
    }

    public int getLevel(int exp) {
        for (int i = expForLevels.size - 1; i >= 0; i--) {
            if (exp >= expForLevels.get(i))
                return i + 1; //return level, not an index
        }
        return 0;
    }

    public int getMaxLevel() {
        return expForLevels.size;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getExpForLevel(int level) {
        return getExpForNextLevel(level - 1);
    }

    public int getExpForNextLevel(int level) {
        if (level < 0)
            return 0;
        if (level >= expForLevels.size)
            return expForLevels.peek();
        return expForLevels.get(level);
    }

    public float getLevelProgress(int exp) {
        int level = getLevel(exp);
        int expForCurrentLevel = getExpForNextLevel(level - 1);
        int expForNextLevel = getExpForNextLevel(level);
        if (expForCurrentLevel == expForNextLevel) {
            return 1f;
        } else {
            return ((float) (exp - expForCurrentLevel)) / ((float) (expForNextLevel - expForCurrentLevel));
        }
    }

    public String locKey() {
        return "{profession-" + name.toLowerCase() + '}';
    }
}
