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

package com.vlaaad.dice.game.config.purchases;

import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.managers.SoundManager;

/**
 * Created 03.03.14 by vlaaad
 */
public class ItemPurchaseInfo extends PurchaseInfo {

    public final String itemName;
    public final int count;
    public final boolean hidden;
    public final int discount;

    public ItemPurchaseInfo(String itemName, int count, boolean hidden, int discount) {
        this.itemName = itemName;
        this.count = count;
        this.hidden = hidden;
        this.discount = discount;
    }

    @Override public String skuName() {
        return itemName + count;
    }

    @Override public void apply(UserData userData) {
        Item item = Config.items.get(itemName);
        userData.setItemCount(item, userData.getItemCount(item) + count);
        SoundManager.instance.playSoundIfExists("coins");
    }
}
