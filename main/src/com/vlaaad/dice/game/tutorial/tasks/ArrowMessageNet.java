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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.vlaaad.common.tutorial.tasks.ArrowMessage;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.tutorial.ui.windows.TutorialMessageWindow;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.states.GameMapState;
import com.vlaaad.dice.ui.components.DiePane;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 12.11.13 by vlaaad
 */
public class ArrowMessageNet extends ArrowMessage {

    private final String dieName;
    private final String locKey;
    private Stage stage;
    private DiePane pane;

    public ArrowMessageNet(String dieName, String locKey) {
        super();
        this.dieName = dieName;
        this.locKey = locKey;
    }

    @Override protected void init() {
        GameMapState mapState = resources.get("map");
        UserData userData = resources.get("userData");
        Die die = userData.findDieByName(dieName);
        stage = mapState.stage;
        pane = mapState.diceWindow.getPane(die);
    }

    @Override protected Actor getTargetActor() {
        return pane.net;
    }

    @Override protected Drawable getArrowDrawable() {
        return Config.skin.getDrawable("tutorial-arrow-up");
    }

    @Override protected ArrowDirection getDirection() {
        return ArrowDirection.bottom;
    }

    @Override protected Table getMessageTable() {

        LocLabel label = new LocLabel(locKey);
        label.setWrap(true);
        label.setAlignment(Align.center);

        Label tapToContinue = new LocLabel("tap-to-continue", ShowTutorialMessage.Message.TAP_TO_CONTINUE);
        tapToContinue.setWrap(true);
        tapToContinue.setAlignment(Align.center);

        Table result = new Table();
        result.align(Align.top);
        result.add(label).width(stage.getWidth() / 1.5f).row();
        result.add(new Image(Config.skin, "ui-creature-info-line")).width(120).padTop(4).row();
        result.add(tapToContinue);
        return result;
    }
}
