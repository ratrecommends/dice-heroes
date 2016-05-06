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

package com.vlaaad.dice.game.user;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.signals.Signal;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.events.EventType;
import com.vlaaad.dice.achievements.events.imp.DieEvent;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;

import java.util.*;

/**
 * Created 10.10.13 by vlaaad
 */
public class UserData implements Iterable<Item> {


    public static UserData deserialize(Object load) {
        return deserialize(new UserData(), load);
    }

    @SuppressWarnings("unchecked")
    public static UserData deserialize(UserData result, Object load) {
        HashMap map = (HashMap) load;
        Object levelsData = map.get("levels");

        final String strUuid = MapHelper.get(map, "uuid");
        result.uuid = strUuid == null ? UUID.randomUUID().toString() : strUuid;
        result.tutorialCompleted = MapHelper.get(map, "tutorialCompleted", Boolean.FALSE);
        result.initiativeTutorialCompleted = MapHelper.get(map, "initiativeTutorialCompleted", Boolean.FALSE);
        result.professionAbilitiesTutorialCompleted = MapHelper.get(map, "professionAbilitiesTutorialCompleted", Boolean.FALSE);
        result.potionsAvailable = MapHelper.get(map, "potionsAvailable", Boolean.FALSE);
        result.potionsTutorialCompleted = MapHelper.get(map, "potionsTutorialCompleted", Boolean.FALSE);
        result.playPotionsTutorialCompleted = MapHelper.get(map, "playPotionsTutorialCompleted", Boolean.FALSE);
        result.spawnSwipeTutorialCompleted = MapHelper.get(map, "spawnSwipeTutorialCompleted", Boolean.FALSE);
        result.bossTutorialCompleted = MapHelper.get(map, "bossTutorialCompleted", Boolean.FALSE);

        result.setDonated(MapHelper.get(map, "donated", Boolean.FALSE));

        Map<String, Object> achievements = MapHelper.get(map, "achievements");
        if (achievements != null) result.achievements.putAll(achievements);

        if (levelsData != null) {
            ArrayList<String> levels = (ArrayList<String>) levelsData;
            for (String levelName : levels) {
                result.passedLevels.add(Config.levels.get(levelName));
            }
        }
        Object diceData = map.get("dice");
        if (diceData != null) {
            ArrayList<HashMap> dice = (ArrayList<HashMap>) diceData;
            for (HashMap dieData : dice) {
                result.ownedDice.add(Die.fromMap(dieData));
            }
        }
        Object last = map.get("last-level");
        if (last != null) {
            String lastLevelName = (String) last;
            result.lastPassedLevel = Config.levels.get(lastLevelName);
        }
        Object itemData = map.get("items");
        if (itemData != null) {
            HashMap<String, Integer> items = (HashMap<String, Integer>) itemData;
            for (String itemName : items.keySet()) {
                result.items.put(Config.items.get(itemName), items.get(itemName));
            }
        }
        Map<String, Integer> potions = MapHelper.get(map, "potions");
        if (potions != null) {
            for (Map.Entry<String, Integer> m : potions.entrySet()) {
                result.potions.put(Config.abilities.get(m.getKey()), m.getValue());
            }
        }
        return result;
    }

    public static Map<String, Object> serialize(UserData data) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        ArrayList<String> levels = new ArrayList<String>();
        for (BaseLevelDescription level : data.passedLevels) {
            levels.add(level.name);
        }

