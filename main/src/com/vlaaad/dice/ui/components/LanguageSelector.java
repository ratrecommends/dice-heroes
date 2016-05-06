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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;

/**
 * Created 15.12.13 by vlaaad
 */
public class LanguageSelector extends Table {
    private static final String[] languages = {"en", "ru"};

    private ObjectMap<String, Label> languageToLabel = new ObjectMap<String, Label>();
    private final Button left = new Button(Config.skin, "arrow-left");
    private final ScrollPane languagePane = new ScrollPane(createPaneContent(languages));
    private final Button right = new Button(Config.skin, "arrow-right");
    private int idx;

    public LanguageSelector() {
        super();
        add(left);
        add(languagePane).width(30);
        add(right);
        languagePane.setTouchable(Touchable.disabled);
        invalidate();
        validate();
        show(Config.preferences.getLanguage());
        left.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                scroll(-1);
            }
        });
        right.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                scroll(1);
            }
        });
    }

    private void scroll(int delta) {
        idx += delta;
        idx = MathUtils.clamp(idx, 0, languages.length - 1);
        String lang = languages[idx];
        Config.preferences.setLanguage(lang);
        left.setDisabled(idx == 0);
        right.setDisabled(idx == languages.length - 1);
        Label target = languageToLabel.get(lang);
        languagePane.scrollTo(target.getX(), target.getY(), target.getWidth(), target.getHeight(), true, true);
    }

    private void show(String language) {
        idx = -1;
        int i = 0;
        for (String lang : languages) {
            if (lang.equals(language)) {
                idx = i;
                break;
            }
            i++;
        }
        if (idx == -1) {
            language = "en";
            idx = 0;
        }
        left.setDisabled(idx == 0);
        right.setDisabled(idx == languages.length - 1);
        Label target = languageToLabel.get(language);
        languagePane.scrollTo(target.getX(), target.getY(), target.getWidth(), target.getHeight());
        languagePane.updateVisualScroll();
    }

    private Actor createPaneContent(String[] languages) {
        Table table = new Table(Config.skin);
        for (String lang : languages) {
            Label label = new Label(lang.toUpperCase(), Config.skin);
            label.setAlignment(Align.center);
            languageToLabel.put(lang, label);
            table.add(label).width(30);
        }
        return table;
    }
}
