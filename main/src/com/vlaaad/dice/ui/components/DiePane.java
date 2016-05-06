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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.common.gdx.scene2d.TabPane;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.windows.DieSettingsWindow;
import com.vlaaad.dice.util.SoundHelper;

import java.util.Comparator;

/**
 * Created 17.10.13 by vlaaad
 */
public class DiePane extends WidgetGroup {
    private final Die die;
    private final UserData userData;
    private final Group diceWindowGroup;
    private final Comparator<Ability> shopComparator;

    public final Table info = new Table(Config.skin) {
        @Override public Actor hit(float x, float y, boolean touchable) {
            if (touchable && getTouchable() == Touchable.disabled) return null;
            return x >= 0 && x < getWidth() && y >= 0 && y < getHeight() ? this : null;
        }
    };
    public final Table params = new Table(Config.skin);
    private final SplitPane splitPane = new SplitPane(info, params, true, new SplitPane.SplitPaneStyle(new BaseDrawable()));
    private boolean minimized = true;
    public DieInventory inventory;
    private float infoMaxHeight;
    private float infoMinHeight = 34;
    private Table infoAbilitiesList;
    public DieNet net;
    public Table storeTabHeader;
    public DieStore store;
    private LocLabel dieNameLabel;
    public LocLabel skillLabel;

    public DiePane(Die die, UserData userData, Group diceWindowGroup) {
        this.die = die;
        this.userData = userData;
        this.diceWindowGroup = diceWindowGroup;
        shopComparator = Ability.shopComparator(die);
        setTransform(false);

        addActor(splitPane);
        splitPane.clearListeners();

        initInfoPanel();
        initParamsPanel();

        splitPane.setSplitAmount(1f);
        splitPane.setHeight(info.getPrefHeight());
        splitPane.layout();
        setSize(getPrefWidth(), splitPane.getHeight());
        params.setTouchable(Touchable.disabled);
    }

    private void initInfoPanel() {
        info.align(Align.top);
        info.setBackground(Config.skin.getDrawable("ui/dice-window/info-background"));
        WorldObjectView view = ViewController.createView(new Creature(die, PlayerHelper.defaultProtagonist));
        view.setTouchable(Touchable.disabled);
        view.removeSubView("name");

        Table infoRightRow = new Table(Config.skin);
        infoRightRow.setTouchable(Touchable.disabled);
        dieNameLabel = new LocLabel("ui-dice-window-name", Thesaurus.params()
            .with("name", die.nameLocKey())
            .with("profession", die.profession.locKey())
            .with("level", String.valueOf(die.getCurrentLevel()))
        );
        infoRightRow.add(dieNameLabel).align(Align.left).width(120).row();
        infoRightRow.add(new ExpBar(die.getCurrentProgress())).align(Align.left).width(100).row();
        infoRightRow.row();

        infoAbilitiesList = new Table();
        infoAbilitiesList.defaults().padTop(-4).padBottom(-4);
        refreshInfo();

        info.add(view).align(Align.right).padTop(4).padBottom(3);
        info.add(infoRightRow).fillX().padTop(-5).padLeft(3).row();
        info.add(infoAbilitiesList).colspan(2).row();
        info.pack();
        infoMaxHeight = info.getHeight();
    }

    private void refreshInfo() {
        infoAbilitiesList.clearChildren();
        int i = 0;
        for (Ability ability : die.abilities) {
            infoAbilitiesList.add(new AbilityIcon(ability));
            i++;
        }
        while (i < 6) {
            infoAbilitiesList.add(new AbilityIcon(null));
            i++;
        }
    }

    private void initParamsPanel() {
        Table table = new Table(Config.skin);
        table.setBackground(Config.skin.getDrawable("ui/dice-window/params-background"));
        Table equippedDesc = new Table(Config.skin);
        equippedDesc.defaults().pad(3);
        equippedDesc.align(Align.left);
        equippedDesc.add(new Image(Config.skin, "ui/dice-window/icon-equipped"));
        equippedDesc.add(new LocLabel("ui-dice-window-equipment", "inventory-text")).padLeft(0).padTop(-1);
        Button diceSettings = new Button(Config.skin, "dice-settings");
        diceSettings.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                event.stop();
                new DieSettingsWindow(diceWindowGroup).show(new DieSettingsWindow.Params(die, userData)).addListener(new RefreshListener() {
                    @Override protected void refreshed() {
                        Thesaurus.Params newParams = Thesaurus.params().withAll(dieNameLabel.getParams()).with("name", die.nameLocKey());
                        dieNameLabel.setParams(newParams);
                    }
                });
            }
        });
