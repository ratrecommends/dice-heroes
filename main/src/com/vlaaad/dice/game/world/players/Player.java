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

package com.vlaaad.dice.game.world.players;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.players.tutorial.TutorialProvider;

/**
 * Created 08.07.14 by vlaaad
 */
public class Player {

    public final Fraction fraction;
    private final ObjectMap<Fraction, FractionRelation> relations;
    public final ObjectIntMap<Ability> potions = new ObjectIntMap<Ability>();
    public final ObjectIntMap<Item> earnedItems = new ObjectIntMap<Item>();
    public final ObjectSet<Creature> creatures = new ObjectSet<Creature>();
    public final ObjectSet<Die> dice = new ObjectSet<Die>();
    public final ObjectIntMap<Ability> usedPotions = new ObjectIntMap<Ability>();
    public TutorialProvider tutorialProvider = TutorialProvider.NO_TUTORIALS;
    private int nextSummonedId = 10000000;

    /**
     * @param relations map like (enemy -> enemy, strangers -> ally). Must contain relations for any other fraction.
     */
    public Player(Fraction fraction, ObjectMap<Fraction, FractionRelation> relations) {
        this.fraction = fraction;
        this.relations = relations;
    }

    public Creature addCreature(Die die) {
        Creature creature = new Creature(die, this);
        addCreature(creature);
        return creature;
    }

    public void addDie(Die die) {
        dice.add(die);
    }

    public void addCreature(Creature creature) {
        creatures.add(creature);
    }

    public FractionRelation getFractionRelation(Player that) {
        if (this == that)
            return FractionRelation.ally;
        FractionRelation res = relations.get(that.fraction);
        if (res == null)
            throw new IllegalStateException("undefined relation between " + this + " and " + that + ": " + relations);
        return res;
    }

    /**
     * @return self, ally or enemy
     */
    public PlayerRelation getPlayerRelation(Player that) {
        if (this == that)
            return PlayerRelation.self;
        return relations.get(that.fraction) == FractionRelation.ally ? PlayerRelation.ally : PlayerRelation.enemy;
    }

    public boolean inRelation(Player that, PlayerRelation relation) {
        switch (relation) {
            case any:
                return true;
            case none:
                return false;

            case self:
                return this == that;
            case allyOrEnemy:
                return this != that;

            case ally:
                return this != that && relations.get(that.fraction) == FractionRelation.ally;
            case enemy:
                return this != that && relations.get(that.fraction) == FractionRelation.enemy;

            case allyOrSelf:
                return this == that || relations.get(that.fraction) == FractionRelation.ally;
            case enemyOrSelf:
                return this == that || relations.get(that.fraction) == FractionRelation.enemy;

            default:
                throw new IllegalArgumentException("unknown relation: " + relation);
        }
    }

    public void setPotions(ObjectIntMap<Ability> potions) {
        this.potions.putAll(potions);
    }

    public void earn(ObjectIntMap<Item> items) {
        for (Item item : items.keys()) {
            earnedItems.getAndIncrement(item, 0, items.get(item, 0));
        }
    }

    public Iterable<? extends Ability> potions() {
        return potions.keys();
    }

    public int getPotionCount(Ability potion) {
        return potions.get(potion, 0);
    }

    public boolean hasPotions() { return potions.size > 0; }

    public void onUsePotion(Ability potion) {
        potions.getAndIncrement(potion, 0, -1);
        usedPotions.getAndIncrement(potion, 0, 1);
        int count = potions.get(potion, 0);
        if (count == 0) {
            potions.remove(potion, 0);
        } else if (count < 0) {
            throw new IllegalStateException("no more potions!");
        }
    }

    @Override public String toString() {
        return "@" + fraction.toString() + "#" + Integer.toHexString(hashCode());
    }
    public int nextSummonedId() {
        return nextSummonedId++;
    }
}
