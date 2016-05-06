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

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.objects.WorldObject;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.events.MoveEvent;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.util.PlayerColors;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.game.world.view.*;

import java.util.*;

import static com.vlaaad.dice.game.config.levels.LevelElementType.*;

/**
 * Created 06.10.13 by vlaaad
 */
public class ViewController extends WorldController {
    public static final float CELL_SIZE = 24;

    private static final Vector2 tmp = new Vector2();

    private final Comparator<Actor> comparator = new Comparator<Actor>() {
        @Override public int compare(Actor o1, Actor o2) {
            WorldObject wo1 = viewObjectMap.get(o1);
            WorldObject wo2 = viewObjectMap.get(o2);
            int dt = wo2.getY() - wo1.getY();
            if (dt != 0)
                return dt;
            return wo1.viewPriority - wo2.viewPriority;
        }
    };

    public final Group root = new Group() {
        @Override public void act(float delta) {
            super.act(delta);
            if (resortNeeded) {
                resortNeeded = false;
                objectLayer.getChildren().sort(comparator);
            }
        }
    };

    public final Group backgroundLayer = new Group();
    public final Group tileBackgroundLayer = new Group();
    public final Group tileLayer = new Group();
    public final Group overTileLayer = new Group();
    public final Group coverLayer = new Group();
    public final Group stepDetectorLayer = new Group();
    public final Group selectionLayer = new Group();
    public final Group spawnLayer = new Group();
    public final Group objectLayer = new Group();
    public final Group effectLayer = new Group();
    public final Group notificationLayer = new Group();

    private final ObjectMap<WorldObject, WorldObjectView> removedViews = new ObjectMap<WorldObject, WorldObjectView>();
    private final ObjectMap<WorldObject, WorldObjectView> objectViewMap = new ObjectMap<WorldObject, WorldObjectView>();
    private final ObjectMap<Actor, WorldObject> viewObjectMap = new ObjectMap<Actor, WorldObject>();
    private ObjectMap<Object, Array<Image>> selectionMap = new ObjectMap<Object, Array<Image>>();

    private final ResultVisualizer visualizer;
    public final ViewScroller scroller;

    private final Image turnSelection = new Image(Config.skin, "selection/turn");
    private Creature turnObject;
    private boolean resortNeeded;

