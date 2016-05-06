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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 10.03.14 by vlaaad
 */
public class IngredientWindow extends GameWindow<IngredientWindow.Params> {

    private boolean buy;
    private Params params;

    @Override protected void doShow(Params params) {
        this.params = params;
        Table content = new Table(Config.skin);
        content.setTouchable(Touchable.enabled);
        content.setBackground(Config.skin.getDrawable("ui-store-window-background"));
        Array<Ability> potions = new Array<Ability>();
        for (Ability p : Config.abilities.byType(Ability.Type.potion)) {
            if (p.ingredients.containsKey(params.item)) potions.add(p);
        }

        LocLabel name = new LocLabel("item-" + params.item.name);
        LocLabel desc = new LocLabel(
            "item-" + params.item.name + "-desc",
            Thesaurus.params()
                .with("enum", Thesaurus.Util.enumerate(Config.thesaurus, potions, new Thesaurus.Util.Stringifier<Ability>() {
                    @Override public String toString(Ability ability) {
                        return ability.locNameKey() + ".gen";
                    }
                }))
        );
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        Image image = new Image(Config.skin, "item/" + params.item.name);

        content.add(image).size(image.getWidth() * 2, image.getHeight() * 2).padBottom(-12).padTop(-6).row();
        content.add(name).padBottom(3).row();
        content.add(new Tile("ui-creature-info-line")).width(50).row();
        Cell cell = content.add(desc).width(130).padLeft(4).padRight(4);
        content.row();

        if (params.item.type == Item.Type.ingredient) {
            Button buyButton = new Button(Config.skin);
            SoundHelper.initButton(buyButton);

            boolean notEnoughCoins = params.userData.getItemCount(Config.items.get("coin")) < params.item.cost;

            buyButton.add(new LocLabel("ui-store-window-buy-for")).padLeft(4);
            buyButton.add(new Image(Config.skin, "item/coin")).padTop(-3).padBottom(-3);
            buyButton.add(String.valueOf(params.item.cost)).padRight(4);
            buyButton.setDisabled(notEnoughCoins);
            buyButton.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    buy = true;
                    hide();
                }
            });
            content.add(buyButton).padTop(3).padBottom(4).row();
            if (notEnoughCoins) {
                int requiredCoins = params.item.cost - params.userData.getItemCount(Config.items.get("coin"));
                String reason = Config.thesaurus.localize(
                    "ui-store-window-coins-needed",
                    Thesaurus.params()
                        .with("count", String.valueOf(requiredCoins))
                        .with("coin-form", "coins." + Thesaurus.Util.countForm(requiredCoins))
                );
                Label label = new Label(reason, Config.skin, "default", StoreWindow.INACTIVE);
                label.setWrap(true);
                label.setAlignment(Align.center);
                content.add(label).width(120).padBottom(3).padTop(-4).row();
            }
        } else {
            cell.padBottom(3);
        }

        table.clearChildren();
        table.add(content);
    }

    @Override protected void onHide() {
        if (buy) {
            params.userData.decrementItemCount(Config.items.get("coin"), params.item.cost);
            params.userData.incrementItemCount(params.item, 1);
            fire(new Bought());
        }
        buy = false;
        params = null;
    }

    public static class Params {
        private final Item item;
        private final UserData userData;

        public Params(Item item, UserData userData) {
            this.item = item;
            this.userData = userData;
        }
    }

    public static class Bought extends Event {}

    public abstract static class Listener implements EventListener {

        @Override public boolean handle(Event event) {
            if (event instanceof Bought) {
                bought();
                return true;
            }
            return false;
        }

        protected abstract void bought();
    }
}
