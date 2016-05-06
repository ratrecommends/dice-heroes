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

package com.vlaaad.dice.ui.components;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.util.SoundHelper;

import java.util.Comparator;

/**
 * Created 29.10.13 by vlaaad
 */
public class DieStore extends WidgetGroup {

    private static final int NUM_ROWS = 2;
    private static final int NUM_ITEMS = 5;

    private final Die die;
    private final UserData userData;
    private final Callback callback;
    private final Table table = new Table(Config.skin);
    private final ObjectMap<Ability, StoreIcon> iconsByAbilities = new ObjectMap<Ability, StoreIcon>();
    private final Comparator<Ability> shopComparator;


    public DieStore(Die die, UserData userData, Callback callback) {
        this.die = die;
        this.userData = userData;
        this.callback = callback;
        this.shopComparator = Ability.shopComparator(die);
        table.setTouchable(Touchable.enabled);
        table.align(Align.top | Align.left);
        addActor(table);
        refresh();
        setSize(getPrefWidth(), getPrefHeight());
    }


    public void refresh() {
        table.clearChildren();
        iconsByAbilities.clear();
        Array<Ability> availableAbilities = new Array<Ability>();
        for (Ability ability : Config.abilities.byType(Ability.Type.wearable)) {
            if (ability.cost < 0 || !ability.requirement.canBeSatisfied(die))
                continue;
            availableAbilities.add(ability);
        }
        availableAbilities.sort(shopComparator);
        for (int i = 0; i < NUM_ROWS; i++) {
            Table row = new Table(Config.skin);
            row.setBackground("ui/dice-window/store-background");
            row.align(Align.top | Align.left);
            for (int j = 0; j < NUM_ITEMS; j++) {
                int idx = i * NUM_ITEMS + j;
                if (availableAbilities.size > idx) {
                    Ability ability = availableAbilities.get(idx);
                    StoreIcon icon = new StoreIcon(ability, die, userData, createCallback(ability));
                    SoundHelper.init(icon);
                    iconsByAbilities.put(ability, icon);
                    row.add(icon).padLeft(2).padRight(2).padTop(-2).size(20, 20);
                }
            }
            table.add(row).fillX().height(22).row();
        }
    }

    private StoreIcon.Callback createCallback(final Ability ability) {
        return new StoreIcon.Callback() {
            @Override public void onBuy() {
                int cost = ability.cost;
                Item coin = Config.items.get("coin");
                if (cost > userData.getItemCount(coin))
                    return;
                die.inventory.getAndIncrement(ability, 0, 1);
                userData.setItemCount(coin, userData.getItemCount(coin) - cost);
                SoundManager.instance.playSound("coins");
//                refresh();
                fire(RefreshEvent.INSTANCE);
                callback.onAbilityBought(ability);
            }
        };
    }

    @Override public void layout() {
        table.setSize(getWidth(), getHeight());
        table.invalidate();
        table.validate();
    }

    @Override public float getPrefHeight() {
        return table.getPrefHeight();
    }

    @Override public float getPrefWidth() {
        return table.getPrefWidth();
    }

    public StoreIcon getIconByAbility(Ability ability) {
        return iconsByAbilities.get(ability);
    }

    public static interface Callback {
        public void onAbilityBought(Ability ability);
    }
}
