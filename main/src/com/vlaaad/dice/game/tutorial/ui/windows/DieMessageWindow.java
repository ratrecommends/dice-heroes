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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 11.11.13 by vlaaad
 */
public class DieMessageWindow extends GameWindow<DieMessageWindow.Params> {
    public static final Color ACTIVE = new Color(0.17647f, 0.23529f, 0.23922f, 1f);
    public static final Color INACTIVE = new Color(0.17647f, 0.23529f, 0.23922f, 0.7f);

    private LocLabel label;
    private Table imageTable = new Table(Config.skin);

    @Override protected void initialize() {
        Table table = new Table(Config.skin);
        table.setBackground(Config.skin.getDrawable("ui-tutorial-window-background"));
        label = new LocLabel("", ACTIVE);
        label.setWrap(true);
        label.setAlignment(Align.center);

        table.setTouchable(Touchable.disabled);

        Label tapToContinue = new LocLabel("tap-to-continue", INACTIVE);
        tapToContinue.setWrap(true);
        tapToContinue.setAlignment(Align.center);

        table.add(imageTable).padTop(6).row();
        table.add(label).width(100).row();
        table.add(new Image(Config.skin, "ui-tutorial-window-line")).padTop(4).row();
        table.add(tapToContinue).width(80).row();

        this.table.add(table);
    }

    @Override protected void doShow(Params params) {
        label.setKey(params.locKey);
        imageTable.clearChildren();
        Image left = new Image(Config.skin, "ui-tutorial-window-halfline");
        left.setOrigin(left.getWidth() / 2f, left.getHeight() / 2f);
        left.rotateBy(180);
        imageTable.add(left);
        imageTable.add(ViewController.createView(new Creature(params.die, PlayerHelper.defaultProtagonist))).padLeft(2).padRight(2);
        imageTable.add(new Image(Config.skin, "ui-tutorial-window-halfline"));
    }

    public static class Params {
        private final Die die;
        private final String locKey;

        public Params(Die die, String locKey) {
            this.die = die;
            this.locKey = locKey;
        }
    }
}