    public ViewController(final World world) {
        super(world);
        visualizer = new ResultVisualizer(this);
        world.stage.addActor(root);
        root.addActor(backgroundLayer);
        root.addActor(tileBackgroundLayer);
        root.addActor(tileLayer);
        root.addActor(overTileLayer);
        root.addActor(coverLayer);
        root.addActor(stepDetectorLayer);
        root.addActor(spawnLayer);
        root.addActor(selectionLayer);
        root.addActor(objectLayer);
        root.addActor(effectLayer);
        root.addActor(notificationLayer);

        initLayer(root);
        root.setTransform(true);
        initLayer(backgroundLayer);
        initLayer(tileBackgroundLayer);
        initLayer(tileLayer);
        initLayer(overTileLayer);
        initLayer(selectionLayer);
        initLayer(coverLayer);
        initLayer(stepDetectorLayer);
        initLayer(spawnLayer);
        initLayer(objectLayer);
        initLayer(effectLayer);
        initLayer(notificationLayer);

        backgroundLayer.setTouchable(Touchable.disabled);
        selectionLayer.setTouchable(Touchable.disabled);
        tileBackgroundLayer.setTouchable(Touchable.disabled);
        tileLayer.setTouchable(Touchable.disabled);
        overTileLayer.setTouchable(Touchable.disabled);
        spawnLayer.setTouchable(Touchable.disabled);
        effectLayer.setTouchable(Touchable.disabled);
        coverLayer.setTouchable(Touchable.disabled);
        stepDetectorLayer.setTouchable(Touchable.disabled);
        notificationLayer.setTouchable(Touchable.disabled);

        root.setPosition(
            world.stage.getWidth() / 2 - root.getWidth() / 2,
            world.stage.getHeight() / 2 - root.getHeight() / 2
        );

        world.dispatcher.add(PveLoadLevelController.LOAD_TILE, new EventListener<TileInfo>() {
            @Override public void handle(EventType<TileInfo> type, TileInfo tileInfo) {
                addImageToLayer(
                    tileLayer,
                    tileInfo.x * CELL_SIZE,
                    tileInfo.y * CELL_SIZE,
                    "tile/" + tileInfo.image + ((tileInfo.x + tileInfo.y) % 2 == 1 ? "-odd" : "-even")
                );
            }
        });
        world.dispatcher.add(PveLoadLevelController.ADD_SPAWN_POINT, new EventListener<SpawnPoint>() {
            @Override public void handle(EventType<SpawnPoint> type, SpawnPoint spawnPoint) {
                if (world.viewer.fraction != spawnPoint.fraction)
                    return;
                Tile spawn = addImageToLayer(spawnLayer, spawnPoint.x * CELL_SIZE, spawnPoint.y * CELL_SIZE, "selection/spawn");
                blink(spawn, 0.4f, 1f);
            }
        });
        world.dispatcher.add(World.ADD_WORLD_OBJECT, new EventListener<WorldObject>() {
            @Override public void handle(EventType<WorldObject> type, WorldObject worldObject) {
                addView(worldObject);
            }
        });
        world.dispatcher.add(World.REMOVE_WORLD_OBJECT, new EventListener<WorldObject>() {
            @Override public void handle(EventType<WorldObject> type, WorldObject worldObject) {
                removeView(worldObject);
            }
        });
        world.dispatcher.add(SpawnController.START, new EventListener<Void>() {
            @Override public void handle(EventType<Void> type, Void aVoid) {
                spawnLayer.clear();
            }
        });
        world.dispatcher.add(World.MOVE_WORLD_OBJECT, new EventListener<MoveEvent>() {
            @Override public void handle(EventType<MoveEvent> type, MoveEvent event) {
                moveView(event.object);
            }
        });
        world.dispatcher.add(RoundController.TURN_STARTED, new EventListener<Creature>() {
            @Override public void handle(EventType<Creature> type, Creature creature) {

                turnSelection.setPosition(creature.getX() * CELL_SIZE, creature.getY() * CELL_SIZE);
                selectionLayer.addActor(turnSelection);
                turnObject = creature;
            }
        });
        world.dispatcher.add(RoundController.TURN_ENDED, new EventListener<Creature>() {
            @Override public void handle(EventType<Creature> type, Creature abilities) {
                turnSelection.remove();
            }
        });
        world.dispatcher.add(PveLoadLevelController.LEVEL_LOADED, new EventListener<Void>() {
            @Override public void handle(EventType<Void> type, Void aVoid) {
                showTileBackgrounds();
            }
        });
        world.dispatcher.add(Creature.ADD_EFFECT, new EventListener<EffectEvent>() {
            @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                showEffect(event.creature, event.effect);
            }
        });
        world.dispatcher.add(Creature.REMOVE_EFFECT, new EventListener<EffectEvent>() {
            @Override public void handle(EventType<EffectEvent> type, EffectEvent event) {
                hideEffect(event.creature, event.effect);
            }
        });
        addBackgroundObjects();

        for (Map.Entry<Grid2D.Coordinate, String> entry : world.level.getElements(cover)) {
            Grid2D.Coordinate c = entry.getKey();
            Tile cover = new Tile("cover/" + entry.getValue() + ((c.x() + c.y()) % 2 == 1 ? "-odd" : "-even"));
            coverLayer.addActor(cover);
            cover.setPosition(c.x() * ViewController.CELL_SIZE, c.y() * ViewController.CELL_SIZE);
        }
//        for (Map.Entry<Grid2D.Coordinate, StepDetector> e : world.level.getElements(stepDetector)) {
//            Grid2D.Coordinate c = e.getKey();
//            final WorldObjectView view = createView(e.getValue());
//            stepDetectorLayer.addActor(view);
//            view.setPosition(c.x() * ViewController.CELL_SIZE, c.y() * ViewController.CELL_SIZE);
//        }

        scroller = new ViewScroller(this);

        world.dispatcher.add(RoundController.TURN_STARTED, new EventListener<Creature>() {
            @Override public void handle(EventType<Creature> type, Creature creature) {
                scroller.centerOn(creature);
            }
        });

