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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 05.07.14 by vlaaad
 */
public class ExitWindow extends GameWindow<Void> {
    private boolean quit = false;
    @Override protected void initialize() {
        Button yes = new LocTextButton("ui-yes");
        yes.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                quit = true;
                hide();
            }
        });
        Button no = new LocTextButton("ui-no");
        no.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                quit = false;
                hide();
            }
        });

        Table content = new Table(Config.skin);
        content.setTouchable(Touchable.enabled);
        content.setBackground("ui-store-window-background");
        content.defaults().pad(2);

        final LocLabel label = new LocLabel("ui-exit-confirmation");
        label.setAlignment(Align.center);
        content.add(label).colspan(2).row();

        content.add(new Tile("exit-pic")).colspan(2).pad(6).row();

        content.add(yes).width(40);
        content.add(no).width(40);

        table.add(content);
    }

    @Override protected void doShow(Void aVoid) {

    }

    @Override protected void onHide() {
        if (quit) {
            Gdx.app.exit();
            quit = false;
        }
    }
}
