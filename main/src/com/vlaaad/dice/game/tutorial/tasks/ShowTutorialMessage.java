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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.tutorial.TutorialTask;
import com.vlaaad.common.util.Function;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 13.11.13 by vlaaad
 */
public class ShowTutorialMessage extends TutorialTask {

    private final String locKey;
    private final boolean onTop;
    private final boolean addTapToContinueText;
    private Function<Tutorial.TutorialResources, ObjectMap<String, String>> paramsProvider;

    public ShowTutorialMessage(String locKey) {
        this(locKey, false);
    }

    public ShowTutorialMessage(String locKey, boolean onTop) {
        this(locKey, onTop, false);
    }


    public ShowTutorialMessage(String locKey, boolean onTop, boolean addTapToContinueText) {
        super();
        this.locKey = locKey;
        this.onTop = onTop;
        this.addTapToContinueText = addTapToContinueText;
    }

    public ShowTutorialMessage withParams(Function<Tutorial.TutorialResources, ObjectMap<String, String>> paramsProvider) {
        this.paramsProvider = paramsProvider;
        return this;
    }

    @Override public void start(Callback callback) {
        Message message = resources.getIfExists("tutorialMessage");
        if (message != null) {
            new Tutorial(resources, Tutorial.tasks().with(new HideTutorialMessage())).start();
        }
        Stage stage = resources.get("stage");
        message = new Message(addTapToContinueText);
        stage.addActor(message);
        resources.put("tutorialMessage", message);
        if (message.getStage() != stage) {
            stage.addActor(message);
        }
        message.setTop(onTop);
        message.label.setKey(locKey);
        if (paramsProvider != null) {
            message.label.setParams(paramsProvider.apply(resources));
        }
        if (onTop) {
            message.setY(message.child.getPrefHeight());
        } else {
            message.setY(-message.child.getPrefHeight());
        }
        message.addAction(Actions.moveTo(
            0, 0, 0.3f
        ));
        callback.taskEnded();
    }

    public static class Message extends Table {

        public static final Color TAP_TO_CONTINUE = new Color(1, 1, 1, 0.3f);
        public final LocLabel label;
        public final Table child;
        public boolean onTop;

        public Message(boolean addTapToContinueText) {
            super(Config.skin);
            setFillParent(true);
            align(Align.bottom);

            child = new Table();
            label = new LocLabel("");
            label.setWrap(true);
            label.setAlignment(Align.center);
            child.add(label).expandX().fillX().row();
            if (addTapToContinueText) {
//                child.add(new Image(Config.skin, "ui-creature-info-line")).width(Value.percentWidth(0.7f)).padTop(4).row();
                child.add(new LocLabel("tap-to-continue", TAP_TO_CONTINUE)).row();
            }

            add(child).expandX().fillX();
        }

        public void setTop(boolean onTop) {
            this.onTop = onTop;
            if (onTop) {
                align(Align.top);
                child.setBackground(Config.skin.getDrawable("ui-tutorial-message-background-top"));
            } else {
                align(Align.bottom);
                child.setBackground(Config.skin.getDrawable("ui-tutorial-message-background"));
            }
        }
    }
}
