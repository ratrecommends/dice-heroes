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

package com.vlaaad.dice.game.tutorial.tasks;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.common.tutorial.tasks.ArrowForceClick;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.states.GameMapState;
import com.vlaaad.dice.ui.components.DiePane;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 12.11.13 by vlaaad
 */
public class ArrowForceClickStoreTab extends ArrowForceClick {

    private final String dieName;
    private final String locKey;
    private Stage stage;
    private DiePane pane;

    public ArrowForceClickStoreTab(String dieName, String locKey) {
        super();
        this.dieName = dieName;
        this.locKey = locKey;
    }

//    @Override protected void addListener(final Stage stage, final Actor target, final Image arrow, final Table message, final Callback callback) {
//        stage.addCaptureListener(new ClickListener() {
//            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//                if (!event.getTarget().isDescendantOf(target)) {
//                    event.cancel();
//                    return false;
//                }
//                return super.touchDown(event, x, y, pointer, button);
//            }
//
//            @Override public void clicked(InputEvent event, float x, float y) {
//                stage.removeCaptureListener(this);
//                arrow.remove();
//                message.remove();
//                callback.taskEnded();
//            }
//        });
//    }

    @Override protected void init() {
        GameMapState mapState = resources.get("map");
        UserData userData = resources.get("userData");
        Die die = userData.findDieByName(dieName);
        stage = mapState.stage;
        pane = mapState.diceWindow.getPane(die);
    }

    @Override protected Actor getTargetActor() {
        return pane.storeTabHeader;
    }

    @Override protected Drawable getArrowDrawable() {
        return Config.skin.getDrawable("tutorial-arrow-down");
    }

    @Override protected ArrowDirection getDirection() {
        return ArrowDirection.top;
    }

    @Override protected Table getMessageTable() {
        Table result = new Table();
        result.align(Align.bottom);
        LocLabel label = new LocLabel(locKey);
        label.setWrap(true);
        label.setAlignment(Align.center);
        result.add(label).width(stage.getWidth() / 1.5f);
        return result;
    }
}
