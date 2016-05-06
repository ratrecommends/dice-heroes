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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.events.EventType;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 15.12.13 by vlaaad
 */
public class AboutWindow extends GameWindow<Void> {
    public static final Color INACTIVE = new Color(0.647f, 0.647f, 0.67f, 1f);
    public static final Color LINK = new Color(1f, 0.894f, 0.16f, 1f);

    private Table content;

    @Override protected void initialize() {


        Label codeLabel = new LocLabel("author-vlaaad", LINK);
        codeLabel.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(Config.thesaurus.localize("author-vlaaad-link"));
            }
        });

        Label graphicsLabel = new LocLabel("author-shtukaturka", LINK);
        graphicsLabel.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(Config.thesaurus.localize("author-shtukaturka-link"));
            }
        });

        Label musicLabel = new LocLabel("author-sagamor", LINK);
        musicLabel.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(Config.thesaurus.localize("author-sagamor-link"));
            }
        });

        Label communityLabel = new LocLabel("community-short-link", LINK);
        communityLabel.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(Config.thesaurus.localize("community-link"));
            }
        });
        Label repositoryLabel = new LocLabel("repository-short-link", LINK);
        repositoryLabel.setAlignment(Align.center);
        repositoryLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(Config.thesaurus.localize("repository-link"));
            }
        });

        Table code = new Table();
        code.add(new LocLabel("ui-about-code", INACTIVE)).align(Align.right);
        code.add(codeLabel).align(Align.left);

        Table graphics = new Table();
        graphics.add(new LocLabel("ui-about-graphics", INACTIVE)).align(Align.right);
        graphics.add(graphicsLabel).align(Align.left);

        Table music = new Table();
        music.add(new LocLabel("ui-about-music", INACTIVE)).align(Align.right);
        music.add(musicLabel).align(Align.left);


        content = new Table(Config.skin);
        content.setBackground("ui-store-window-background");

        content.add(new Image(Config.skin, "ui-about-window-pic")).padTop(10).row();
        content.add(new Image(Config.skin, "ui-creature-info-line")).width(50).row();

        content.add(new LocLabel("app-name")).padTop(10).row();

        content.add(new LocLabel("ui-about-author", INACTIVE)).row();

        content.add(code).padTop(10).row();
        content.add(graphics).row();
        Image line = new Image(Config.skin, "ui-creature-info-line");
        line.getColor().a = 0.5f;
        content.add(line).width(50).padTop(4).row();
        content.add(music).row();

        content.add(new Image(Config.skin, "ui-creature-info-line")).width(80).padTop(10).row();

        content.add(new LocLabel("ui-about-community", INACTIVE)).padTop(10).row();
        content.add(communityLabel).row();
        content.add(new LocLabel("ui-about-repository", INACTIVE)).row();
        content.add(repositoryLabel).padBottom(10).row();

        table.add(content).width(150);
    }

    @Override protected void doShow(Void aVoid) {
        Config.achievements.fire(EventType.aboutWindow);
    }
}
