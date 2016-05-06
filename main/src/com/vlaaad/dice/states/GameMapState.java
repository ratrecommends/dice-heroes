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

package com.vlaaad.dice.states;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.*;
import com.vlaaad.common.gdx.State;
import com.vlaaad.common.gdx.scene2d.events.ResizeListener;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.*;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.signals.ISignalListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.ConflictResolver;
import com.vlaaad.dice.ServicesState;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.tutorial.DiceTutorial;
import com.vlaaad.dice.game.tutorial.ui.windows.TutorialMessageWindow;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.components.RefreshEvent;
import com.vlaaad.dice.ui.scene2d.LocImage;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.util.AnimationHelper;
import com.vlaaad.dice.ui.windows.*;
import com.vlaaad.dice.util.SoundHelper;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created 10.10.13 by vlaaad
 */
public class GameMapState extends State {

    private static final Color LABEL = new Color(0.36f, 0.29f, 0.27f, 1f);
    private Table itemTable;

    private final SettingsWindow settingsWindow = new SettingsWindow();
    private SettingsWindow.Callback settingsCallback = new SettingsWindow.Callback() {

        @Override public void onBuy(PurchaseInfo purchaseInfo) {
            callback.onBuy(purchaseInfo);
        }
    };

    private final UserData userData;
    private final Callback callback;
    private final ObjectMap<BaseLevelDescription, Image> buttonsByLevel = new ObjectMap<BaseLevelDescription, Image>();
    private final ObjectMap<Item, Label> labelsByItem = new ObjectMap<Item, Label>();
    public final Group diceWindowGroup = new Group();
    public final DiceWindow diceWindow = new DiceWindow(diceWindowGroup);
    public final PotionsWindow potionsWindow = new PotionsWindow(diceWindowGroup);
    private final PurchaseWindow purchaseWindow = new PurchaseWindow();
    public Button diceWindowButton;
    private final ObjectMap<BaseLevelDescription, Actor> countersByLevel = new ObjectMap<BaseLevelDescription, Actor>();
    private final Array<BaseLevelDescription> availableToCenterLevels = new Array<BaseLevelDescription>();
    private ScrollPane pane;
    private boolean shouldCenter;
    public Button potionsButton;
    private Image topBackground;
    private Button settingsButton;
    private final ExitWindow exitWindow = new ExitWindow();

    public GameMapState(final UserData userData, Callback callback) {
        super();
        this.userData = userData;
        this.callback = callback;
        userData.levelPassed.add(new ISignalListener<BaseLevelDescription>() {
            @Override public void handle(BaseLevelDescription description) {
                refreshAvailableLevels();
                shouldCenter = true;
            }
        });
        userData.itemCountChanged.add(new ISignalListener<Item>() {
            @Override public void handle(Item item) {
                Label label = labelsByItem.get(item);
                if (label == null) {
                    addItemView(item);
                } else {
                    try {
                        int count = Integer.parseInt(label.getText().toString());
                        AnimationHelper.animateCounter(label, count, userData.getItemCount(item));
                    } catch (Exception e) {
                        Logger.error("could not animate counter", e);
                        label.setText(String.valueOf(userData.getItemCount(item)));
                    }
                }
                diceWindow.fire(RefreshEvent.INSTANCE);
            }
        });
    }


    public Actor getLevelIcon(BaseLevelDescription description) {
        return buttonsByLevel.get(description);
    }

