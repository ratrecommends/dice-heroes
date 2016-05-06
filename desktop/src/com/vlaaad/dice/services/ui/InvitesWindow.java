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

package com.vlaaad.dice.services.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;

/**
 * Created 27.07.14 by vlaaad
 */
public class InvitesWindow extends GameWindow<InvitesWindow.Callback> {

    private Callback callback;

    public static interface Callback {
        void onSelected(String player);
        void onCancelled();
    }

    private Table list = new Table();
    {
        list.defaults().expandX().fillX().pad(4);
    }
    private String selected;

    public void setPlayers(Array<String> players) {
        list.clear();
        for (String player : players) {
            TextButton button = new TextButton(player, Config.skin);
            button.addListener(createListener(player));
            list.add(button).row();
        }
    }
    private EventListener createListener(final String player) {
        return new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                selected = player;
                hide();
            }
        };
    }

    @Override protected void initialize() {
        Table content = new Table(Config.skin);
        content.setTouchable(Touchable.enabled);
        content.setBackground("ui-store-window-background");
        content.add(new ScrollPane(list)).size(100, 100);

        table.add(content);
    }

    @Override protected void doShow(Callback callback) {
        this.callback = callback;
    }

    @Override protected void onHide() {
        if (selected != null) {
            callback.onSelected(selected);
            selected = null;
        } else {
            callback.onCancelled();
        }
    }
}
