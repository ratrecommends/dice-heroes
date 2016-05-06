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
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 07.12.13 by vlaaad
 */
public class PauseWindow extends GameWindow<PauseWindow.Params> {

    private Callback callback;
    private Slider volumeSlider;
    private Cell restartCell;
    private LocTextButton restart;

    private static enum Action {
        cancel, restart
    }

    private Action action;

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

        TextButton cancel = new LocTextButton("ui-lose-window-to-map");
        cancel.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                action = Action.cancel;
                hide();
            }
        });
        restart = new LocTextButton("ui-restart-game");
        restart.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                action = Action.restart;
                hide();
            }
        });

        Table table = new Table(Config.skin);
        table.setBackground("ui-inventory-ability-window-background");
        table.add(new LocLabel("ui-settings-sound-volume")).row();
        table.add(volumeSlider).width(110).padBottom(10).padLeft(5).padRight(5).padTop(5).row();
        table.add(cancel).width(80).padBottom(5).row();
        restartCell = table.add(restart).width(80).padBottom(5);
        table.row();

        this.table.add(table);
    }

    @Override protected void doShow(Params params) {
        this.callback = params.callback;
        if(params.canRestart){
            restartCell.setActor(restart);
            restartCell.padBottom(5);
        } else {
            restartCell.setActor(null);
            restartCell.padBottom(0);
        }
        volumeSlider.setValue(Config.preferences.getVolume());
    }

    public static class Params {
        private final Callback callback;
        private final boolean canRestart;

        public Params(Callback callback) {
            this(callback, true);
        }

        public Params(Callback callback, boolean canRestart) {
            this.callback = callback;
            this.canRestart = canRestart;
        }
    }

    public static interface Callback {
        public void onRestart();
        public void onCancel();
    }

    @Override protected void onHide() {
        if (action != null) {
            switch (action) {
                case cancel:
                    callback.onCancel();
                    break;
                case restart:
                    callback.onRestart();
                    break;
                default:
                    throw new IllegalStateException("unknown action: " + action);
            }
        }
        action = null;
    }
}
