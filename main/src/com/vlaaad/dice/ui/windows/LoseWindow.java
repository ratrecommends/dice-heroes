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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.components.Rain;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 04.11.13 by vlaaad
 */
public class LoseWindow extends GameWindow<LoseWindow.Params> {

    public static final Color DEFEAT_TEXT_COLOR = new Color(114 / 255f, 150 / 255f, 146 / 255f, 1f);
    private Callback callback;
    private boolean doRestart;
    private TextButton restart;

    @Override protected float backgroundMaxAlpha() {
        return 0.75f;
    }

    @Override protected void initialize() {
        table.defaults().pad(5);
    }

    public void disableRestart() {
        restart.setDisabled(true);
        doRestart = false;
    }

    @Override protected void doShow(Params params) {
        this.callback = params.callback;
        table.clearChildren();
        Rain.RainStyle style = new Rain.RainStyle();
        float min = getStage().getHeight() * 0.13f;
        float max = getStage().getHeight() * 0.3f;
        style.minDropDistance = min;
        style.minDropHeight = min;
        style.maxDropDistance = max;
        style.maxDropHeight = max;
        Rain rain = new Rain(style);
        rain.setTouchable(Touchable.disabled);
        rain.setFillParent(true);
        table.addActor(rain);
        Label defeat = new LocLabel("ui-lose-window-defeat", DEFEAT_TEXT_COLOR);
        defeat.setFontScale(3f);
        TextButton ok = new LocTextButton("ui-lose-window-to-map");
        ok.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });
        restart = new LocTextButton("ui-replay-game");
        restart.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                doRestart = true;
                hide();
            }
        });
        table.add(defeat).padBottom(25).row();
        if (params.result.viewer.earnedItems.size > 0) {
            Table items = new Table();
            items.setTransform(true);
            ObjectIntMap<Item> earned = params.result.viewer.earnedItems;
            Array<Item> sorted = earned.keys().toArray();
            sorted.sort(Item.ORDER_COMPARATOR);
            for (Item item : sorted) {
                Table rewardView = new Table(Config.skin);
                Image image = new Image(Config.skin, "item/" + item.name);
                Label counter = new Label(String.valueOf(earned.get(item, 0)), Config.skin, "default", DEFEAT_TEXT_COLOR);
                counter.setFontScale(2f);
                rewardView.add(image).size(image.getPrefWidth() * 2, image.getPrefHeight() * 2);
                rewardView.add(counter).padTop(-5);
                items.add(rewardView);
            }
            table.add(items).padTop(-10).row();
        }
        float w = Math.max(90, restart.getPrefWidth() + 4);
        table.add(restart).width(w).row();
        table.add(ok).width(w).row();
        SoundManager.instance.playMusicAsSound("lose");
    }

    @Override protected boolean canBeClosed() {
        return false;
    }
    @Override public boolean handleBackPressed() {
        return true;
    }
    @Override protected void onHide() {
        if (doRestart) {
            callback.onRestart();
        } else {
            callback.onClose();
        }
        doRestart = false;
        callback = null;
    }

    public static class Params {
        private final LevelResult result;
        private final Callback callback;

        public Params(LevelResult result, Callback callback) {
            this.result = result;
            this.callback = callback;
        }

    }

    public static interface Callback {

        void onRestart();

        void onClose();
    }
}
