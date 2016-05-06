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
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 13.02.14 by vlaaad
 */
public class CreatureQueueWindow extends GameWindow<Array<Creature>> {

    private final CreatureInfoWindow creatureInfoWindow = new CreatureInfoWindow();
    private Table creaturesList = new Table(Config.skin);
    private Array<Creature> array;

    @Override protected void initialize() {
        creaturesList.defaults().pad(2);
        creaturesList.padTop(12);

        Image left = new Image(Config.skin, "ui-creature-queue-gradient-left");
        left.setScaling(Scaling.stretchY);
        left.setAlign(Align.left);
        left.setTouchable(Touchable.disabled);

        Image right = new Image(Config.skin, "ui-creature-queue-gradient-right");
        right.setScaling(Scaling.stretchY);
        right.setAlign(Align.right);
        right.setTouchable(Touchable.disabled);

        Stack stack = new Stack();
        stack.add(new ScrollPane(creaturesList, new ScrollPane.ScrollPaneStyle()));
        stack.add(left);
        stack.add(right);

        Table content = new Table(Config.skin);
        content.setTouchable(Touchable.enabled);
        content.setBackground("ui-inventory-ability-window-background");
        content.defaults().pad(2);
        content.add(new LocLabel("ui-turns-order")).row();
        content.add(new Image(Config.skin, "ui-creature-info-line")).width(100).row();
        content.add(stack).maxWidth(table.getStage().getWidth() - 45).padRight(4).padLeft(4).row();

        table.add(content);
    }

    @Override protected void doShow(Array<Creature> array) {
        this.array = array;
        Creature curr = array.first();
        creaturesList.clearChildren();
        for (int i = 0, n = array.size; i < n; i++) {
            Creature creature = array.get(i);
            WorldObjectView view = ViewController.createView(curr.world.viewer, curr.world.playerColors, creature);
            view.addListener(createListener(creature));
            creaturesList.add(view);
            if (i != n - 1) {
                creaturesList.add(">>").padTop(-2);
            }
        }
    }

    private EventListener createListener(final Creature creature) {
        return new ActorGestureListener(20, 0.4f, 0.5f, 0.15f) {
            @Override public boolean longPress(Actor actor, float x, float y) {
                creatureInfoWindow.show(new CreatureInfoWindow.Params(creature, array.first().world));
                return true;
            }
        };
    }
}
