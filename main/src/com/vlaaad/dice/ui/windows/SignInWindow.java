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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 10.08.14 by vlaaad
 */
public class SignInWindow extends GameWindow<String> {

    private boolean signIn;

    @Override protected void doShow(String signInReasonKey) {
        Table content = new Table(Config.skin);
        content.setBackground("ui-store-window-background");
        content.defaults().pad(4);

        LocLabel label = new LocLabel(signInReasonKey);
        label.setWrap(true);
        label.setAlignment(Align.center);

        Button button = new Button(Config.skin);
        button.defaults().pad(2);
        button.add(new LocLabel("ui-sign-in")).padTop(1).padLeft(4).expand().right();
        button.add(new Tile("ui/button/services-icon")).padTop(4).padBottom(2).padRight(4).expand().left();
        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                signIn = true;
                hide();
            }
        });

        content.add(label).width(130).row();
        content.add(button).width(70).padBottom(8);

        table.add(content);
    }

    @Override protected void onHide() {
        if (signIn) {
            signIn = false;
            if (!Config.mobileApi.services().isSignedIn()) {
                Config.mobileApi.services().signIn();
            }
        }
    }
}