        int minSpawnX = Integer.MAX_VALUE;
        int maxSpawnX = Integer.MIN_VALUE;
        int minSpawnY = Integer.MAX_VALUE;
        int maxSpawnY = Integer.MIN_VALUE;
        for (Map.Entry<Grid2D.Coordinate, Fraction> entry : world.level.getElements(spawn)) {
            Grid2D.Coordinate coordinate = entry.getKey();
            minSpawnX = Math.min(minSpawnX, coordinate.x());
            maxSpawnX = Math.max(maxSpawnX, coordinate.x());
            minSpawnY = Math.min(minSpawnY, coordinate.y());
            maxSpawnY = Math.max(maxSpawnY, coordinate.y());
        }
        scroller.centerOnImmediately((minSpawnX + maxSpawnX) * 0.5f, (minSpawnY + maxSpawnY) * 0.5f);
    }

    public static void blink(final Actor image, final float from, final float to) {
        image.addAction(Actions.sequence(
            Actions.alpha(to, 0.5f),
            Actions.alpha(from, 0.5f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    blink(image, from, to);
                }
            })
        ));
    }

    private final Array<Drawable> backgroundDrawables = new Array<Drawable>();

    private void addBackgroundObjects() {
        for (String drawableName : world.level.backgroundObjects) {
            backgroundDrawables.add(Config.skin.getDrawable(drawableName));
        }
        for (int i = 0; i < MathUtils.random(7, 8); i++) {
            addBackgroundObject(true);
        }
    }

    private void addBackgroundObject(boolean anywhere) {
        final Image image = new Image(backgroundDrawables.random());
        backgroundLayer.addActor(image);


        float scale = 0.5f * MathUtils.random(1, 2);
        image.setSize(image.getPrefWidth() * scale, image.getPrefHeight() * scale);
        image.setRotation(MathUtils.random(0, 1) * 180);
        image.getColor().a = MathUtils.random(0.1f, 0.3f);

        float w = Math.max(world.stage.getWidth(), root.getWidth() + ViewScroller.LEFT + ViewScroller.RIGHT);
        float h = Math.max(world.stage.getHeight(), root.getHeight() + ViewScroller.TOP + ViewScroller.BOTTOM);

        if (anywhere)
            image.setPosition(-root.getX() + w * MathUtils.random(), -root.getY() + h * MathUtils.random());
        else
            image.setPosition(-root.getX() - image.getWidth(), -root.getY() + h * MathUtils.random());

        image.addAction(Actions.sequence(
            Actions.moveBy(w + image.getWidth() - image.getX(), 0, 15 + MathUtils.random(6)),
            Actions.run(new Runnable() {
                @Override public void run() {
                    image.remove();
                    addBackgroundObject(false);
                }
            })
        ));
    }

    private void showTileBackgrounds() {
        ObjectSet<Grid2D.Coordinate> processed = new ObjectSet<Grid2D.Coordinate>();
        final ArrayList<Grid2D.Coordinate> list = new ArrayList<Grid2D.Coordinate>(world.level.getCoordinates(tile));
        Collections.sort(list, new Comparator<Grid2D.Coordinate>() {
            @Override public int compare(Grid2D.Coordinate o1, Grid2D.Coordinate o2) {
                return o2.y() - o1.y();
            }
        });
        for (Grid2D.Coordinate coordinate : list) {
            showTileBackground(processed, coordinate.x() - 1, coordinate.y());
            showTileBackground(processed, coordinate.x() + 1, coordinate.y());
            showTileBackground(processed, coordinate.x(), coordinate.y() - 1);
            showTileBackground(processed, coordinate.x(), coordinate.y() + 1);
            showTileBackground(processed, coordinate.x() + 1, coordinate.y() + 1);
            showTileBackground(processed, coordinate.x() - 1, coordinate.y() + 1);
            showTileBackground(processed, coordinate.x() + 1, coordinate.y() - 1);
            showTileBackground(processed, coordinate.x() - 1, coordinate.y() - 1);
            showTransitions(coordinate.x(), coordinate.y());
        }
    }

    private void showTransitions(int x, int y) {
        String current = world.level.getElement(tile, x, y);
        String top = world.level.getElement(tile, x, y + 1);
        String bottom = world.level.getElement(tile, x, y - 1);
        String left = world.level.getElement(tile, x - 1, y);
        String right = world.level.getElement(tile, x + 1, y);
        if (top != null && !top.equals(current)) {
            if (top.equals(left)) {
                addTopLeftCornerTransition(top, x, y, overTileLayer);
            }
            if (top.equals(right)) {
                addTopRightCornerTransition(top, x, y, overTileLayer);
            }
        }
        if (bottom != null && !bottom.equals(current)) {
            if (bottom.equals(left)) {
                addBottomLeftCornerTransition(bottom, x, y, overTileLayer);
            }
            if (bottom.equals(right)) {
                addBottomRightCornerTransition(bottom, x, y, overTileLayer);
            }
        }
    }

    public static Image addTopLeftCornerTransition(String name, int x, int y, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("transitions/" + name + "-corner").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        layer.addActor(image);
        image.setPosition(x * CELL_SIZE, y * CELL_SIZE);
        return image;
    }

    public static Image addTopRightCornerTransition(String name, int x, int y, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("transitions/" + name + "-corner").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        layer.addActor(image);
        image.setRotation(-90);
        image.setPosition(x * CELL_SIZE, (y + 1) * CELL_SIZE);
        return image;
    }

    public static Image addBottomLeftCornerTransition(String name, int x, int y, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("transitions/" + name + "-corner").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        layer.addActor(image);
        image.setRotation(90);
        image.setPosition((x + 1) * CELL_SIZE, (y) * CELL_SIZE);
        return image;
    }

    public static Image addBottomRightCornerTransition(String name, int x, int y, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("transitions/" + name + "-corner").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        layer.addActor(image);
        image.setRotation(180);
        image.setPosition((x + 1) * CELL_SIZE, (y + 1) * CELL_SIZE);
        return image;
    }

    private void showTileBackground(ObjectSet<Grid2D.Coordinate> processed, int x, int y) {
        if (world.level.getElement(tile, x, y) != null)
            return;
        Grid2D.Coordinate c = new Grid2D.Coordinate(x, y);
        if (!processed.add(c))
            return;
        String top = world.level.getElement(tile, x, y + 1);
        String bottom = world.level.getElement(tile, x, y - 1);
        String left = world.level.getElement(tile, x - 1, y);
        String right = world.level.getElement(tile, x + 1, y);

        String topLeft = world.level.getElement(tile, x - 1, y + 1);
        String topRight = world.level.getElement(tile, x + 1, y + 1);
        String bottomLeft = world.level.getElement(tile, x - 1, y - 1);
        String bottomRight = world.level.getElement(tile, x + 1, y - 1);
        if (topLeft != null && top == null && left == null) {
            //use opposite
            addBottomRightCorner(x, y, topLeft, tileBackgroundLayer);
        }
        if (topRight != null && top == null && right == null) {
            // tile is bottom right -> corner is opposite of tile
            addBottomLeftCorner(x, y, topRight, tileBackgroundLayer);
        }

        if (top != null) {
            boolean hasLeft = world.level.getElement(tile, x - 1, y + 1) != null;
            boolean hasRight = world.level.getElement(tile, x + 1, y + 1) != null;
            String type;
            if (hasLeft && hasRight) {
                type = "both";
            } else if (hasLeft) {
                type = "left";
            } else if (hasRight) {
                type = "right";
            } else {
                type = "none";
            }
            addOverHang(x, y, top, type, tileBackgroundLayer);
            addTopOutline(x, y, top, tileBackgroundLayer);
        }
        if (bottom != null) {
            addBottomOutline(x, y, bottom, tileBackgroundLayer);
        }
        if (left != null) {
            addLeftOutline(x, y, left, tileBackgroundLayer);
        }
        if (right != null) {
            addRightOutline(x, y, right, tileBackgroundLayer);
        }
        if (bottomLeft != null && bottom == null && left == null) {
            addTopRightCorner(x, y, bottomLeft, tileBackgroundLayer);
        }
        if (bottomRight != null && bottom == null && right == null) {
            addTopLeftCorner(x, y, bottomRight, tileBackgroundLayer);
        }
    }

    public static Image addOverHang(int x, int y, String name, String type, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-overhang-" + type).random();
        if (region == null) {
            region = Config.findRegions("tile/" + name + "-overhang").random();
            if (region == null)
                return null;
        }
        Image image = new Image(region);
        layer.addActor(image);
        image.setPosition(x * CELL_SIZE, (y + 1) * CELL_SIZE - image.getHeight());
        return image;
    }

    public static Image addRightOutline(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-outline").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        layer.addActor(image);
        image.setPosition((x + 1) * CELL_SIZE - image.getWidth(), y * CELL_SIZE);
        return image;
    }

    public static Image addLeftOutline(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-outline").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        image.setRotation(180);
        layer.addActor(image);
        image.setPosition(x * CELL_SIZE + image.getWidth(), (y + 1) * CELL_SIZE);
        return image;
    }

    public static Image addBottomOutline(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-outline").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        image.setRotation(-90);
        layer.addActor(image);
        image.setPosition(x * CELL_SIZE, y * CELL_SIZE + image.getWidth());
        return image;
    }


    public static Image addTopOutline(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-outline").random();
        if (region == null)
            return null;
        Image image = new Image(region);
        image.setRotation(90);
        layer.addActor(image);
        image.setPosition((x + 1) * CELL_SIZE, (y + 1) * CELL_SIZE - image.getWidth());
        return image;
    }

    public static Tile addBottomRightCorner(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-corner-bottom-right").random();
        if (region == null)
            return null;
        Tile image = new Tile(region);
        layer.addActor(image);
        image.setPosition(x * CELL_SIZE, (y + 1) * CELL_SIZE - image.getHeight());
        return image;
    }

    public static Tile addBottomLeftCorner(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-corner-bottom-left").random();
        if (region == null)
            return null;
        Tile image = new Tile(region);
        layer.addActor(image);
        image.setPosition((x + 1) * CELL_SIZE - image.getWidth(), (y + 1) * CELL_SIZE - image.getHeight());
        return image;
    }

    public static Tile addTopRightCorner(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-corner-top-right").random();
        if (region == null)
            return null;
        Tile image = new Tile(region);
        layer.addActor(image);
        image.setPosition(x * CELL_SIZE, y * CELL_SIZE);
        return image;
    }

    public static Tile addTopLeftCorner(int x, int y, String name, Group layer) {
        TextureAtlas.AtlasRegion region = Config.findRegions("tile/" + name + "-corner-top-left").random();
        if (region == null)
            return null;
        Tile image = new Tile(region);
        layer.addActor(image);
        image.setPosition((x + 1) * CELL_SIZE - image.getWidth(), y * CELL_SIZE);
        return image;
    }


    @Override protected void start() {
    }

    @Override protected void stop() {
        root.remove();
        scroller.dispose();
    }

    private void initLayer(Group layer) {
        layer.setSize(world.width * CELL_SIZE, world.height * CELL_SIZE);
        layer.setTransform(false);
    }

    private Tile addImageToLayer(Group layer, float imageX, float imageY, String imageName) {
//        Image image = new Image(Config.skin, imageName);
//        image.setPosition(imageX, imageY);
        Tile tile = new Tile(imageName);
        tile.setPosition(imageX, imageY);
        layer.addActor(tile);
        return tile;
    }

    public WorldObjectView addView(WorldObject worldObject) {
        WorldObjectView view = removedViews.remove(worldObject);
        if (view == null) {
            view = createView(world.viewer, world.playerColors, worldObject);
            if (worldObject instanceof Creature) {
                view.setOffsetY(2);
            }
        } else {
            view.getColor().a = 1;
        }
        view.setPosition(worldObject.getX() * CELL_SIZE, worldObject.getY() * CELL_SIZE);
        view.selectLayer(this).addActor(view);
        objectViewMap.put(worldObject, view);
        viewObjectMap.put(view, worldObject);
        resortNeeded = true;
        return view;
    }

    private void moveView(final WorldObject object) {
        WorldObjectView view = getView(object);
        view.setPosition(object.getX() * CELL_SIZE, object.getY() * CELL_SIZE);
        if (!(object instanceof Creature))
            return;
        Creature creature = (Creature) object;
        if (creature == turnObject) {
            turnSelection.setPosition(object.getX() * CELL_SIZE, object.getY() * CELL_SIZE);
        }
        resortNeeded = true;
    }


    public void removeView(WorldObject object) {
        WorldObjectView view = objectViewMap.remove(object);
        viewObjectMap.remove(view);
        view.remove();
        removedViews.put(object, view);
    }

    public static WorldObjectView createView(Player viewer, PlayerColors colors, WorldObject object) {
        WorldObjectView view = new WorldObjectView();
        ArrayMap<Object, SubView> subViewArrayMap = object.createSubViews(viewer, colors);
        for (Object key : subViewArrayMap.keys()) {
            view.addSubView(key, subViewArrayMap.get(key));
        }
        object.initView(view);
        return view;
    }

    public static WorldObjectView createView(WorldObject object) {
        return createView(PlayerHelper.defaultProtagonist, PlayerHelper.defaultColors, object);
    }

    public static void updateView(Player viewer, PlayerColors colors, WorldObject object, WorldObjectView view) {
        view.clearChildren();
        ArrayMap<Object, SubView> subViewArrayMap = object.createSubViews(viewer, colors);
        for (Object key : subViewArrayMap.keys()) {
            view.addSubView(key, subViewArrayMap.get(key));
        }
    }

    public static void updateView(WorldObject object, WorldObjectView view) {
        updateView(PlayerHelper.defaultProtagonist, PlayerHelper.defaultColors, object, view);
    }

    public WorldObjectView getView(WorldObject worldObject) {
        return objectViewMap.get(worldObject);
    }

    public IFuture<Void> visualize(Object something) {
        return visualizer.visualize(something);
    }

    public boolean canVisualize(Object something) {
        return visualizer.canVisualize(something);
    }

    public Vector2 stageToWorldCoordinates(float x, float y) {
        return stageToWorldCoordinates(new Vector2(x, y));
    }


    public Vector2 worldToStageCoordinates(Vector2 worldCoordinates) {
        return root.localToStageCoordinates(worldCoordinates.scl(ViewController.CELL_SIZE));
    }

    public Vector2 stageToWorldCoordinates(Vector2 input) {
        root.stageToLocalCoordinates(input);
        input.scl(1 / ViewController.CELL_SIZE);
        int x = MathUtils.floor(input.x);
        int y = MathUtils.floor(input.y);
        if (input.x < 0) {
            x = -Math.abs(x);
        }
        if (input.y < 0) {
            y = -Math.abs(y);
        }
        input.set(x, y);
        return input;
    }

    public void resort() {
        objectLayer.getChildren().sort(comparator);
        resortNeeded = false;
    }

    public void cancelResort() {
        resortNeeded = false;
    }

    public void showSelection(Object key, Array<Grid2D.Coordinate> coordinates, String selectionName) {
        if (selectionMap.containsKey(key))
            throw new IllegalStateException("there is already associated selection with key " + key);
        Drawable drawable = Config.skin.getDrawable(selectionName);
        Array<Image> selections = new Array<Image>();
        for (Grid2D.Coordinate coordinate : coordinates) {
            Image image = new Image(drawable);
            blink(image, 0.2f, 1f);
            image.setPosition(coordinate.x() * CELL_SIZE, coordinate.y() * ViewController.CELL_SIZE);
            selectionLayer.addActor(image);
            selections.add(image);
        }
        selectionMap.put(key, selections);
    }

    public boolean hasSelection(Object key) {
        return selectionMap.containsKey(key);
    }

    public void removeSelection(Object key) {
        Array<Image> images = selectionMap.remove(key);
        if (images == null)
            throw new IllegalStateException("there is no selection associated for key " + key);
        for (Image image : images) {
            image.remove();
        }
    }

    public Rectangle[] getSpawnTargets() {
        Set<Map.Entry<Grid2D.Coordinate, Fraction>> spawnPoints = world.level.getElements(spawn);
        Rectangle[] result = new Rectangle[spawnPoints.size()];
        int i = 0;
        for (Map.Entry<Grid2D.Coordinate, Fraction> entry : spawnPoints) {
            Grid2D.Coordinate coordinate = entry.getKey();
            Vector2 screenPos = spawnLayer.localToStageCoordinates(tmp.set(coordinate.x() * CELL_SIZE, coordinate.y() * CELL_SIZE));
            result[i] = new Rectangle(screenPos.x, screenPos.y, CELL_SIZE, CELL_SIZE);
            i++;
        }
        return result;
    }

    public Rectangle getScreenRectangle(int worldX, int worldY) {
        Vector2 screenPos = spawnLayer.localToStageCoordinates(tmp.set(worldX * CELL_SIZE, worldY * CELL_SIZE));
        return new Rectangle(screenPos.x, screenPos.y, CELL_SIZE, CELL_SIZE);
    }

    public WorldObjectView findCreatureViewByName(String dieName) {
        dieName = dieName.toLowerCase();
        for (WorldObject object : world) {
            if (object instanceof Creature) {
                Creature creature = (Creature) object;
                if (creature.description.name.toLowerCase().equals(dieName))
                    return getView(creature);
            }
        }
        return null;
    }

    // -------------- effects -------------- //

    private void showEffect(Creature creature, CreatureEffect effect) {
        EffectsSubView subView = (EffectsSubView) getView(creature).getSubView(CreatureEffect.class);
        subView.addEffect(effect);
    }

    private void hideEffect(Creature creature, CreatureEffect effect) {
        EffectsSubView subView = (EffectsSubView) getView(creature).getSubView(CreatureEffect.class);
        subView.removeEffect(effect);
    }
}
