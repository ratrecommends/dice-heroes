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

package com.vlaaad.dice.game.config.items;

import java.util.Comparator;

/**
 * Created 10.10.13 by vlaaad
 */
public class Item {
    public static final Comparator<Item> ORDER_COMPARATOR = new Comparator<Item>() {
        @Override public int compare(Item o1, Item o2) {
            return o2.order - o1.order;
        }
    };

    public String name;
    public String id;
    public int order;
    public Type type;
    public int cost;

    public Item() {
    }

    public Item(String name, int order, Type type) {
        this.name = name;
        this.order = order;
        this.type = type;
    }

    @Override public String toString() {
        return name;
    }

    public static enum Type {
        resource, ingredient, anyIngredient
    }
}
