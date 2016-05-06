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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.MathHelper;
import com.vlaaad.common.util.signals.ISignalListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.purchases.PurchaseData;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.components.LanguageSelector;
import com.vlaaad.dice.ui.components.ScaleSelector;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;
import com.vlaaad.dice.util.ScreenHelper;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 11.10.13 by vlaaad
 */
public class SettingsWindow extends GameWindow<SettingsWindow.Params> implements ISignalListener<Boolean> {
    private Callback callback;
    private Slider volumeSlider;
    private final AboutWindow aboutWindow = new AboutWindow();
    private LocTextButton donateButton;
    private UserData userData;
    private Table donateContainer = new Table();
    private Cell donateCell;

    @Override protected void initialize() {
        volumeSlider = new Slider(0, 1, 0.125f, false, Config.skin);
        volumeSlider.setValue(Config.preferences.getVolume());
        volumeSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (Config.preferences.getVolume() == volumeSlider.getValue())
                    return;
                Config.preferences.setVolume(volumeSlider.getValue());
                SoundManager.instance.playSound("ui-button-down");
            }
        });

        final Button rate = new LocTextButton("rate-app");
        rate.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Config.mobileApi.rateApp();
                Config.preferences.setRated(true);
                rate.remove();
            }
        });
        Table table = new Table(Config.skin);
        table.setTouchable(Touchable.enabled);
        table.setBackground("ui-inventory-ability-window-background");

        Button about = new LocTextButton("ui-settings-window-about");
        about.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                aboutWindow.show(null);
            }
        });

        ImageButton share = new ImageButton(Config.skin, "share");
        share.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Config.mobileApi.share(Config.thesaurus.localize("app-share-text"));
            }
        });
        SoundHelper.initButton(share);

        TextureRegion maxRegion = Config.assetManager.get("world-map.atlas", TextureAtlas.class).findRegion("world-map");
        int minWScale = MathUtils.ceil(Gdx.graphics.getWidth() / (float) maxRegion.getRegionWidth());
        int minHScale = MathUtils.ceil(Gdx.graphics.getHeight() / (float) maxRegion.getRegionHeight());
        int maxWScale = MathUtils.floor(Gdx.graphics.getWidth() / 160f);
        int maxHScale = MathUtils.floor(Gdx.graphics.getHeight() / 213f);
        int maxScale = MathHelper.min(ScreenHelper.max.scale, maxWScale, maxHScale);
        int minScale = MathHelper.max(minWScale, minHScale, 1);

        Table buttons = new Table();
        buttons.add(about).width(40).padRight(5);
        buttons.add(share).size(19);

        final CheckBox checkBox = new CheckBox("", Config.skin);
        checkBox.setChecked(Config.preferences.isMusic());
        checkBox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Config.preferences.setMusic(checkBox.isChecked());
            }
        });

        Table music = new Table();
        music.add(new LocLabel("ui-settings-music"));
        music.add(checkBox).padTop(2);

        table.add(new LocLabel("ui-settings-sound-volume")).row();
        table.add(volumeSlider).width(88).pad(5).row();
        table.add(new LocLabel("ui-settings-select-language")).row();
        table.add(new LanguageSelector()).width(100).padBottom(5).row();
        if (minScale < maxScale) {
            table.add(new LocLabel("ui-settings-select-scale")).padTop(-4).row();
            table.add(new ScaleSelector(minScale, maxScale, Config.preferences.getScale())).width(100).padBottom(5).row();
        }

        table.add(music).padBottom(5).row();
        table.add(new Image(Config.skin, "ui-creature-info-line")).width(75).padBottom(5).row();

        table.add(buttons).padBottom(5).row();
        if (!Config.preferences.isRated())
            table.add(rate).width(75).padBottom(5).row();

        donateButton = new LocTextButton("ui-donate");
        donateButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (callback != null) {
                    callback.onBuy(PurchaseData.get("donation"));
                }
            }
        });
        donateContainer.add(donateButton).width(75);

        donateCell = table.add(donateContainer).padBottom(5);
        donateCell.row();

        this.table.add(table);
    }

    @Override protected void doShow(Params params) {
        this.callback = params.callback;
        this.userData = params.userData;
        volumeSlider.setValue(Config.preferences.getVolume());
        if (userData.isDonated()) {
            handle(true);
        }
        params.userData.onDonated.add(this);
    }

    @Override public void handle(Boolean donated) {
        if (!donated)
            return;
        donateContainer.clearChildren();
        donateCell.pad(10).padBottom(15);
        LocLabel label = new LocLabel("ui-thank-you");
        label.setWrap(true);
        label.setAlignment(Align.center);
        label.setSize(60, 18);
        donateContainer.add(label).size(60, 18);
        table.layout();
        ParticleActor actor = new ParticleActor(Config.particles.get("ui-thank-you").obtain());
        Image bg = new Image(Config.skin, "ui-thank-you-background");
        donateContainer.addActor(bg);
        donateContainer.addActor(actor);
        bg.toBack();
        bg.setPosition(label.getX(), label.getY());
        bg.setSize(60, 18);
        actor.toBack();
        actor.setPosition(label.getX() + 30, label.getY() + 9);

    }

    @Override protected void onHide() {
        userData.onDonated.remove(this);
    }

    public static final class Params {
        private final UserData userData;
        private final Callback callback;

        public Params(UserData userData, Callback callback) {
            this.userData = userData;
            this.callback = callback;
        }
    }

    public static interface Callback {

        public void onBuy(PurchaseInfo purchaseInfo);
    }

}
