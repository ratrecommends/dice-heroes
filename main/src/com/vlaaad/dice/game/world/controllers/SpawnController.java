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

package com.vlaaad.dice.game.world.controllers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.ui.WindowManager;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.levels.LevelElementType;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.StepDetector;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.tutorial.DiceTutorial;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.scene2d.LocTextButton;
import com.vlaaad.dice.ui.windows.CreatureInfoWindow;

import java.util.Map;
import java.util.Set;

/**
 * Created 06.10.13 by vlaaad
 */
public class SpawnController extends WorldController {

    public static final EventType<Void> START = new EventType<Void>();
    private static final float DIE_PADDING = 2;
    public TextButton startButton;
    public Table table;
    private ObjectMap<WorldObjectView, EventListener> moveListeners = new ObjectMap<WorldObjectView, EventListener>();
    private ObjectMap<WorldObjectView, EventListener> spawnListeners = new ObjectMap<WorldObjectView, EventListener>();
    private final CreatureInfoWindow creatureInfoWindow = new CreatureInfoWindow();
    private ObjectMap<Die, WorldObjectView> dieToIconToSpawn = new ObjectMap<Die, WorldObjectView>();
    private ViewController viewController;
    public final Button autoPlaceButton = new Button(Config.skin, "auto-place");
    private boolean scrollingEnabled;
    private Array<Creature> creatures;
    private Container autoPlaceContainer;

    public SpawnController(World world) {
        super(world);
    }

    private ObjectSet<Creature> placed = new ObjectSet<Creature>();

    @Override protected void start() {

        Table diceList = new Table();
        Table placeHolderList = new Table();
        diceList.setSize(ViewController.CELL_SIZE * world.viewer.creatures.size, ViewController.CELL_SIZE);
        placeHolderList.setSize(ViewController.CELL_SIZE * world.viewer.creatures.size, ViewController.CELL_SIZE);

        creatures = new Array<Creature>(world.viewer.creatures.size);
        for (Die die : world.viewer.dice) {
            creatures.add(new Creature(die, world.viewer));
        }
        creatures.sort(Ability.INITIATIVE_COMPARATOR);
        for (Creature creature : creatures) {
            WorldObjectView view = ViewController.createView(world.viewer, world.playerColors, creature);
            dieToIconToSpawn.put(creature.description, view);
            EventListener listener = createDragToSpawnListener(view, creature);
            spawnListeners.put(view, listener);
            view.addListener(listener);
            diceList.add(view).padLeft(DIE_PADDING).padRight(DIE_PADDING);
            placeHolderList.add(new Image(Config.skin, "ui-spawn-placeholder")).padLeft(DIE_PADDING).padRight(DIE_PADDING);
        }
        startButton = new LocTextButton("ui-spawn-fight", "fight", 7, 5);
        startButton.getLabel().setAlignment(Align.center);
        startButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                for (WorldObjectView view : moveListeners.keys()) {
                    view.removeListener(moveListeners.get(view));
                }
                for (Creature creature : placed) {
                    world.viewer.addCreature(creature);
                }
                world.dispatcher.dispatch(START, null);
            }
        });
        Stack stack = new Stack();
        stack.add(placeHolderList);
        stack.add(diceList);
        Table wrapper = new Table();
        wrapper.add(stack).padTop(15).padBottom(2);
        final ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setupOverscroll(10, 40, 50);
//        scrollPane.setCancelTouchFocus(false);

