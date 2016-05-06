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

package com.vlaaad.dice.achievements.conditions.util;

import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.filters.AllOfFilter;
import com.vlaaad.common.util.filters.Filter;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.events.imp.EndLevelEvent;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.players.PlayerRelation;

import java.util.Map;

/**
 * Created 16.05.14 by vlaaad
 */
public class ConditionUtil {

    public static void initDieFilters(AllOfFilter<Die> filter, Map map) {
        final String profession = MapHelper.get(map, "profession");
        if (profession != null) {
            filter.add(new Filter<Die>() {
                @Override public boolean accept(Die die) {
                    return die.profession.name.equals(profession);
                }
            });
        }
    }

    public static void initItemFilters(AllOfFilter<Item> filter, Map map) {
        String typeName = MapHelper.get(map, "type");
        if (typeName != null) {
            final Item.Type type = Item.Type.valueOf(typeName);
            filter.add(new Filter<Item>() {
                @Override public boolean accept(Item item) {
                    return item.type == type;
                }
            });
        }
    }

    public static Filter<UserData> createPotionFilters(Map map) {
        AllOfFilter<UserData> result = new AllOfFilter<UserData>();
        final String name = MapHelper.get(map, "name");
        if (name != null) {
            result.add(new Filter<UserData>() {
                @Override public boolean accept(UserData userData) {
                    return userData.potions.get(Config.abilities.get(name), 0) > 0;
                }
            });
        }
        return result;
    }

    public static Filter<Ability> createAbilityFilters(Map map) {
        AllOfFilter<Ability> result = new AllOfFilter<Ability>();
        final String name = MapHelper.get(map, "name");
        if (name != null) {
            result.add(new Filter<Ability>() {
                @Override public boolean accept(Ability ability) {
                    return ability.name.equals(name);
                }
            });
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Filter<EndLevelEvent> createLevelFilter(Map map) {
        if (map == null || map.isEmpty()) return Filter.ANY;
        AllOfFilter<EndLevelEvent> result = new AllOfFilter<EndLevelEvent>();
        final Boolean success = MapHelper.get(map, "success");
        if (success != null) {
            result.add(new Filter<EndLevelEvent>() {
                @Override public boolean accept(EndLevelEvent event) {
                    return event.isWin() == success;
                }
            });
        }
        final String mapName = MapHelper.get(map, "level");
        if (mapName != null) {
            final BaseLevelDescription level = Config.levels.get(mapName);
            result.add(new Filter<EndLevelEvent>() {
                @Override public boolean accept(EndLevelEvent event) {
                    return event.level() == level;
                }
            });
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Filter<Creature> createCreatureFilter(Map map) {
        if (map == null || map.isEmpty())
            return Filter.ANY;
        AllOfFilter<Creature> result = new AllOfFilter<Creature>();
        String fractionName = MapHelper.get(map, "player");
        if (fractionName != null) {
            final PlayerRelation relation = PlayerRelation.valueOf(fractionName);
            result.add(new Filter<Creature>() {
                @Override public boolean accept(Creature creature) {
                    return creature.player.inRelation(creature.viewer, relation);
                }
            });
        }
        String professionName = MapHelper.get(map, "profession");
        if (professionName != null) {
            final ProfessionDescription profession = Config.professions.get(professionName);
            result.add(new Filter<Creature>() {
                @Override public boolean accept(Creature creature) {
                    return creature.profession == profession;
                }
            });
        }
        return result;
    }
}
