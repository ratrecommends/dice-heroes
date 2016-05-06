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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.windows.InventoryAbilityWindow;
import com.vlaaad.dice.util.SoundHelper;

import java.util.Comparator;

/**
 * Created 27.10.13 by vlaaad
 */
public class DieInventory extends WidgetGroup {

    private final Drawable background = Config.skin.getDrawable("ui/dice-window/inventory-background");

    private final Table table = new Table(Config.skin);
    private final Group selectionGroup = new Group();
    public final Die die;
    private final UserData userData;
    private final Comparator<Ability> shopComparator;
    private DieNet net;
    private Runnable cancelEquip;
    private ObjectMap<Ability, AbilityIconCounter> icons = new ObjectMap<Ability, AbilityIconCounter>();

    public DieInventory(Die die, UserData userData) {
        this.die = die;
        this.userData = userData;
        this.shopComparator = Ability.shopComparator(die);
        addActor(selectionGroup);
        addActor(table);
        table.setTouchable(Touchable.enabled);
        table.align(Align.top | Align.left);
        table.moveBy(1, -2);
        refresh();
    }

    public void setNet(DieNet net) {
        if (die != net.die)
            throw new IllegalStateException("not owner!");
        this.net = net;
    }

    void refresh() {
        selectionGroup.clearChildren();
        table.clearChildren();
        icons.clear();
        int i = 0;
        Array<Ability> abilities = new Array<Ability>();
        for (Ability ability : die.inventory.keys()) {
            if (die.inventory.get(ability, 0) != 0) {
                abilities.add(ability);
            }
        }
        abilities.sort(shopComparator);

        for (Ability ability : abilities) {
            int value = die.inventory.get(ability, 0);
            if (value != 0) {
                addCounter(ability, value);
                i++;
                if (i % 5 == 0) table.row();
            }
        }
        while (i % 5 != 0) {
            table.add().size(23);
            i++;
        }
    }

    private void addCounter(final Ability ability, int value) {
        final AbilityIconCounter counter = new AbilityIconCounter(ability, value);
        SoundHelper.init(counter);
        table.add(counter).size(23);
        icons.put(ability, counter);
        if (!ability.requirement.isSatisfied(die)) {
            counter.getColor().a = 0.5f;
        }
        counter.addListener(new ActorGestureListener(1.5f, 0.4f, 1.1f, 0.15f) {
            public Vector2 tmp = new Vector2();
            public AbilityIcon icon = new AbilityIcon(ability);
            public Image selection = new Image(Config.skin, "ui/dice-window/inventory-selection-selected");

            @Override public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!ability.requirement.isSatisfied(die))
                    return;
                event.stop();
                selectionGroup.addActor(selection);
                Vector2 pos = selectionGroup.stageToLocalCoordinates(counter.localToStageCoordinates(tmp.set(0, 0)));
                selection.setPosition(pos.x, pos.y + 1);
            }

            @Override public void tap(InputEvent event, float x, float y, int count, int button) {
                new InventoryAbilityWindow().show(new InventoryAbilityWindow.Params(ability, new InventoryAbilityWindow.Callback() {
                    @Override public void equip() {
                        if (!ability.requirement.isSatisfied(die))
                            return;
                        if (net == null)
                            return;
                        selectionGroup.addActor(selection);
                        net.startHighlighting(ability);

                        final ClickListener listener = new ClickListener() {
                            @Override public boolean handle(Event e) {
                                if (!(e instanceof InputEvent)) return false;
                                InputEvent event = (InputEvent) e;
                                switch (event.getType()) {
                                    case keyDown:
                                    case keyUp:
                                    case keyTyped:
                                        return super.handle(e);
                                }
                                e.stop();
                                return super.handle(e);
                            }

                            @Override public void clicked(InputEvent event, float x, float y) {
                                cancelEquip.run();
                                checkAddToNet(ability, event);
                            }
                        };

                        cancelEquip = new Runnable() {
                            @Override public void run() {

                                cancelEquip = null;
                                net.stopHighlighting();
                                selection.remove();
                                getStage().removeCaptureListener(listener);
                            }
                        };

                        getStage().addCaptureListener(listener);
                    }

                    @Override public void sell() {
                        int count = die.inventory.get(ability, 0);
                        if (count <= 0)
                            return;
                        die.inventory.getAndIncrement(ability, 0, -1);
                        Item coin = Config.items.get("coin");
                        int coinCount = userData.getItemCount(coin);
                        userData.setItemCount(coin, coinCount + ability.sellCost);
                        SoundManager.instance.playSound("coins");
                        refresh();
                        fire(RefreshEvent.INSTANCE);
                    }
                }, die));
            }

            @Override public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                if (!ability.requirement.isSatisfied(die))
                    return;
                if (icon.getStage() == null) {
                    net.startHighlighting(ability);
                    getStage().addActor(icon);
                    icon.setPosition(event.getStageX() - icon.getWidth() / 2, event.getStageY() - icon.getHeight() / 2);
                } else {
                    icon.moveBy(deltaX, deltaY);
                }
                event.stop();
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (!ability.requirement.isSatisfied(die))
                    return;
                icon.remove();
                selection.remove();
                net.stopHighlighting();
                if (!event.isCancelled())
                    checkAddToNet(ability, event);

            }

            private void checkAddToNet(Ability ability, InputEvent event) {
                if (net != null) {
                    tmp.set(event.getStageX(), event.getStageY());
                    net.stageToLocalCoordinates(tmp);
                    int abilityIndex = net.getAbilityIndex(tmp.x, tmp.y);
                    if (abilityIndex != -1 && die.getAvailableIndices(ability).contains(abilityIndex)) {
                        Ability prev = null;
                        if (die.abilities.size > abilityIndex)
                            prev = die.abilities.get(abilityIndex);
                        if (prev != null) {
                            die.inventory.getAndIncrement(prev, 0, 1);
                        }
                        while (die.abilities.size <= abilityIndex)
                            die.abilities.add(null);
                        die.abilities.set(abilityIndex, ability);
                        die.inventory.getAndIncrement(ability, 0, -1);
                        if (die.inventory.get(ability, 0) == 0) {
                            die.inventory.remove(ability, 0);
                        }
                        net.refresh();
                        refresh();
                    }
                }
            }
        });
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        background.draw(
            batch,
            getX(),
            getY(),
            getWidth(),
            getHeight()
        );
        super.draw(batch, parentAlpha);
    }

    @Override public void layout() {
        table.setSize(getWidth(), getHeight());
        table.invalidate();
        table.validate();
    }

    @Override public float getPrefWidth() {
        return background.getMinWidth();
    }

    @Override public float getPrefHeight() {
        return background.getMinHeight();
    }

    public boolean handleBackPressed() {
        if (cancelEquip != null) {
            cancelEquip.run();
            return true;
        }
        return false;
    }

    public Actor getInventoryIcon(Ability ability) {
        return icons.get(ability);
    }
}