//        scrollPane.setScrollingDisabled(true,true);

        table = new Table(Config.skin);
        table.setBackground("ui-spawn-background");

        scrollingEnabled = scrollPane.getPrefWidth() >= world.stage.getWidth();

        table.add(scrollPane).expandX().fillX().padLeft(-1).padRight(-1).padTop(-20);
        table.setSize(world.stage.getWidth(), table.getPrefHeight());

        viewController = world.getController(ViewController.class);

        world.stage.addActor(startButton);
        startButton.setPosition(
                world.stage.getWidth() / 2 - startButton.getPrefWidth() / 2,
                world.stage.getHeight() + startButton.getPrefHeight()
        );
        world.stage.addActor(table);
        table.setPosition(world.stage.getWidth() / 2 - table.getWidth() / 2, 0);
        refreshStartButton();
        if (creatures.size > 1 && !world.viewer.tutorialProvider.isInitiativeTutorialCompleted()) {
            table.invalidate();
            table.validate();
            new Tutorial(
                    Tutorial.resources()
                            .with("world", world)
                            .with("stage", world.stage)
                            .with("tutorial-provider", world.viewer.tutorialProvider),
                    DiceTutorial.initiativeTutorialTasks()
            ).start();
        }
        autoPlaceContainer = new Container(autoPlaceButton);
        autoPlaceContainer.setFillParent(true);
        autoPlaceContainer.top().right().pad(4);
        world.stage.addActor(autoPlaceContainer);
        autoPlaceButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                autoPlace();
            }
        });
        if (scrollingEnabled) {
            if (!Tutorial.hasRunningTutorials() && !world.viewer.tutorialProvider.isSpawnSwipeTutorialCompleted()) {
                new Tutorial(
                        Tutorial.resources()
                                .with("stage", world.stage)
                                .with("world", world)
                                .with("tutorial-provider", world.viewer.tutorialProvider),
                        DiceTutorial.spawnScrollingTasks()
                ).start();
            }
        } else {
            scrollPane.setCancelTouchFocus(false);
        }

    }

    @SuppressWarnings("unchecked")
    private void autoPlace() {
        if (placed.size > 0) {
            ObjectSet<Creature> tmp = Pools.obtain(ObjectSet.class);
            tmp.addAll(placed);
            for (Creature c : tmp) {
                removeFromPlaced(c);
            }
            tmp.clear();
            Pools.free(tmp);
        }
        Array<Grid2D.Coordinate> coordinates = Pools.obtain(Array.class);
        Set<Map.Entry<Grid2D.Coordinate, Fraction>> spawns = world.level.getElements(LevelElementType.spawn);
        for (Map.Entry<Grid2D.Coordinate, Fraction> e : spawns) {
            if (e.getValue() == world.viewer.fraction) {
                coordinates.add(e.getKey());
            }
        }
        coordinates.shuffle();
        int usedCount = Math.min(creatures.size, coordinates.size);
        Array<Creature> toPlace = Pools.obtain(Array.class);
        toPlace.addAll(creatures);
        toPlace.shuffle();
        toPlace.truncate(usedCount);
        for (Creature creature : toPlace) {
            Grid2D.Coordinate coordinate = coordinates.pop();
            place(creature, coordinate.x(), coordinate.y());
        }
        toPlace.clear();
        coordinates.clear();
        Pools.free(toPlace);
        Pools.free(coordinates);
    }

    private static final Vector2 tmp = new Vector2();

    private boolean shouldHideOnDrag() {
        return viewController.worldToStageCoordinates(tmp.set(0, 0)).y < table.getHeight();
    }

    @Override protected void stop() {
        table.remove();
        startButton.remove();
        autoPlaceContainer.remove();
        autoPlaceButton.setTouchable(Touchable.disabled);

        for (WorldObjectView view : moveListeners.keys()) {
            view.removeListener(moveListeners.get(view));
        }
    }

    private DragListener createDragToSpawnListener(final WorldObjectView view, final Creature creature) {
        return new DragListener() {
            private float initialX;
            private float initialY;

            {
                setTapSquareSize(4);
            }

            private boolean shouldHideOnDrag;
            private WorldObjectView draggedView = ViewController.createView(world.viewer, world.playerColors, creature);

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!isDragging()) {
                    creatureInfoWindow.show(new CreatureInfoWindow.Params(creature, world));
                }

                if (shouldHideOnDrag) {
                    table.clearActions();
                    table.addAction(Actions.alpha(1, 0.3f));
                }
                super.touchUp(event, x, y, pointer, button);
            }

            @Override public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (scrollingEnabled) {
                    if (Math.abs(event.getStageX() - initialX) < Math.abs(event.getStageY() - initialY)) {
                        event.getStage().cancelTouchFocusExcept(this, event.getListenerActor());
                    } else {
                        return;
                    }
                }
                if (placed.contains(creature))
                    return;

