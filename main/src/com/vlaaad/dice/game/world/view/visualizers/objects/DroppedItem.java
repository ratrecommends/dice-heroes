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

package com.vlaaad.dice.game.world.view.visualizers.objects;

import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.objects.Creature;

import java.util.Comparator;

/**
 * Created 06.03.14 by vlaaad
 */
public class DroppedItem {
    public static final Comparator<? super DroppedItem> ORDER_COMPARATOR = new Comparator<DroppedItem>() {
        @Override public int compare(DroppedItem o1, DroppedItem o2) {
            return Item.ORDER_COMPARATOR.compare(o1.item, o2.item);
        }
    };
    public final Creature target;
    public final Item item;
    public final int count;

    public DroppedItem(Creature target, Item item, int count) {
        this.target = target;
        this.item = item;
        this.count = count;
    }
}
