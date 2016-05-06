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
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 29.10.13 by vlaaad
 */
public class StoreWindow extends GameWindow<StoreWindow.Params> {

    public static final Color INACTIVE = new Color(1, 1, 1, 0.3f);
    public static final int INVENTORY_SIZE = 10;

    private boolean buy;
    private Params params;
    public Button buyButton;

    @Override protected void initialize() {
    }

    @Override protected void doShow(Params params) {
        this.params = params;
        Table info = new Table(Config.skin);
        info.setTouchable(Touchable.enabled);
        info.setTransform(true);
        info.setBackground(Config.skin.getDrawable("ui-store-window-background"));

        Image line = new Image(Config.skin, "ui-creature-info-line");

        Label desc = new LocLabel(params.ability.locDescKey(), params.ability.fillDescriptionParams(params.die));
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        boolean requirementsNotSatisfied = !params.ability.requirement.isSatisfied(params.die);

        int abilityCount = params.die.inventory.get(params.ability, 0);
        for (Ability ability : params.die.abilities) {
            if (params.ability == ability)
                abilityCount++;
        }
        boolean maxCountReached = abilityCount >= 6;

        boolean notEnoughCoins = params.userData.getItemCount(Config.items.get("coin")) < params.ability.cost;

        boolean inventoryIsFull = MapHelper.countPositive(params.die.inventory) >= INVENTORY_SIZE;

        buyButton = new Button(Config.skin);
        SoundHelper.initButton(buyButton);
        buyButton.add(new LocLabel("ui-store-window-buy-for")).padLeft(4);
        buyButton.add(new Image(Config.skin, "item/coin")).padTop(-3).padBottom(-3);
        buyButton.add(String.valueOf(params.ability.cost)).padRight(4);
        buyButton.setDisabled(requirementsNotSatisfied || maxCountReached || notEnoughCoins || inventoryIsFull);
        buyButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                buy();
            }
        });

        Image image = new Image(Config.skin, "ability/" + params.ability.name + "-icon");

        info.add(image).size(image.getWidth() * 2, image.getHeight() * 2).padBottom(-12).padTop(-6).row();
        info.add(new LocLabel(params.ability.locNameKey())).padBottom(3).row();
        info.add(line).width(50).row();
        info.add(desc).width(120).padLeft(4).padRight(4).row();
        info.add(new LocLabel("ui-needed-skill", Thesaurus.params().with("skill", String.valueOf(params.ability.skill)))).row();
        info.add(buyButton).padTop(3).padBottom(4).row();


        String reason = null;
        if (notEnoughCoins) {
            int requiredCoins = params.ability.cost - params.userData.getItemCount(Config.items.get("coin"));
            reason = Config.thesaurus.localize(
                "ui-store-window-coins-needed",
                Thesaurus.params()
                    .with("count", String.valueOf(requiredCoins))
                    .with("coin-form", "coins." + Thesaurus.Util.countForm(requiredCoins))
            );
        }
        if (maxCountReached) {
            reason = Config.thesaurus.localize("ui-store-window-max-count-reached", Thesaurus.params().with("name", params.die.nameLocKey()));
        }
        if (requirementsNotSatisfied) {
            reason = params.ability.requirement.describe(params.die);
        }
        if (inventoryIsFull) {
            reason = Config.thesaurus.localize("ui-inventory-is-full");
        }
        if (reason != null) {
            Label label = new Label(reason, Config.skin, "default", INACTIVE);
            label.setWrap(true);
            label.setAlignment(Align.center);
            info.add(label).width(120).padBottom(3).padTop(-4).row();
        }

        table.clearChildren();
        table.add(info);

    }

    public static boolean canBeBought(Ability ability, Die die, UserData userData) {
        if (!ability.requirement.isSatisfied(die))
            return false;
        if (userData.getItemCount(Config.items.get("coin")) < ability.cost)
            return false;
        if (MapHelper.countPositive(die.inventory) >= INVENTORY_SIZE)
            return false;
        int abilityCount = die.inventory.get(ability, 0);
        for (Ability a : die.abilities) {
            if (a == ability)
                abilityCount++;
        }
        return abilityCount < 6;
    }

    private void buy() {
        buy = true;
        hide();
    }

    @Override protected void onHide() {
        if (buy) {
            buy = false;
            params.callback.onBuy();
        }
        params = null;
    }

    public static interface Callback {
        public void onBuy();
    }

    public static final class Params {

        public Params(Ability ability, Die die, UserData userData, Callback callback) {
            this.ability = ability;
            this.die = die;
            this.userData = userData;
            this.callback = callback;
        }

        private final Ability ability;
        private final Die die;
        private final UserData userData;
        private final Callback callback;
    }
}