//
        equippedDesc.add(diceSettings).expandX().right();

        final TabPane tabPane = new TabPane(Config.skin);

        net = new DieNet(die);

        Table inventoryDesc = new Table(Config.skin);
        SoundHelper.init(inventoryDesc);
        inventoryDesc.defaults().pad(3);
        inventoryDesc.align(Align.left);
        inventoryDesc.add(new Image(Config.skin, "ui/dice-window/icon-inventory"));
        inventoryDesc.add(new LocLabel("ui-dice-window-inventory", "inventory-text")).padLeft(0).padTop(-1);

        storeTabHeader = new Table(Config.skin);
        SoundHelper.init(storeTabHeader);
        storeTabHeader.defaults().pad(3);
        storeTabHeader.align(Align.left);
        storeTabHeader.add(new Image(Config.skin, "ui/dice-window/icon-store"));
        storeTabHeader.add(new LocLabel("ui-dice-window-store", "inventory-text")).padLeft(0).padTop(-1);

        inventory = new DieInventory(die, userData);
        inventory.setNet(net);
        net.setInventory(inventory);

        store = new DieStore(die, userData, new DieStore.Callback() {
            @Override public void onAbilityBought(Ability ability) {
                tabPane.setSelectedIndex(0);
                inventory.refresh();
            }
        });

        Table storeTable = new Table();
        storeTable.add(store).pad(5);

        Table inventoryTable = new Table();
        inventoryTable.add(inventory).pad(5);


        skillLabel = new LocLabel(
            "ui-skill-desc",
            Thesaurus.params()
                .with("current", String.valueOf(die.getUsedSkill()))
                .with("total", String.valueOf(die.getTotalSkill())),
            Config.skin.getColor("inventory-text")
        );
        net.addListener(new RefreshListener() {
            @Override protected void refreshed() {
                skillLabel.setParams(Thesaurus.params()
                    .with("current", String.valueOf(die.getUsedSkill()))
                    .with("total", String.valueOf(die.getTotalSkill())));
            }
        });

        tabPane.addTab(inventoryDesc, inventoryTable);
        tabPane.addTab(storeTabHeader, storeTable);
        table.add(equippedDesc).fillX().row();
        table.add(net).pad(3).row();
        table.add(skillLabel).pad(3).padTop(-skillLabel.getHeight() - 3).left().row();
        table.add(tabPane).width(138).padLeft(-2).padRight(-2).padBottom(-2);
        table.pack();

        params.add(table).padLeft(3).padRight(3);
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        if (minimized == this.minimized)
            return;
        DiePaneListener.PaneEvent event = Pools.obtain(DiePaneListener.PaneEvent.class).setPane(this);
        if (minimized) {
            event.setType(DiePaneListener.EventType.minimize);
        } else {
            event.setType(DiePaneListener.EventType.maximize);
        }
        boolean cancelled = fire(event);
        Pools.free(event);
        if (cancelled)
            return;
        this.minimized = minimized;
        if (minimized) {
            refreshInfo();
            splitPane.clearActions();
            params.setTouchable(Touchable.disabled);
            splitPane.addAction(new TemporalAction(0.2f) {
                public float amount;
                public float height;

                @Override protected void begin() {
                    amount = splitPane.getSplit();
                    height = splitPane.getHeight();
                }

                @Override protected void update(float percent) {
                    splitPane.setHeight(MathUtils.round(height + (infoMaxHeight - height) * percent));
                    splitPane.setSplitAmount(infoMaxHeight / splitPane.getHeight());
                    setSize(getPrefWidth(), splitPane.getHeight());
                    invalidateHierarchy();
                }

                @Override protected void end() {
                    DiePaneListener.PaneEvent event = Pools
                        .obtain(DiePaneListener.PaneEvent.class)
                        .setPane(DiePane.this)
                        .setType(DiePaneListener.EventType.minimized);
                    fire(event);
                    Pools.free(event);
                }
            });
        } else {
            splitPane.addAction(new TemporalAction(0.2f) {
                public float amount;
                public float height;

                @Override protected void end() {
                    params.setTouchable(Touchable.enabled);
                    DiePaneListener.PaneEvent event = Pools
                        .obtain(DiePaneListener.PaneEvent.class)
                        .setPane(DiePane.this)
                        .setType(DiePaneListener.EventType.maximized);
                    fire(event);
                    Pools.free(event);
                    ChangeListener.ChangeEvent e = Pools.obtain(ChangeListener.ChangeEvent.class);
                    fire(e);
                    Pools.free(e);
                }

                @Override protected void begin() {
                    amount = splitPane.getSplit();
                    height = splitPane.getHeight();
                }

                @Override protected void update(float percent) {
                    splitPane.setHeight(MathUtils.round(height + (infoMinHeight + params.getPrefHeight() - height) * percent));
                    splitPane.setSplitAmount(infoMinHeight / (splitPane.getHeight()));
                    setSize(getPrefWidth(), splitPane.getHeight());
                    invalidateHierarchy();
                }
            });
        }
    }

    @Override public void invalidate() {
        super.invalidate();
        splitPane.invalidate();
    }

    @Override public void layout() {
        splitPane.invalidate();
//                    if (actor instanceof Layout) {
//                        Layout layout = (Layout) actor;
//                        layout.invalidate();
//                        layout.validate();
//                        pane.invalidate();
//                        pane.validate();
//                        items.invalidate();
//                        items.validate();
//                    }
        splitPane.validate();
    }

    @Override public void validate() {
        super.validate();
        splitPane.validate();
    }

    @Override public void setWidth(float width) {
        splitPane.setWidth(width);
        super.setWidth(width);
    }

    @Override public void setHeight(float height) {
        splitPane.setHeight(height);
        super.setHeight(height);
    }

    @Override public float getPrefWidth() {
        return splitPane.getPrefWidth() + 8;
    }

    @Override public float getPrefHeight() {
        return splitPane.getHeight();
    }

    public boolean handleBackPressed() {
        if (inventory != null && inventory.handleBackPressed()) {
            return true;
        }
        return false;
    }
}