    private void addItemView(final Item item) {
        if (item.type != Item.Type.resource)
            return;
        Button button = new Button(Config.skin, "get" + item.name);
        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                purchaseWindow.show(new PurchaseWindow.Params(item, new PurchaseWindow.Callback() {
                    @Override public void onBuy(PurchaseInfo info) {
                        callback.onBuy(info);
                    }
                }));
            }
        });
        SoundHelper.initButton(button);
        itemTable.add(button).padRight(-8).padLeft(-2);
        itemTable.add(new Image(Config.skin, "item/" + item.name));
        Label label = new Label(String.valueOf(userData.getItemCount(item)), Config.skin, "default", Color.WHITE);
        labelsByItem.put(item, label);
        itemTable.add(label).minWidth(27).padTop(-3).padLeft(-2).padRight(-2);
    }

    private final Color INACTIVE = new Color(1, 1, 1, 0.4f);

    private void refreshAvailableLevels() {
        for (BaseLevelDescription level : Config.levels) {
            if (level.hidden)
                continue;
            Image image = buttonsByLevel.get(level);
            Actor label = countersByLevel.get(level);
            String iconName = "ui/level-icon/" + level.name;
            if (!level.canBeStarted(userData)) {
                if (label != null) label.setColor(INACTIVE);
                image.setDrawable(Config.skin.getDrawable("ui/level-icon/unknown" + level.iconStyle));
            } else if (userData.isPassed(level)) {
                if (label != null) label.setColor(Color.WHITE);
                image.setDrawable(Config.skin.getDrawable("ui/level-icon/passed" + level.iconStyle));
            } else {
                if (label != null) label.setColor(Color.WHITE);
                image.setDrawable(Config.skin.getDrawable(Config.skin.has(iconName, TextureRegion.class) ? iconName : "ui/level-icon/default" + level.iconStyle));
            }
        }
    }

    @Override protected void init() {
        addMap();
        addGameServicesButtons();
        addItems();
        addSettingsButton();
        addDiceButton();
        addPotionsButton();
        stage.addListener(new ResizeListener() {
            @Override protected void resize() {
                topBackground.setSize(stage.getWidth(), topBackground.getPrefHeight());
                topBackground.setY(stage.getHeight() - topBackground.getHeight());
                diceWindowButton.setPosition(stage.getWidth() / 2 - diceWindowButton.getWidth() / 2, stage.getHeight() - diceWindowButton.getHeight());
                potionsButton.setPosition(diceWindowButton.getX() + diceWindowButton.getWidth() + 2, stage.getHeight() - potionsButton.getHeight());
                settingsButton.setPosition(stage.getWidth() - settingsButton.getWidth(), stage.getHeight() - 12 - settingsButton.getHeight() / 2);
                pane.setSize(stage.getWidth(), stage.getHeight());
                pane.layout();
                pane.updateVisualScroll();
                diceWindowGroup.setSize(stage.getWidth(), topBackground.getY());
            }
        });

        if (Gdx.app.getType() != Application.ApplicationType.Desktop)
            return;
        stage.addListener(new InputListener() {
            @Override public boolean keyTyped(InputEvent event, char character) {
                if (character == '1') {
                    new LoseWindow().show(new LoseWindow.Params(
                        new LevelResult(new ObjectIntMap<Die>(), new ObjectMap<Fraction, Player>(), PlayerHelper.defaultProtagonist),
                        new LoseWindow.Callback() {
                            @Override public void onRestart() {
                            }

                            @Override public void onClose() {
                            }
                        }
                    ));
                } else if (character == '2') {
                    LevelDescription level = (LevelDescription) Config.levels.get("m-12");
                    String shareKey = "share-level-" + level.name;
                    if (!Config.thesaurus.keyExists(shareKey))
                        shareKey = "share-level-default";
                    final Array<Reward> rewards = new Array<Reward>();
                    rewards.addAll(((LevelDescription) Config.levels.get("final-boss")).passRewards);
                    rewards.addAll(((LevelDescription) Config.levels.get("tutorial")).passRewards);
                    final Array<RewardResult> results = ArrayHelper.map(rewards, new Function<Reward, RewardResult>() {
                        @Override public RewardResult apply(Reward reward) {
                            return reward.apply(userData);
                        }
                    });
                    new RewardWindow().show(
                        new RewardWindow.Params(results,
                            Config.thesaurus.localize(shareKey, Thesaurus.params().with("level", String.valueOf(level.getLevelNumber()))),
                            new LevelResult(new ObjectIntMap<Die>(), new ObjectMap<Fraction, Player>(), PlayerHelper.defaultProtagonist),
                            new RewardWindow.Callback() {
                                @Override public void onClose() {
                                    for (Reward reward : rewards) {
                                        reward.apply(userData);
                                    }
                                }
                            }, userData
                        )
                    );
                } else if (character == '3') {
                    Config.achievements.visualizer().unlock(Config.achievements.get("first-ingredient"));
                } else if (character == '4') {
                    Config.achievements.visualizer().showAchievements();
                } else if (character == '5') {
                    new ConflictWindow().show(new ConflictWindow.Params(userData, UserData.deserialize(new Yaml().load(Gdx.files.internal("initial-game-data.yml").read())), new ConflictWindow.Callback() {
                        @Override public void onResult(ConflictResolution conflictResolution) {
                            System.out.println("result: " + conflictResolution);
                        }
                    }));
                } else if (character == '6') {
                    final Array<Reward> rewards = ((LevelDescription) Config.levels.get("tutorial")).passRewards;
                    final Array<RewardResult> results = ArrayHelper.map(rewards, new Function<Reward, RewardResult>() {
                        @Override public RewardResult apply(Reward reward) {
                            return reward.apply(userData);
                        }
                    });
                    new PvpWinWindow().show(new PvpWinWindow.Params(results, "", new LevelResult(new ObjectIntMap<Die>(), new ObjectMap<Fraction, Player>(), PlayerHelper.defaultProtagonist), new PvpWinWindow.Callback() {
                        @Override public void onClose() {
                        }

                        @Override public void onRestart() {
                        }
                    }, userData));
                } else if (character == '7') {
                    new LoseWindow().show(new LoseWindow.Params(new LevelResult(new ObjectIntMap<Die>(), new ObjectMap<Fraction, Player>(), PlayerHelper.defaultAntagonist), new LoseWindow.Callback() {
                        @Override public void onRestart() {
                        }

                        @Override public void onClose() {
                        }
                    }));
                } else if (character == '8') {
                    new PauseWindow().show(new PauseWindow.Params(new PauseWindow.Callback() {
                        @Override public void onRestart() {
                        }

                        @Override public void onCancel() {
                        }
                    }, true));
                } else if (character == '*') {
                    new PauseWindow().show(new PauseWindow.Params(new PauseWindow.Callback() {
                        @Override public void onRestart() {
                        }

                        @Override public void onCancel() {
                        }
                    }, false));
                } else if (character == 'e') {
                    new DisconnectedWindow().show(Tuple3.make(false,
                        Option.some(Config.thesaurus.localize("disconnect-exception-on-receive")),
                        Option.<Throwable>some(new RuntimeException())
                    ));
                } else if (character == 'E') {
                    new DisconnectedWindow().show(Tuple3.make(false,
                        Option.some(Config.thesaurus.localize("disconnect-exception-on-receive")),
                        Option.<Throwable>none()
                    ));
                } else if (character == 'w') {

                    final GameWindow<IFuture<?>> window = new BlockingWindow().show(new Future());
                    window.addCaptureListener(new InputListener() {
                        @Override public boolean keyDown(InputEvent event, int keycode) {
                            if (keycode == Input.Keys.ESCAPE) {
                                window.hide();
                            }
                            return super.keyDown(event, keycode);
                        }
                    });
                } else if (character == 't') {
                    new TutorialMessageWindow("tutorial-pic-1").show("tutorial-intro-1");
                } else if (character == '9') {
                    new TutorialMessageWindow("ending-pic", 28, 85).show("tutorial-final-ending");
                }
                return super.keyTyped(event, character);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void addGameServicesButtons() {
        final Table table = new Table(Config.skin);
        table.setFillParent(true);
        table.bottom().left();
        final Table content = new Table();
        content.left();
        table.add(content).expandX().fillX().height(22);
        stage.addActor(table);
        Config.mobileApi.services().dispatcher().addListener(new IStateDispatcher.Listener<ServicesState>() {
            final Ref<ActorGestureListener> listenerVar = new Ref<ActorGestureListener>();
            final Table iconContainer = new Table();
            boolean isShown;

            @Override public void onChangedState(ServicesState newState) {
                content.clearChildren();
                if (newState == ServicesState.CONNECTED) {
                    Group servicesButton = new Group();
                    Image image = new Image(Config.skin, "ui/button/services-up");
                    final Image notification = new Image();
                    notification.setScaling(Scaling.none);
                    notification.setPosition(image.getWidth() - 1, image.getHeight() - 4);
                    final Group arrow = new Group();
                    final Tile arrowUp = new Tile("ui/button/arrow-hide");
                    final Tile arrowDown = new Tile("ui/button/arrow-show");
                    image.setPosition(2, -1);
                    arrowDown.getColor().a = 0;
                    arrow.addActor(arrowUp);
                    arrow.addActor(arrowDown);
                    arrow.setSize(arrowUp.getWidth(), arrowUp.getHeight());
                    arrow.setOrigin(arrow.getWidth() / 2, arrow.getHeight() / 2);
                    servicesButton.setSize(image.getWidth() + 1 + arrow.getWidth(), image.getHeight());
                    servicesButton.addActor(image);
                    servicesButton.addActor(arrow);
                    servicesButton.addActor(notification);
                    arrow.setPosition(image.getWidth() + 1, image.getHeight() / 2 - arrow.getHeight() / 2);
                    content.add(servicesButton);

                    stage.getRoot().addActorBefore(table, iconContainer);
                    iconContainer.clearChildren();
                    iconContainer.bottom().setFillParent(true);
                    Table icons = new Table(Config.skin);
                    icons.left().setBackground(Config.skin.newDrawable("particle-white-pixel", new Color(0, 0, 0, 0.25f)));
                    icons.defaults().padBottom(-2);
                    iconContainer.add(icons).expandX().fillX().height(22);
                    icons.add().width(servicesButton.getWidth()).padRight(2);
                    iconContainer.setY(-22);
                    listenerVar.set(new ActorGestureListener() {
                        @Override public void tap(InputEvent event, float x, float y, int count, int button) {
                            notification.setDrawable(null);
                            iconContainer.clearActions();
                            arrowUp.clearActions();
                            arrowDown.clearActions();
                            isShown = !isShown;
                            Config.preferences.setServicesPaneShownByDefault(isShown);
                            arrow.addAction(Actions.rotateBy(180, 0.25f, Interpolation.pow3Out));
                            if (isShown) {
                                arrowDown.addAction(Actions.alpha(1, 0.25f, Interpolation.pow3Out));
                                arrowUp.addAction(Actions.alpha(0, 0.25f, Interpolation.pow3Out));
                                iconContainer.addAction(Actions.moveTo(0, 0, 0.25f, Interpolation.pow3Out));
                            } else {
                                arrowDown.addAction(Actions.alpha(0, 0.25f, Interpolation.pow3Out));
                                arrowUp.addAction(Actions.alpha(1, 0.25f, Interpolation.pow3Out));
                                iconContainer.addAction(Actions.moveTo(0, -22, 0.25f, Interpolation.pow3Out));
                            }
                        }
                    });
                    servicesButton.addListener(listenerVar.get());
                    final Cell conflictCell = icons.add();
                    final Cell inviteCell = icons.add();

                    Button achievements = new Button(Config.skin, "achievements");
                    achievements.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            Config.mobileApi.services().gameAchievements().showAchievements();
                        }
                    });
                    icons.add(achievements).padLeft(4);

                    Button leaderboards = new Button(Config.skin, "leaderboards");
                    leaderboards.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            Config.mobileApi.services().showLeaderboard("CgkIsNnQ2ZcKEAIQFw");
                        }
                    });
                    icons.add(leaderboards).padLeft(4);

                    Button signOut = new Button(Config.skin, "sign-out");
                    signOut.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            Config.mobileApi.services().signOut();
                        }
                    });
                    icons.add(signOut).expandX().right().padRight(2);

                    final Button conflict = new Button(Config.skin, "conflict");
                    conflict.addAction(Actions.forever(Actions.sequence(
                        Actions.color(Color.valueOf("eb653c"), 0.5f, Interpolation.sine),
                        Actions.color(Color.WHITE, 0.5f, Interpolation.sine)
                    )));
                    conflict.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            new ConflictWindow().show(new ConflictWindow.Params(userData, UserData.deserialize(callback.getConflictServerData()), new ConflictWindow.Callback() {
                                @Override public void onResult(ConflictResolution conflictResolution) {
                                    callback.resolveConflictingState(conflictResolution);
                                }
                            }));
                        }
                    });
                    final Button invitesButton = new Button(Config.skin, "invite");
                    invitesButton.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            new BlockingWindow().show(Config.mobileApi.services().multiplayer().displayInvitations());
                        }
                    });
                    invitesButton.setColor(Color.GREEN);

                    Config.mobileApi.services().multiplayer().invites().addListener(new IStateDispatcher.Listener<Integer>() {
                        @Override public void onChangedState(Integer invites) {
//                            Logger.debug("invites count: " + invites);
                            if (invites > 0) {
                                if (!isShown) {
                                    notification.setDrawable(Config.skin.getDrawable("ui/button/notification"));
                                }
                                inviteCell.padLeft(4);
                                inviteCell.setActor(invitesButton);
                            } else {
                                inviteCell.padRight(0);
                                inviteCell.setActor(null);
                            }
                        }
                    }, true);

                    callback.dispatcher().addListener(new IStateDispatcher.Listener<ConflictResolver.ResolverState>() {
                        @Override public void onChangedState(ConflictResolver.ResolverState newState) {
                            if (newState == ConflictResolver.ResolverState.hasConflict) {
                                if (!isShown) {
                                    notification.setDrawable(Config.skin.getDrawable("ui/button/notification"));
                                }
                                conflictCell.padLeft(4);
                                conflictCell.setActor(conflict);
                            } else {
                                conflictCell.padRight(0);
                                conflictCell.setActor(null);
                            }
                        }
                    }, true);

                    if (!isShown && Config.preferences.isServicesPaneShownByDefault()) {
                        listenerVar.get().tap(null, 0, 0, 0, 0);
                    }
                } else {
                    Button signIn = new Button(Config.skin, "services-sign-in");
                    signIn.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            Config.mobileApi.services().signIn();
                        }
                    });
                    content.add(signIn).left().padBottom(-4).padLeft(1);
                    if (isShown && listenerVar.get() != null) {
                        listenerVar.get().tap(null, 0, 0, 0, 0);
                        Config.preferences.setServicesPaneShownByDefault(true);
                        listenerVar.clear();
                    }
                }
            }
        }, true);
    }

    private void addDiceButton() {
        diceWindowButton = new Button(Config.skin, "dice");
        SoundHelper.initButton(diceWindowButton);
        diceWindowButton.setPosition(stage.getWidth() / 2 - diceWindowButton.getWidth() / 2, stage.getHeight() - diceWindowButton.getHeight());
        stage.addActor(diceWindowButton);
        diceWindowButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (diceWindow.isShown()) {
                    diceWindow.hide();
                } else {
                    diceWindow.show(userData);
                }
                if (potionsWindow.isShown())
                    potionsWindow.hide();
            }
        });
    }

    private void addPotionsButton() {
        potionsButton = new Button(Config.skin, "potions");
        SoundHelper.initButton(potionsButton);
        potionsButton.setPosition(diceWindowButton.getX() + diceWindowButton.getWidth() + 2, stage.getHeight() - potionsButton.getHeight());
        stage.addActor(potionsButton);
        potionsButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (potionsWindow.isShown()) {
                    potionsWindow.hide();
                } else {
                    potionsWindow.show(userData);
                }
                if (diceWindow.isShown())
                    diceWindow.hide();
            }
        });
        updatePotionsButton();
    }

    private void updatePotionsButton() {
        potionsButton.setVisible(userData.potionsAvailable);
        if (userData.potionsAvailable && !userData.potionsTutorialCompleted && !Tutorial.hasRunningTutorials()) {
            Tutorial.TutorialResources resources = Tutorial.resources()
                .with("userData", userData)
                .with("state", GameMapState.this)
                .with("stage", stage);
            new Tutorial(resources, DiceTutorial.potionsTasks()).start();
        }
    }

    private void addSettingsButton() {
        settingsButton = new Button(Config.skin, "settings");
        SoundHelper.initButton(settingsButton);
        settingsButton.setPosition(stage.getWidth() - settingsButton.getWidth(), stage.getHeight() - 12 - settingsButton.getHeight() / 2);
        stage.addActor(settingsButton);
        settingsButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                settingsWindow.show(new SettingsWindow.Params(userData, settingsCallback));
            }
        });
    }

    @Override protected void onMenuPressed() {
        settingsWindow.show(new SettingsWindow.Params(userData, settingsCallback));
    }

    private void addItems() {
        topBackground = new Image(new TiledDrawable(Config.skin.getRegion("ui-top-background")));
        topBackground.setSize(stage.getWidth(), topBackground.getPrefHeight());
        topBackground.setY(stage.getHeight() - topBackground.getHeight());

        itemTable = new Table(Config.skin);
        itemTable.setFillParent(true);
        itemTable.align(Align.top | Align.left);
        Array<Item> items = new Array<Item>();
        for (Item item : Config.items) {
            items.add(item);
        }
        items.sort(Item.ORDER_COMPARATOR);
        for (Item item : items) {
            addItemView(item);
        }

        stage.addActor(diceWindowGroup);
        stage.addActor(topBackground);
        stage.addActor(itemTable);
        diceWindowGroup.setSize(stage.getWidth(), topBackground.getY());
        diceWindowGroup.setTouchable(Touchable.childrenOnly);
    }

    private void addMap() {
        Image map = new Image(Config.assetManager.get("world-map.atlas", TextureAtlas.class).findRegion("world-map")) {
            @Override public void draw(Batch batch, float parentAlpha) {
                batch.disableBlending();
                super.draw(batch, parentAlpha);
                batch.enableBlending();
            }
        };

        Group group = new Group();
        group.addActor(map);

        ArrayList<Map> labels = MapHelper.get(Config.worldMapParams, "labels");
        for (Map labelInfo : labels) {
            Label label = new LocLabel(MapHelper.get(labelInfo, "key", "NOT SPECIFIED"));
            label.setPosition(
                MapHelper.get(labelInfo, "x", Numbers.ZERO).floatValue() - 50,
                MapHelper.get(labelInfo, "y", Numbers.ZERO).floatValue() - 50
            );
            label.setAlignment(Align.center);
            label.setSize(100, 100);
            label.setColor(LABEL);
            label.setTouchable(Touchable.disabled);

            group.addActor(label);
        }

        ArrayList<Map> images = MapHelper.get(Config.worldMapParams, "images");
        for (Map imageInfo : images) {
            Image image = new LocImage(MapHelper.get(imageInfo, "name", "NOT SPECIFIED"));
            image.setPosition(
                MapHelper.get(imageInfo, "x", Numbers.ZERO).floatValue() - 50,
                MapHelper.get(imageInfo, "y", Numbers.ZERO).floatValue() - 50
            );
            image.setSize(100, 100);
            image.setTouchable(Touchable.disabled);
            image.setScaling(Scaling.none);

            group.addActor(image);
        }


        for (BaseLevelDescription level : Config.levels) {
            if (level.hidden)
                continue;
            Table table = new Table();
            Image button = new Image(Config.skin.getDrawable("ui/level-icon/unknown"));
            button.setScaling(Scaling.none);
            button.setAlign(Align.bottom | Align.left);
            SoundHelper.init(button);
            button.setSize(button.getPrefWidth(), button.getPrefHeight());
            button.addListener(createStartLevelListener(level));
            buttonsByLevel.put(level, button);

            table.add(button).size(button.getWidth(), button.getHeight());
            Stack stack = new Stack();
            stack.setSize(100, 100);
            stack.setPosition(level.iconX /*- button.getWidth() / 2 */ - 50, level.iconY /*- button.getHeight() / 2 */ - 50);
            stack.add(table);

            if (level.useNumberInIcon) {
                int counter = 1;
                BaseLevelDescription check = level;
                while (check.parent != null) {
                    counter++;
                    check = Config.levels.get(check.parent);
                }
                Table labelTable = new Table(Config.skin);
                Label label = new Label(String.valueOf(counter), Config.skin);
                label.setTouchable(Touchable.disabled);
                labelTable.add(label).padLeft(1).padTop(-2);
                countersByLevel.put(level, label);
                stack.add(labelTable);
            } else if (level.levelIcon != null) {
                Table iconTable = new Table();
                iconTable.setTouchable(Touchable.disabled);
                Tile icon = new Tile("ui/level-icon/" + level.levelIcon);
                iconTable.add(icon).padTop(0);
                countersByLevel.put(level, icon);
                stack.add(iconTable);
            }
            group.addActor(stack);
        }
        refreshAvailableLevels();
        group.setSize(map.getPrefWidth(), map.getPrefHeight());

        pane = new ScrollPane(group, new ScrollPane.ScrollPaneStyle());
        pane.setOverscroll(false, false);
        pane.setFillParent(true);
        pane.setFlingTime(0.3f);
//        pane.setSmoothScrolling(false);
        pane.setSize(stage.getWidth(), stage.getHeight());

        stage.addActor(pane);

        centerOnLevel(true);

        map.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Logger.log(x + ", " + y);
            }
        });
    }

    private void centerOnLevel(boolean force) {
        if (pane == null)
            return;
        availableToCenterLevels.clear();
        userData.getAvailableLevels(availableToCenterLevels);
        BaseLevelDescription centered = null;
        if (availableToCenterLevels.size == 1) {
            centered = availableToCenterLevels.first();
        } else if (availableToCenterLevels.size > 1) {
            BaseLevelDescription lastPassed = userData.getLastPassedLevel();
            if (lastPassed != null) {
                for (BaseLevelDescription level : availableToCenterLevels) {
                    if (level.getParentLevel() == lastPassed) {
                        centered = level;
                        break;
                    }
                }
            }
            if (centered == null) centered = availableToCenterLevels.random();
        }

        if (centered == null) centered = userData.getLastPassedLevel();
        if (centered == null) {
            for (BaseLevelDescription level : Config.levels) {
                if (level.parent == null) {
                    centered = level;
                    break;
                }
            }
        }
        if (centered != null) {
            centerOnLevel(centered, force);
        }
    }

    public void centerOnLevel(BaseLevelDescription level, boolean immediately) {
        pane.layout();
        float x = level.iconX - stage.getWidth() / 2;
        float y = level.iconY - stage.getHeight() / 2;
        pane.scrollTo(x, y, stage.getWidth(), stage.getHeight());
        if (immediately) pane.updateVisualScroll();
    }

    private EventListener createStartLevelListener(final BaseLevelDescription level) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!level.canBeStarted(userData))
                    return;
                callback.onStartLevel(level);
            }
        };
    }

    @Override protected void resume(boolean isStateChange) {
        if (isStateChange) {
            SoundManager.instance.playMusicBeautifully("map", stage);
        }
        if (shouldCenter) {
            centerOnLevel(false);
            shouldCenter = false;
        }
        if (!userData.potionsAvailable && (userData.isPassed(Config.levels.get("w-7")) || userData.hasItems(Item.Type.ingredient))) {
            userData.potionsAvailable = true;
        }
        updatePotionsButton();

    }

    @Override protected void pause(boolean isStateChange, Stage stage) {
        if (isStateChange) {
            if (stage != null) {
                SoundManager.instance.stopMusicBeautifully("map", stage);
            } else {
                SoundManager.instance.stopMusic("map");
            }
        }
    }

    @Override protected void onBackPressed() {
        if (exitWindow.isShown()) {
            exitWindow.hide();
        } else {
            exitWindow.show(null);
        }
    }

    @Override protected void dispose(boolean isStateChange, Stage stage) {
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                GameMapState.this.stage.dispose();
            }
        });
    }

    public static enum ConflictResolution {
        useLocal, useServer
    }


    public static interface Callback {
        public void onStartLevel(BaseLevelDescription level);

        public void onBuy(PurchaseInfo info);

        public IStateDispatcher<ConflictResolver.ResolverState> dispatcher();

        public void resolveConflictingState(ConflictResolution resolution);

        public Map getConflictServerData();
    }
}