//                event.getStage().cancelTouchFocus(this, event.getListenerActor());
                shouldHideOnDrag = shouldHideOnDrag();
                if (shouldHideOnDrag) {
                    table.clearActions();
                    table.addAction(Actions.alpha(0, 0.3f));
                }
                view.getColor().a = 0f;
                view.setTouchable(Touchable.disabled);
                world.stage.addActor(draggedView);
                drag(event, x, y, pointer);
                SoundManager.instance.playSound("ui-button-down");
            }

            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                initialX = event.getStageX();
                initialY = event.getStageY();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override public void drag(InputEvent event, float x, float y, int pointer) {
                if (placed.contains(creature))
                    return;
                draggedView.setPosition(event.getStageX() - view.getWidth() / 2, event.getStageY() - view.getHeight() / 2);
            }

            @Override public void dragStop(InputEvent event, float x, float y, int pointer) {
                if (placed.contains(creature))
                    return;
                draggedView.remove();
                Vector2 coordinate = world.getController(ViewController.class).root.stageToLocalCoordinates(new Vector2(
                        draggedView.getX() + draggedView.getWidth() / 2, draggedView.getY() + draggedView.getHeight() / 2
                ));
                coordinate.scl(1 / ViewController.CELL_SIZE);
                final int cellX = MathUtils.floor(coordinate.x);
                final int cellY = MathUtils.floor(coordinate.y);
                WorldObject w = world.get(cellX, cellY);
                if (cellX < 0 || cellY < 0 || cellX >= world.width || cellY >= world.height || (w != null && !(w instanceof Creature && ((Creature) w).initialPlayer == world.viewer))) {
                    view.getColor().a = 1f;
                    view.setTouchable(Touchable.enabled);
                    refreshStartButton();
                    return;
                }
                if (world.level.getElement(LevelElementType.spawn, cellX, cellY) == world.viewer.fraction) {
                    if (w != null) {
                        removeFromPlaced((Creature) w);
                    }
                    SoundManager.instance.playSound("ui-button-up");
                    place(creature, cellX, cellY);
                } else {
                    view.setTouchable(Touchable.enabled);
                    view.getColor().a = 1f;
                }
            }
        };
    }

    private void place(Creature creature, int x, int y) {
        WorldObjectView spawnView = dieToIconToSpawn.get(creature.description);
        placed.add(creature);
        refreshStartButton();
        world.add(x, y, creature);
        WorldObjectView worldView = world.getController(ViewController.class).getView(creature);
        EventListener listener = createMoveSpawnedListener(creature, worldView, spawnView);
        EventListener prev = moveListeners.remove(worldView);
        if (prev != null) {
            worldView.removeListener(prev);
        }
        moveListeners.put(worldView, listener);
        worldView.addListener(listener);
        spawnView.getColor().a = 0f;
        spawnView.setTouchable(Touchable.disabled);
    }

    private void removeFromPlaced(Creature creature) {
        placed.remove(creature);
        refreshStartButton();
        world.remove(creature);
        WorldObjectView spawnView = dieToIconToSpawn.get(creature.description);
        spawnView.getColor().a = 1f;
        spawnView.setTouchable(Touchable.enabled);
    }

    private EventListener createMoveSpawnedListener(final Creature creature, final WorldObjectView view, final WorldObjectView spawnView) {
        return new ActorGestureListener(8, 0.4f, 1.1f, 0.15f) {
            private boolean isDragging;

            @Override public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                view.toFront();
                SoundManager.instance.playSound("ui-button-down");
            }

            @Override public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!isDragging) {
                    return;
                }
                isDragging = false;
                viewController.scroller.enable();
                Vector2 coordinate = world.getController(ViewController.class).stageToWorldCoordinates(event.getStageX(), event.getStageY());
                final int cellX = (int) coordinate.x;
                final int cellY = (int) coordinate.y;
                boolean isAllowedCell = world.level.getElement(LevelElementType.spawn, cellX, cellY) == world.viewer.fraction;
                if (!isAllowedCell) {
                    removeFromPlaced(creature);
                    SoundManager.instance.playSound("ui-button-up");
                    return;
                }
                if (!world.inBounds(cellX, cellY) || world.get(cellX, cellY) != null) {
                    WorldObject prev = world.get(cellX, cellY);
                    boolean shouldSwap = false;
                    int swapX = 0;
                    int swapY = 0;
                    Creature other = null;
                    if (prev instanceof Creature) {
                        other = (Creature) prev;
                        if (other.player == world.viewer && other != creature) {
                            shouldSwap = true;
                            for (int i = 0; i < world.width; i++) {
                                for (int j = 0; j < world.height; j++) {
                                    if (world.get(i, j) == null) {
                                        swapX = i;
                                        swapY = j;
                                        shouldSwap = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (shouldSwap) {
                        int prevX = prev.getX();
                        int prevY = prev.getY();
                        int creatureX = creature.getX();
                        int creatureY = creature.getY();
                        world.move(creature, swapX, swapY);
                        world.move(prev, creatureX, creatureY);
                        world.move(creature, prevX, prevY);
                    } else {
                        view.setPosition(creature.getX() * ViewController.CELL_SIZE, creature.getY() * ViewController.CELL_SIZE);
                    }

                } else {
                    world.move(creature, cellX, cellY);
                    SoundManager.instance.playSound("ui-button-up");
                }
                world.getController(ViewController.class).resort();
            }

            @Override public void tap(InputEvent event, float x, float y, int count, int button) {
                if (event.isCancelled() || WindowManager.instance.isShown(CreatureInfoWindow.class)) {
                    world.getController(ViewController.class).resort();
                    return;
                }
                removeFromPlaced(creature);
                SoundManager.instance.playSound("ui-button-up");
            }

            private Vector2 tmp = new Vector2();

            @Override public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                Vector2 local = view.getParent().stageToLocalCoordinates(tmp.set(event.getStageX(), event.getStageY()));
                view.setPosition(local.x - ViewController.CELL_SIZE / 2, local.y - ViewController.CELL_SIZE / 2);
                isDragging = true;
                viewController.scroller.disable();
            }
        };
    }

    private void refreshStartButton() {
        startButton.setDisabled(placed.size == 0);
        startButton.clearActions();
        if (placed.size == 0) {
            startButton.addAction(Actions.moveTo(
                    world.stage.getWidth() / 2 - startButton.getWidth() / 2,
                    world.stage.getHeight() + startButton.getHeight(),
                    0.5f,
                    Interpolation.swingIn
            ));
        } else {
            startButton.addAction(Actions.moveTo(
                    world.stage.getWidth() / 2 - startButton.getWidth() / 2,
                    world.stage.getHeight() - startButton.getHeight() - 4,
                    0.5f,
                    Interpolation.swingOut
            ));
        }
    }

    public Actor getDieIconToSpawn(Die die) {
        return dieToIconToSpawn.get(die);
    }
}
