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

package com.vlaaad.dice.game.tutorial.ui.windows;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 07.11.13 by vlaaad
 */
public class TutorialMessageWindow extends GameWindow<String> {

    private int dy;
    private int labelHeight;
    private boolean forceLabelHeight;
    private Image image;

    private LocLabel label;

    public TutorialMessageWindow() {
    }

    public TutorialMessageWindow(String imageName) {
        this(imageName, 0);
    }

    public TutorialMessageWindow(String imageName, int dy) {
        this.dy = dy;
        image = new Image(Config.skin, imageName);
    }

    public TutorialMessageWindow(String imageName, int dy, int labelHeight) {
        this.dy = dy;
        this.labelHeight = labelHeight;
        forceLabelHeight = true;
        image = new Image(Config.skin, imageName);
    }

    @Override protected void initialize() {
        Table table = new Table(Config.skin);
        table.setBackground(Config.skin.getDrawable("ui-tutorial-window-background"));

        label = new LocLabel("", DieMessageWindow.ACTIVE);
        label.setWrap(true);
        label.setAlignment(Align.center);

        table.setTouchable(Touchable.disabled);

        Label tapToContinue = new LocLabel("tap-to-continue", DieMessageWindow.INACTIVE);
        tapToContinue.setWrap(true);
        tapToContinue.setAlignment(Align.center);

        if (image != null) {
            image.setTouchable(Touchable.disabled);
            table.add(image).padTop(-15 - dy).row();
        }
        final Cell<LocLabel> cell = table.add(label).width(100);
        if (forceLabelHeight) cell.height(labelHeight);
        cell.row();
        table.add(new Image(Config.skin, "ui-tutorial-window-line")).padTop(4).row();
        table.add(tapToContinue).width(80).row();

        this.table.add(table);
    }

    @Override protected void doShow(String key) {
        label.setKey(key);
    }
}
