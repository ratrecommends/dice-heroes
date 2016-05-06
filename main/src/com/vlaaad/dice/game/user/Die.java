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
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.common.util.Numbers;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;

import java.util.*;

/**
 * Created 10.10.13 by vlaaad
 */
public class Die {
    public ProfessionDescription profession;
    public String name;
    public final Array<Ability> abilities;
    public final ObjectIntMap<Ability> inventory;
    public int exp;
    public int renames;

    public static Comparator<? super Die> abilitiesCountComparator(final Ability ability) {
        return new Comparator<Die>() {
            @Override public int compare(Die o1, Die o2) {
                return o1.getAbilitiesCount(ability) - o2.getAbilitiesCount(ability);
            }
        };
    }

    private int getAbilitiesCount(Ability ability) {
        int i = inventory.get(ability, 0);
        for (Ability a : abilities) {
            if (a == ability) i++;
        }
        return i;
    }

    public static class ToMapParams {
        public static final ToMapParams USER_DATA = new ToMapParams(false, true);
        public static final ToMapParams EDITOR = new ToMapParams(true, false);

        public boolean useLevelInsteadOfExp;
        public boolean saveRenamesInfo;

        public ToMapParams(boolean useLevelInsteadOfExp, boolean saveRenamesInfo) {
            this.useLevelInsteadOfExp = useLevelInsteadOfExp;
            this.saveRenamesInfo = saveRenamesInfo;
        }
    }

    public static HashMap<String, Object> toMap(Die die) {
        return toMap(die, ToMapParams.USER_DATA);
    }

    public static HashMap<String, Object> toMap(Die die, ToMapParams params) {
        HashMap<String, Object> result = new LinkedHashMap<String, Object>();
        ArrayList<String> abilities = new ArrayList<String>();
        for (Ability ability : die.abilities) {
            if (ability != null)
                abilities.add(ability.name);
            else
                abilities.add(null);
        }
        Map<String, Integer> inventory = new HashMap<String, Integer>();
        for (Ability ability : die.inventory.keys()) {
            inventory.put(ability.name, die.inventory.get(ability, 0));
        }
        result.put("name", die.name);
        result.put("profession", die.profession.name);
        if (params.useLevelInsteadOfExp) {
            result.put("level", die.getCurrentLevel());
        } else {
            result.put("exp", die.exp);
        }
        if (params.saveRenamesInfo) {
            result.put("renames", die.renames);
        }
        result.put("abilities", abilities);
        if (!inventory.isEmpty())
            result.put("inventory", inventory);
        return result;
    }


    @SuppressWarnings("unchecked")
    public static Die fromMap(Map map) {
        if (map.get("exp") == null) {
            map.put("exp", 1000000);
        }
        String professionName = MapHelper.get(map, "profession", "warrior");
        ProfessionDescription profession = Config.professions.get(professionName);
        int level = MapHelper.get(map, "level", Numbers.MINUS_ONE).intValue();
        if (level != -1) {
            map.put("exp", profession.getExpForLevel(level));
        }

        Array<Ability> abilities = new Array<Ability>();
        Iterable<String> abilitiesRaw = MapHelper.get(map, "abilities");
        if (abilitiesRaw != null) {
            for (String abilityName : abilitiesRaw) {
                if (abilityName == null) {
                    abilities.add(null);
                } else {
                    abilities.add(Config.abilities.get(abilityName));
                }
            }
        }
        ObjectIntMap<Ability> inventory = new ObjectIntMap<Ability>();
        Object o = map.get("inventory");
        if (o instanceof Map) {
            Map<String, Integer> inventoryRaw = MapHelper.get(map, "inventory");
            if (inventoryRaw != null) {
                for (String abilityName : inventoryRaw.keySet()) {
                    inventory.put(Config.abilities.get(abilityName), inventoryRaw.get(abilityName));
                }
            }
        }
        return new Die(
            profession,
            MapHelper.get(map, "name", "Null"),
            MapHelper.get(map, "exp", Numbers.ZERO).intValue(),
            abilities,
            inventory,
            MapHelper.get(map, "renames", Numbers.THREE).intValue()
        );
    }

    public Die() {
        abilities = new Array<Ability>();
        inventory = new ObjectIntMap<Ability>();
    }

    public Die(ProfessionDescription profession, String name, int exp, Array<Ability> abilities, ObjectIntMap<Ability> inventory, int renames) {
        this(profession, name, exp, abilities, inventory);
        this.renames = renames;
    }

    public Die(ProfessionDescription profession, String name, int exp, Array<Ability> abilities, ObjectIntMap<Ability> inventory) {
        this.profession = profession;
        this.name = name;
        this.exp = exp;
        this.abilities = abilities;
        this.inventory = inventory;
        Iterator<Ability> it = abilities.iterator();
        while (it.hasNext()) {
            Ability ability = it.next();
            if (ability != null && !profession.ignoreRequirements && (!ability.requirement.isSatisfied(this))) {
                inventory.getAndIncrement(ability, 0, 1);
                it.remove();
            }
        }
        int used = getUsedSkill();
        int total = getTotalSkill();
        if (used > total) {
            System.out.print(name + " has skill overdraft, " + used + " > " + total + ", inventory = ");
        }
        while (abilities.size > 0 && getUsedSkill() > getTotalSkill()) {
            Ability popped = abilities.pop();
            if (popped != null) inventory.getAndIncrement(popped, 0, 1);
        }
        if (used > total)
            System.out.println(inventory);

        while (abilities.size < 6)
            abilities.add(null);
    }

    public Array<Ability> getProfessionAbilities() {
        return profession.getAvailableAbilities(getCurrentLevel());
    }

    public int getCurrentLevel() {
        return profession.getLevel(exp);
    }

    public int getUsedSkill() {
        int r = 0;
        for (int i = 0; i < abilities.size; i++) {
            Ability ability = abilities.get(i);
            if (ability != null) r += ability.skill;
        }
        return r;
    }

    public IntArray getAvailableIndices(Ability ability) {
        int availableSkill = getTotalSkill() - getUsedSkill();
        IntArray res = new IntArray();
        for (int i = 0; i < abilities.size; i++) {
            Ability check = abilities.get(i);
            if (check == ability)
                continue;
            boolean canBePlaced = check == null ? availableSkill >= ability.skill : availableSkill >= ability.skill - check.skill;
            if (canBePlaced) {
                res.add(i);
            }
        }
        return res;
    }

    public int getTotalSkill() {
        return profession.getSkillForLevel(getCurrentLevel());
    }

    @Override public String toString() {
        return name + " the " + profession + ", level " + getCurrentLevel();
    }

    public float getCurrentProgress() {
        return profession.getLevelProgress(exp);
    }

    public String nameLocKey() {
        return '{' + name.toLowerCase() + '}';
    }

    public int getTotalCount(Ability ability) {
        int result = inventory.get(ability, 0);
        for (Ability a : abilities) {
            if (a == ability) {
                result++;
            }
        }
        return result;
    }

    public Array<Ability> abilities() {
        Array<Ability> result = new Array<Ability>(abilities.size);
        for (Ability a : abilities) {
            if (a == null) a = Config.abilities.get("skip-turn");
            result.add(a);
        }
        return result;
    }
}
