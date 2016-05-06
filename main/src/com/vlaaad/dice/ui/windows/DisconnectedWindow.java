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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.Option;
import com.vlaaad.common.util.Tuple2;
import com.vlaaad.common.util.Tuple3;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 04.08.14 by vlaaad
 */
public class DisconnectedWindow extends GameWindow<Tuple3<Boolean, Option<String>, Option<Throwable>>> {

    private Throwable ex;

    @Override protected void doShow(final Tuple3<Boolean, Option<String>, Option<Throwable>> reason) {
        ex = null;
        String message = reason._2.isDefined() ? reason._2.get() : Config.thesaurus.localize("disconnect-unknown-reason");
        Table content = new Table(Config.skin);
        content.setTouchable(Touchable.enabled);
        content.defaults().pad(2);
        content.setBackground("ui-store-window-background");
        Label label = new Label(message, Config.skin);
        label.setWrap(true);
        label.setAlignment(Align.center);
        content.add(label).width(140).colspan(2).row();
        if (reason._3.isDefined()) {
            Label error = new LocLabel("ui-please-send-report", Color.GRAY);
            error.setWrap(true);
            error.setAlignment(Align.center);
            content.add(error).width(140).colspan(2).padTop(-2).row();
            TextButton crashAndSend = new LocTextButton("ui-crash-and-send");
            crashAndSend.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    ex = reason._3.get();
                    hide();
                }
            });
            TextButton cancel = new LocTextButton("ui-cancel-crash-report");
            cancel.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    ex = null;
                    hide();
                }
            });
            content.add(crashAndSend).width(60);
            content.add(cancel).width(60);
        } else {
            TextButton cancel = new LocTextButton("ui-okay");
            cancel.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    hide();
                }
            });
            content.add(cancel).colspan(2).width(60);
        }
        table.add(content);
    }

    @Override protected void onHide() {
        if (ex != null) throw new RuntimeException(ex);
    }
}
