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

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.ui.components.DiePane;
import com.vlaaad.dice.ui.components.RefreshListener;

/**
 * Created 16.10.13 by vlaaad
 */
public class DiceWindow extends GameWindow<UserData> {
    private final ObjectMap<Die, DiePane> map = new ObjectMap<Die, DiePane>();
    private final Group diceWindowGroup;

    public DiceWindow(Group diceWindowGroup) {
        this.diceWindowGroup = diceWindowGroup;
    }

    @Override protected void initialize() {
        addListener(new RefreshListener() {
            @Override protected void refreshed() {
                for (DiePane pane : map.values()) {
                    pane.store.refresh();
                }
            }
        });
    }

    @Override protected void doShow(final UserData userData) {
        final Table items = new Table();
        final ScrollPane pane = new ScrollPane(items, new ScrollPane.ScrollPaneStyle()) {

            @Override public void layout() {
                float w = items.getPrefWidth();
                float h = Math.min(getParent().getHeight(), items.getPrefHeight());
                if (w != getWidth() || h != getHeight()) {
                    setSize(w, h);
                    invalidateHierarchy();
                }
                super.layout();
            }
        };
        pane.setTouchable(Touchable.childrenOnly);
        pane.setOverscroll(false, false);
        pane.setCancelTouchFocus(false);
        pane.addListener(new ChangeListener() {

            @Override public void changed(ChangeEvent event, Actor actor) {
                pane.layout();
                pane.layout();
                items.layout();
                if (actor instanceof Layout)
                    ((Layout) actor).layout();
                pane.layout();
                pane.layout();
                pane.scrollTo(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight() + 100);
            }
        });

        Iterable<Die> dice = userData.dice();
        int i = 1;
        int count = userData.diceCount();
        for (Die die : dice) {
            DiePane diePane = new DiePane(die, userData, diceWindowGroup);
            table.add(diePane);
            diePane.setWidth(0);
            diePane.pack();
            diePane.setWidth(ViewController.CELL_SIZE * 6.6f);
            Cell cell = items.add(diePane).fillX().maxWidth(ViewController.CELL_SIZE * 6.6f);
            if (i != count) {
                cell.padBottom(-1);
            }
            cell.row();
            map.put(die, diePane);
            diePane.info.addListener(createMinimizeListener(die, dice));
            i++;
        }
        items.pack();
        table.add(pane).width(items.getPrefWidth());//.size(items.getPrefWidth(), 200);
    }

    @Override protected void onHide() {
        table.clearChildren();
        map.clear();
    }

    private EventListener createMinimizeListener(final Die die, final Iterable<Die> dice) {
        return new ActorGestureListener(){
            @Override public void tap(InputEvent event, float x, float y, int count, int button) {
                for (Die check : dice) {
                    DiePane pane = map.get(check);
                    if (pane == null)
                        continue;
                    if (check == die) {
                        pane.setMinimized(!pane.isMinimized());
                    } else {
                        pane.setMinimized(true);
                    }
                }
            }
        };
    }

    @Override public boolean handleBackPressed() {
        for (DiePane diePane : map.values()) {
            if (diePane.handleBackPressed())
                return true;
        }
        return super.handleBackPressed();
    }

    @Override public Group getTargetParent() {
        return diceWindowGroup;
    }

    @Override public boolean useParentSize() {
        return true;
    }

    public DiePane getPane(Die die) {
        return map.get(die);
    }
}
