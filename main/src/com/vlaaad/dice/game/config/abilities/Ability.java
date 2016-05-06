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

package com.vlaaad.dice.game.config.abilities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.common.util.StringHelper;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.results.imp.MoveResult;
import com.vlaaad.dice.game.config.CreatureActionFactory;
import com.vlaaad.dice.game.config.CreatureRequirementFactory;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.requirements.DieRequirement;
import com.vlaaad.dice.game.requirements.imp.AllOf;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public class Ability {
    private static final Array<Grid2D.Coordinate> tmp = new Array<Grid2D.Coordinate>();

    public static final Comparator<Ability> COST_COMPARATOR = new Comparator<Ability>() {
        @Override public int compare(Ability o1, Ability o2) {
            return o2.cost - o1.cost;
        }
    };
    public static final Comparator<Creature> INITIATIVE_COMPARATOR = new Comparator<Creature>() {
        @Override public int compare(Creature o1, Creature o2) {
            return getInitiative(o2) - getInitiative(o1);
        }

        private int getInitiative(Creature creature) {
            int result = 0;
            for (int i = 0, n = creature.abilities.size; i < n; i++) {
                Ability a = creature.abilities.get(i);
                result += a.initiative;
            }
            result *= 10;
            result /= creature.abilities.size;
            if (creature.world != null) {
                tmp.clear();
                result += MoveResult.fillAvailableCoordinates(tmp, creature).size;
            }
            return result;
        }
    };

    public static Comparator<Ability> shopComparator(final Die die) {
        return new Comparator<Ability>() {
            @Override public int compare(Ability o1, Ability o2) {
                return o1.cost - o2.cost;
            }
        };
    }

    public ObjectMap<String, String> fillDescriptionParams(Die die) {
        Creature creature = new Creature(die, PlayerHelper.defaultProtagonist);
        return fillDescriptionParams(Thesaurus.params(), creature);
    }

    public static Comparator<? super Ability> countComparator(UserData userData) {
        final ObjectIntMap<Ability> counts = new ObjectIntMap<Ability>();
        for (Die die : userData.dice()) {
            for (ObjectIntMap.Entry<Ability> e : die.inventory)
                counts.getAndIncrement(e.key, e.value, e.value);
            for (Ability ability : die.abilities)
                if (ability != null) counts.getAndIncrement(ability, 0, 1);
        }
        return new Comparator<Ability>() {
            @Override public int compare(Ability o1, Ability o2) {
                return counts.get(o1, 0) - counts.get(o2, 0);
            }
        };
    }

    public enum Type {
        util, wearable, potion, profession
    }

    public final int skill;
    public final String name;
    public final Map description;
    public CreatureAction action;
    public DieRequirement requirement;
    public final int cost;
    public final int sellCost;
    public final int initiative;
    public final ObjectIntMap<Item> ingredients;
    public final Type type;
    public final String id;
    public final String soundName;

    public Ability(Map info) {
        description = info;
        name = MapHelper.get(info, "name");
        soundName = MapHelper.get(info, "sound-name", "ability-" + name);
        String typeName = MapHelper.get(info, "type");
        if (typeName == null)
            throw new IllegalStateException("type not defined in " + name);
        type = Type.valueOf(typeName);
        cost = MapHelper.get(info, "cost", Numbers.ZERO).intValue();
        sellCost = MapHelper.get(info, "sell", (Number) (MathUtils.ceil(cost / 2))).intValue();
        initiative = MapHelper.get(info, "initiative", Numbers.ZERO).intValue();
        skill = MapHelper.get(info, "skill", Numbers.ONE).intValue();
        String id = MapHelper.get(info, "id");
        if (id == null) throw new IllegalStateException("ability " + name + " does not have any id!");
        this.id = id;
        HashMap<String, Object> req = MapHelper.get(info, "requirement");
        this.ingredients = new ObjectIntMap<Item>();
        Map<String, Integer> ing = MapHelper.get(info, "ingredients");
        if (ing != null) {
            for (String itemName : ing.keySet()) {
                Item item = Config.items.get(itemName);
                if (item.type != Item.Type.ingredient && item.type != Item.Type.anyIngredient)
                    throw new IllegalStateException(item + " in " + name + " ability is not an ingredient");
                ingredients.put(item, ing.get(itemName));
            }
        }

        requirement = CreatureRequirementFactory.parse(req);

        HashMap<String, Object> ac = MapHelper.get(info, "action");
        if (ac != null) {
            if (ac.size() != 1)
                throw new IllegalStateException(ac + " should contain 1 element");
            for (String name : ac.keySet()) {
                action = CreatureActionFactory.create(name, ac.get(name), this);
            }
        } else {
            action = CreatureAction.doNothing(this);
        }
    }

    @Override public String toString() {
        return name + ":" + action;
    }

    public String locNameKey() {
        return "ability-" + name;
    }

    public String locDescKey() {
        return "ability-" + name + "-desc";
    }

    public Thesaurus.Params fillDescriptionParams(Thesaurus.Params params, Creature creature) {
        action.fillDescriptionParams(params, creature);
        return params;
    }
}