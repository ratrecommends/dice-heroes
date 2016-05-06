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

package com.vlaaad.dice.ui.windows;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.purchases.ItemPurchaseInfo;
import com.vlaaad.dice.game.config.purchases.PurchaseData;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 30.10.13 by vlaaad
 */
public class PurchaseWindow extends GameWindow<PurchaseWindow.Params> {

    private static final Color NAME_COLOR = new Color(1, 1, 1, 0.8f);
    private static final Color COUNT_COLOR = new Color(1, 1, 1, 0.5f);


    private Params params;
    private PurchaseInfo chosen;

    @Override protected void initialize() {
        background.setDrawable(Config.skin.getDrawable("ui-reward-window-background"));
    }

    @Override protected void doShow(Params params) {
        table.clearChildren();
        Table items = new Table(Config.skin);
        items.setBackground("ui-purchase-window-background");
        items.add(new LocLabel("ui-purchase-window-header")).padBottom(4).row();
        items.defaults().padBottom(-1).padLeft(-1).padRight(-1);
        this.params = params;
        Array<ItemPurchaseInfo> infos = new Array<ItemPurchaseInfo>();
        for (PurchaseInfo i : PurchaseData.infos()) {
            if (!(i instanceof ItemPurchaseInfo))
                continue;
            ItemPurchaseInfo info = (ItemPurchaseInfo) i;
            if (info.itemName.equals(params.item.name) && !info.hidden) {
                infos.add(info);
            }
        }
        int i = 0;
        for (ItemPurchaseInfo info : infos) {
            Cell cell = add(items, info);

            if (i == 0) {
                cell.padTop(-1);
            }
            cell.row();
            i++;
        }
        table.add(items).padLeft(-1).padRight(-1).padTop(-1);
    }

    private Cell add(Table items, final ItemPurchaseInfo info) {
        Button button = new Button(Config.skin, "purchase-item");
        SoundHelper.initButton(button);
        button.align(Align.left);
        Table description = new Table(Config.skin);
        description.align(Align.left);
        description.add(new LocLabel("ui-purchase-item-" + info.itemName + "-" + info.count + "-name", NAME_COLOR)).align(Align.left).padTop(-1).row();
        description.add(new LocLabel("ui-purchase-item-" + info.itemName + "-" + info.count + "-count", COUNT_COLOR)).align(Align.left).padTop(-4);
        button.add(new Image(Config.skin, "ui-purchase-item-" + info.itemName + "-" + info.count)).padLeft(2);
        button.add(description).pad(2).padLeft(4);
        if (info.discount != 0) {
            Group discount = new Group();
            discount.setTouchable(Touchable.disabled);
            discount.setTransform(false);
            discount.addActor(new Image(Config.skin, "ui-discount-icon"));
            Label label = new Label(info.discount + "%", Config.skin);
            label.setPosition(7, 5);
            discount.addActor(label);
            button.addActor(discount);
            discount.setPosition(115, 5);
        }
        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                chosen = info;
                hide();
            }
        });
        return items.add(button).width(140);
    }

    @Override protected void onHide() {
        if (chosen != null) {
            params.callback.onBuy(chosen);
            chosen = null;
        }
        params = null;
    }

    public static class Params {
        private final Item item;
        private final Callback callback;

        public Params(Item item, Callback callback) {
            this.item = item;
            this.callback = callback;
        }
    }

    public static interface Callback {
        public void onBuy(PurchaseInfo info);
    }
}
