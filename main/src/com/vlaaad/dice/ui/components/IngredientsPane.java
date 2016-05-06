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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.achievements.events.EventType;
import com.vlaaad.dice.achievements.events.imp.BrewEvent;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.windows.IngredientWindow;
import com.vlaaad.dice.ui.windows.PotionsWindow;

/**
 * Created 09.03.14 by vlaaad
 */
public class IngredientsPane extends Table {
    private static final Vector2 tmp = new Vector2();

    private final UserData userData;
    private final CraftingPane craftingPane;
    private final ObjectMap<Item, ItemCountIcon> icons = new ObjectMap<Item, ItemCountIcon>();
    private static final Runnable empty = new Runnable() {
        @Override public void run() {
        }
    };

    public IngredientsPane(final UserData userData, final CraftingPane craftingPane, final PotionsWindow window) {
        super(Config.skin);
        this.userData = userData;
        this.craftingPane = craftingPane;
        craftingPane.clean();
        craftingPane.addListener(new CraftingPane.Listener() {
            @Override protected void onIngredientReplaced(Item item) {
                ItemCountIcon icon = icons.get(item);
                icon.setCount(icon.getCount() + 1);
            }
        });
        craftingPane.addListener(new ActorGestureListener(8, 0.4f, 1.1f, 0.15f) {
            private boolean alreadyPanning;
            public ItemIcon pannedIcon;
            public float stageX;
            public float stageY;

            @Override public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                stageX = event.getStageX();
                stageY = event.getStageY();
                event.stop();
                SoundManager.instance.playSound("ui-button-down");
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pannedIcon != null) {
                    if (!craftingPane.swapIngredients(pannedIcon, stageX, stageY)) {
                        ItemCountIcon icon = icons.get(pannedIcon.item);
                        icon.setCount(icon.getCount() + 1);
                    }
                    pannedIcon.remove();
                    SoundManager.instance.playSound("ui-button-up");
                }
                alreadyPanning = false;
                pannedIcon = null;
            }

            @Override public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                if (!alreadyPanning) {
                    alreadyPanning = true;
                    pannedIcon = craftingPane.removeIngredient(stageX, stageY);
                    if (pannedIcon != null) {
                        getStage().addActor(pannedIcon);
                        pannedIcon.setPosition(event.getStageX() - pannedIcon.getWidth() / 2, event.getStageY() - pannedIcon.getWidth() / 2);
                    }
                } else if (pannedIcon != null) {
                    pannedIcon.moveBy(deltaX, deltaY);
                }
            }

            @Override public void tap(InputEvent event, float x, float y, int count, int button) {
                final Actor actor = event.getTarget();
                if (!(actor instanceof AbilityIcon))
                    return;
                window.scrollTo(window.potionsList, new Runnable() {
                    @Override public void run() {
                        Ability ability = ((AbilityIcon) actor).ability;
                        if (!userData.canWithdraw(craftingPane.res))
                            return;
                        userData.withdrawItems(craftingPane.res);
                        userData.addPotion(ability);
                        craftingPane.clean();
                        Config.achievements.fire(EventType.brewPotion, Pools.obtain(BrewEvent.class).potion(ability));
                    }
                });
            }
        });
        setBackground("ui/dice-window/inventory-background");
        defaults().size(24).padLeft(1).padRight(-2).padBottom(-1);
        top().left();
        int i = 0;
        for (Item item : Config.items) {
            if (item.type != Item.Type.ingredient)
                continue;
            ItemCountIcon icon = new ItemCountIcon(item, userData.getItemCount(item));
            icons.put(item, icon);
            icon.addListener(createListener(icon));
            add(icon);
            i++;
            if (i % 5 == 0) row();
        }
    }

    public void showIngredientWindow(Item item) {
        final ItemCountIcon icon = icons.get(item);
        IngredientWindow window = new IngredientWindow();
        window.show(new IngredientWindow.Params(item, userData));
        window.addListener(new IngredientWindow.Listener() {
            @Override protected void bought() {
                icon.setCount(icon.getCount() + 1);
            }
        });
    }

    private EventListener createListener(final ItemCountIcon icon) {
        return new ActorGestureListener(1.5f, 0.4f, 1.1f, 0.15f) {
            private final ItemIcon pannedIcon = new ItemIcon(icon.item);
            public boolean alreadyPanning;

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (alreadyPanning && !craftingPane.putIngredient(pannedIcon)) {
                    icon.setCount(icon.getCount() + 1);
                }
                alreadyPanning = false;
                pannedIcon.remove();
                SoundManager.instance.playSound("ui-button-up");
            }

            @Override public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                SoundManager.instance.playSound("ui-button-down");
            }

            @Override public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                if (!alreadyPanning) {
                    if (icon.getCount() == 0)
                        return;
                    icon.setCount(icon.getCount() - 1);
                    alreadyPanning = true;
                    getStage().addActor(pannedIcon);
                    pannedIcon.setPosition(event.getStageX() - pannedIcon.getWidth() / 2, event.getStageY() - pannedIcon.getWidth() / 2);
                } else {
                    pannedIcon.moveBy(deltaX, deltaY);
                }
            }

            @Override public void tap(InputEvent event, float x, float y, int count, int button) {
                IngredientWindow window = new IngredientWindow();
                window.show(new IngredientWindow.Params(icon.item, userData));
                window.addListener(new IngredientWindow.Listener() {
                    @Override protected void bought() {
                        icon.setCount(icon.getCount() + 1);
                    }
                });
            }
        };
    }
}
