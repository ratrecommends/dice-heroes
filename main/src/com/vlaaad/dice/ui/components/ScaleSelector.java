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
import com.badlogic.gdx.utils.IntMap;
import com.vlaaad.dice.Config;

/**
 * Created 07.04.14 by vlaaad
 */
public class ScaleSelector extends Table {
    private final int min;
    private IntMap<Label> scaleToLabel = new IntMap<Label>();
    private final Button left = new Button(Config.skin, "arrow-left");
    private final ScrollPane scalePane;
    private final Button right = new Button(Config.skin, "arrow-right");
    private int idx;
    private final int[] scales;

    public ScaleSelector(int min, int max, int current) {
        this.min = min;
        scales = new int[max - min + 1];
        for (int i = min; i <= max; i++) {
            scales[i - min] = i;
        }
        scalePane = new ScrollPane(createPaneContent(min, max));
        add(left);
        add(scalePane).width(30);
        add(right);
        scalePane.setTouchable(Touchable.disabled);
        invalidate();
        validate();
        show(current);
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

    private void show(int scale) {
        idx = -1;
        int i = 0;
        for (int s : scales) {
            if (s == scale) {
                idx = i;
                break;
            }
            i++;
        }
        if (idx == -1) {
            scale = min;
            idx = 0;
        }
        left.setDisabled(idx == 0);
        right.setDisabled(idx == scales.length - 1);
        Label target = scaleToLabel.get(scale);
        scalePane.scrollTo(target.getX(), target.getY(), target.getWidth(), target.getHeight());
        scalePane.updateVisualScroll();
    }

    private void scroll(int delta) {
        idx += delta;
        idx = MathUtils.clamp(idx, 0, scales.length - 1);
        int s = scales[idx];

        Config.preferences.setScale(s);

        left.setDisabled(idx == 0);
        right.setDisabled(idx == scales.length - 1);
        Label target = scaleToLabel.get(s);
        scalePane.scrollTo(target.getX(), target.getY(), target.getWidth(), target.getHeight(), true, true);
    }

    private Actor createPaneContent(int min, int max) {
        Table table = new Table(Config.skin);
        for (int lang : scales) {
            Label label = new Label("x" + lang, Config.skin);
            label.setAlignment(Align.center);
            scaleToLabel.put(lang, label);
            table.add(label).width(30);
        }
        return table;
    }
}