        ArrayList<HashMap<String, Object>> dice = new ArrayList<HashMap<String, Object>>();
        for (Die dieDescription : data.ownedDice) {

            dice.add(Die.toMap(dieDescription));
        }
        HashMap<String, Integer> items = new HashMap<String, Integer>();
        for (Item item : data.items.keys()) {
            int value = data.items.get(item, 0);
            if (value == 0)
                continue;
            items.put(item.name, value);
        }
        Map<String, Integer> potions = new HashMap<String, Integer>();
        for (Ability a : data.potions.keys()) {
            int count = data.potions.get(a, 0);
            if (count != 0)
                potions.put(a.name, count);
        }
        result.put("uuid", data.uuid);
        result.put("potions", potions);
        result.put("levels", levels);
        result.put("dice", dice);
        result.put("items", items);
        result.put("last-level", data.lastPassedLevel == null ? null : data.lastPassedLevel.name);
        result.put("tutorialCompleted", data.tutorialCompleted);
        result.put("initiativeTutorialCompleted", data.initiativeTutorialCompleted);
        result.put("professionAbilitiesTutorialCompleted", data.professionAbilitiesTutorialCompleted);
        result.put("potionsAvailable", data.potionsAvailable);
        result.put("potionsTutorialCompleted", data.potionsTutorialCompleted);
        result.put("playPotionsTutorialCompleted", data.playPotionsTutorialCompleted);
        result.put("spawnSwipeTutorialCompleted", data.spawnSwipeTutorialCompleted);
        result.put("bossTutorialCompleted", data.bossTutorialCompleted);
        result.put("donated", data.donated);
        result.put("achievements", data.achievements);
        return result;
    }

    public final ObjectIntMap<Ability> potions = new ObjectIntMap<Ability>();

    public final Signal<Boolean> onDonated = new Signal<Boolean>();
    private boolean donated;
    public boolean tutorialCompleted;
    public boolean initiativeTutorialCompleted;
    public boolean professionAbilitiesTutorialCompleted;
    public boolean potionsTutorialCompleted;
    public boolean spawnSwipeTutorialCompleted;
    public boolean playPotionsTutorialCompleted;
    public boolean potionsAvailable;
    public boolean bossTutorialCompleted;
    public final Signal<BaseLevelDescription> levelPassed = new Signal<BaseLevelDescription>();
    public final Signal<Item> itemCountChanged = new Signal<Item>();
    public final Signal<Die> dieAdded = new Signal<Die>();
    public final Signal<Ability> onPotionCountChanged = new Signal<Ability>();

    private final ObjectSet<BaseLevelDescription> passedLevels = new ObjectSet<BaseLevelDescription>();
    private final Array<Die> ownedDice = new Array<Die>();
    private BaseLevelDescription lastPassedLevel;
    private final ObjectIntMap<Item> items = new ObjectIntMap<Item>();
    public final Map<String, Object> achievements = new HashMap<String, Object>();
    private String uuid;

    public String uuid() {
        return uuid;
    }

    public void addLevelToPassed(BaseLevelDescription level) {
        if (passedLevels.add(level)) {
            levelPassed.dispatch(level);
        }
        lastPassedLevel = level;
    }

    public int numPassedLevels() {
        return passedLevels.size;
    }

    public boolean isPassed(BaseLevelDescription level) {
        return passedLevels.contains(level);
    }

    public BaseLevelDescription getLastPassedLevel() {
        return lastPassedLevel;
    }

    @Override public Iterator<Item> iterator() {
        return items.keys().iterator();
    }

    public int getItemCount(Item item) {
        return items.get(item, 0);
    }

    public Iterable<Die> dice() {
        return ownedDice;
    }

    public void incrementItemCount(Item item, int value) {
        setItemCount(item, getItemCount(item) + value);
    }

    public void decrementItemCount(Item item, int by) {
        setItemCount(item, getItemCount(item) - by);
    }

    public void setItemCount(Item item, int value) {
        if (value < 0)
            throw new IllegalArgumentException("item count can't be negative!");
        if (value == items.get(item, 0))
            return;
        if (value == 0) {
            items.remove(item, 0);
        } else {
            items.put(item, value);
        }
        itemCountChanged.dispatch(item);
    }

    public Map toMap() {
        return UserData.serialize(this);
    }

    public void addDie(Die description) {
        if (ownedDice.contains(description, true))
            throw new IllegalArgumentException("Already have die " + description);
        ownedDice.insert(0, description);
        dieAdded.dispatch(description);

        Config.achievements.fire(EventType.obtainDie, Pools.obtain(DieEvent.class).die(description));
    }

    public int diceCount() {
        return ownedDice.size;
    }

    public Die findDieByName(String name) {
        String lowerCased = name.toLowerCase();
        for (Die die : dice()) {
            if (die.name.toLowerCase().equals(lowerCased))
                return die;
        }
        return null;
    }

    public Array<BaseLevelDescription> getAvailableLevels(Array<BaseLevelDescription> target) {
        for (BaseLevelDescription level : Config.levels) {
            if (level.hidden || passedLevels.contains(level))
                continue;
            String parentName = level.parent;
            if (parentName == null || passedLevels.contains(Config.levels.get(parentName)))
                target.add(level);
        }
        return target;
    }

    public void setDonated(boolean donated) {
        if (donated == this.donated)
            return;
        this.donated = donated;
        onDonated.dispatch(donated);
        if (donated && Config.achievements != null) {
            Config.achievements.fire(EventType.donated);
        }
    }

    public boolean isDonated() {
        return donated;
    }

    public void withdrawItems(ObjectIntMap<Item> cost) {
        for (Item item : cost.keys()) {
            decrementItemCount(item, cost.get(item, 0));
        }
    }

    public void addPotion(Ability a) {
        potions.getAndIncrement(a, 0, 1);
        onPotionCountChanged.dispatch(a);
    }

    public int getPotionCount(Ability potion) {
        return potions.get(potion, 0);
    }

    public boolean hasPotions() {
        return potions.size > 0;
    }

    public Iterable<? extends Ability> potions() {
        return potions.keys();
    }

    public void setPotions(ObjectIntMap<Ability> potionCount) {
        potions.clear();
        for (Ability potion : potionCount.keys()) {
            int currentCount = potions.get(potion, 0);
            int newCount = potionCount.get(potion, 0);
            if (currentCount != newCount) {
                potions.put(potion, newCount);
                onPotionCountChanged.dispatch(potion);
            }
        }
    }

    public boolean hasItems(Item.Type type) {
        for (Item item : items.keys()) {
            if (item.type == type && items.get(item, 0) > 0)
                return true;
        }
        return false;
    }

    public boolean canWithdraw(ObjectIntMap<Item> items) {
        for (Item item : items.keys()) {
            int count = items.get(item, 0);
            if (count > this.items.get(item, 0))
                return false;
        }
        return true;
    }

    public boolean hasPotion(Ability ability) {
        return potions.get(ability, 0) > 0;
    }

    public int potionsCount() {
        int r = 0;
        ObjectIntMap.Values v = potions.values();
        while (v.hasNext()) {
            r += v.next();
        }
        return r;
    }

    public int itemsCount(Item.Type type) {
        int r = 0;
        for (ObjectIntMap.Entry<Item> e : items.entries()) {
            if (e.key.type == type) r += e.value;
        }
        return r;
    }
}
