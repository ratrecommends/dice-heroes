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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.ui.components.AbilityIcon;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 03.11.13 by vlaaad
 */
public class InventoryAbilityWindow extends GameWindow<InventoryAbilityWindow.Params> {

    private Params params;
    private Button sellButton;
    private Action action;

    @Override protected void doShow(Params params) {
        this.params = params;
        Table info = new Table();
        info.setTransform(true);
        info.setBackground(Config.skin.getDrawable("ui-store-window-background"));

        Image line = new Image(Config.skin, "ui-creature-info-line");


        Label desc = new LocLabel(params.ability.locDescKey(), params.ability.fillDescriptionParams(params.die));
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        sellButton = new Button(Config.skin);
        SoundHelper.initButton(sellButton);
        sellButton.add(new LocLabel("ui-inventory-window-sell-for")).padLeft(4);
        sellButton.add(new Image(Config.skin, "item/coin")).padTop(-3).padBottom(-3);
        sellButton.add(String.valueOf(params.ability.sellCost)).padRight(4);
        sellButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                action = Action.sell;
                hide();
            }
        });

        Button equipButton = new Button(Config.skin);
        SoundHelper.initButton(equipButton);
        equipButton.add(new LocLabel("ui-inventory-equip")).padLeft(2).padRight(2);
        equipButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                action = Action.equip;
                hide();
            }
        });

        Image image = new Image(Config.skin, "ability/" + params.ability.name + "-icon");
        if (params.ability.cost < 0) {
            image.setColor(AbilityIcon.unique);
        }
        info.add(image).size(image.getWidth() * 2, image.getHeight() * 2).padBottom(-12).padTop(-6).row();
        info.add(new LocLabel(params.ability.locNameKey())).padBottom(3).row();
        info.add(line).width(50).row();
        info.add(desc).width(120).row();
        info.add(new LocLabel("ui-needed-skill", Thesaurus.params().with("skill", String.valueOf(params.ability.skill)))).padBottom(3).row();
        if (params.ability.cost >= 0)
            info.add(sellButton).padBottom(3).row();
        info.add(equipButton).size(sellButton.getPrefWidth(), sellButton.getPrefHeight()).padBottom(3).row();

        boolean requirementNotSatisfied = !params.ability.requirement.isSatisfied(params.die);
        boolean notEnoughSkill = params.die.getAvailableIndices(params.ability).size == 0;
        if (requirementNotSatisfied || notEnoughSkill) {
            equipButton.setDisabled(true);
            Label label;
            if (requirementNotSatisfied) {
                label = new LocLabel(params.ability.requirement.describe(params.die), StoreWindow.INACTIVE);
            } else {
                label = new LocLabel("not-enough-skill", Thesaurus.params(), StoreWindow.INACTIVE);
            }
            label.setWrap(true);
            label.setAlignment(Align.center);
            info.add(label).padLeft(4).padRight(4).padBottom(3).width(120).row();
        }

        table.clearChildren();
        table.add(info).width(150);
    }

    @Override protected void onHide() {
        if (action != null) {
            if (action == Action.equip) {
                params.callback.equip();
            } else if (action == Action.sell) {
                params.callback.sell();
            }
        }
        action = null;
        params = null;
    }

    public static final class Params {
        private final Ability ability;
        private final Callback callback;
        private final Die die;

        public Params(Ability ability, Callback callback, Die die) {
            this.ability = ability;
            this.callback = callback;
            this.die = die;
        }
    }

    public static interface Callback {
        void equip();

        void sell();
    }

    private static enum Action {
        equip, sell
    }
}
