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

package com.vlaaad.dice.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 27.10.13 by vlaaad
 */
public class DieNet extends WidgetGroup {

    private static final Vector2[] iconPositions = {
        new Vector2(20, 41),
        new Vector2(2, 23),
        new Vector2(20, 23),
        new Vector2(38, 23),
        new Vector2(56, 23),
        new Vector2(38, 5)
    };
    private static final Vector2 tmp = new Vector2();
    private static final int size = 14;

    public final Die die;
    private final Drawable back;
    private DieInventory inventory;

    public DieNet(Die die) {
        this.die = die;
        back = Config.skin.getDrawable("ui-net-" + die.profession);
        refresh();
    }

    public void setInventory(DieInventory inventory) {
        if (die != inventory.die)
            throw new IllegalStateException("wrong owner!");
        this.inventory = inventory;
    }


    public void refresh() {
        clearChildren();
        for (int i = 0; i < die.abilities.size; i++) {
            Ability ability = die.abilities.get(i);
            if (ability != null) {
                addAbilityIcon(i, ability);
            }
        }
        fire(RefreshEvent.INSTANCE);
    }

    private void addAbilityIcon(final int index, final Ability ability) {
        final AbilityDarkIcon abilityIcon = new AbilityDarkIcon(ability);
        SoundHelper.init(abilityIcon);
        Vector2 iconPosition = iconPositions[index];
        abilityIcon.setPosition(iconPosition.x, iconPosition.y);
        addActor(abilityIcon);
        abilityIcon.addListener(new ActorGestureListener(1.5f, 0.4f, 1.1f, 0.15f) {
            public Vector2 tmp = new Vector2();
            public AbilityIcon draggedIcon;

            @Override public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                abilityIcon.setVisible(false);
                draggedIcon = new AbilityIcon(ability);
                getStage().addActor(draggedIcon);
                draggedIcon.setPosition(event.getStageX() - draggedIcon.getWidth() / 2, event.getStageY() - draggedIcon.getHeight() / 2);
                event.cancel();
                highlightMove(ability);
            }

            @Override public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                super.pan(event, x, y, deltaX, deltaY);
                draggedIcon.moveBy(deltaX, deltaY);
                event.cancel();
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                abilityIcon.setVisible(true);
                stopHighlighting();
                draggedIcon.remove();
                if (inventory != null) {
                    tmp.set(event.getStageX(), event.getStageY());
                    inventory.stageToLocalCoordinates(tmp);
                    if (tmp.x > 0 && tmp.x < inventory.getWidth() && tmp.y > 0 && tmp.y < inventory.getHeight() && inventory.getStage() != null) {
                        Ability v = die.abilities.get(index);
                        if (v == null)
                            return;
                        die.abilities.set(index, null);
                        die.inventory.getAndIncrement(v, 0, 1);
                        refresh();
                        inventory.refresh();
                        return;
                    }
                }
                tmp.set(event.getStageX(), event.getStageY());
                stageToLocalCoordinates(tmp);
                int newIndex = getAbilityIndex(tmp.x, tmp.y);
                while (die.abilities.size < 6)
                    die.abilities.add(null);
                if (newIndex != -1) {
                    Ability prev = die.abilities.get(newIndex);
                    die.abilities.set(newIndex, ability);
                    die.abilities.set(index, prev);
                    refresh();
                }
            }
        });
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        validate();
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        back.draw(
            batch,
            getX() + getWidth() / 2 - back.getMinWidth() / 2,
            getY() + getHeight() / 2 - back.getMinHeight() / 2,
            back.getMinWidth(),
            back.getMinHeight()
        );
        super.draw(batch, parentAlpha);
    }

    @Override public float getPrefWidth() {
        return back.getMinWidth();
    }

    @Override public float getPrefHeight() {
        return back.getMinHeight();
    }

    public int getAbilityIndex(float x, float y) {
        for (int i = 0; i < iconPositions.length; i++) {
            Vector2 pos = iconPositions[i];
            if (x == MathUtils.clamp(x, pos.x, pos.x + size) && y == MathUtils.clamp(y, pos.y, pos.y + size))
                return i;
        }
        return -1;
    }

    private Array<Image> highlights = new Array<Image>(6);

    public void startHighlighting(Ability ability) {
        highlight(die.getAvailableIndices(ability));
    }

    private void highlight(IntArray indices) {
        for (int i = 0; i < indices.size; i++) {
            Vector2 pos = iconPositions[indices.get(i)];
            Image image = new Image(Config.skin, "ui/dice-window/net-selection-selected");
            image.setPosition(pos.x - 2, pos.y - 2);
            addActor(image);
            image.toBack();
            image.getColor().a = 0f;
            blink(image);
            highlights.add(image);
        }
    }

    private void highlightMove(Ability ability) {
        IntArray available = new IntArray();
        for (int i = 0; i < die.abilities.size; i++) {
            if (die.abilities.get(i) != ability) available.add(i);
        }
        highlight(available);
    }

    private void blink(final Image image) {
        image.addAction(Actions.sequence(
            Actions.alpha(0.4f, 0.5f),
            Actions.alpha(0f, 0.5f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    blink(image);
                }
            })
        ));
    }

    public void stopHighlighting() {
        for (Image image : highlights) {
            image.remove();
            image.clearActions();
        }
        highlights.clear();
    }

    public Rectangle[] getEmptyPlaces() {
        Vector2 offset = localToStageCoordinates(tmp.set(0, 0));
        int count = 0;
        for (int i = 0; i < die.abilities.size; i++) {
            Ability ability = die.abilities.get(i);
            if (ability == null) {
                count++;
            }
        }
        Rectangle[] result = new Rectangle[count];
        int idx = 0;
        for (int i = 0; i < die.abilities.size; i++) {
            Ability ability = die.abilities.get(i);
            if (ability == null) {
                Vector2 position = iconPositions[i];
                result[idx] = new Rectangle(position.x + offset.x, position.y + offset.y, size, size);
                idx++;
            }
        }
        return result;
    }
}
