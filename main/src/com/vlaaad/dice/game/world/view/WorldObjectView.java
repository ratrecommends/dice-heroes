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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.ArrayMap;
import com.vlaaad.common.util.Function;
import com.vlaaad.dice.game.world.controllers.ViewController;

import java.util.Comparator;

/**
 * Created 07.10.13 by vlaaad
 */
public class WorldObjectView extends Group {
    private static final Function<ViewController, Group> OBJECT_LAYER = new Function<ViewController, Group>() {
        @Override public Group apply(ViewController viewController) {
            return viewController.objectLayer;
        }
    };

    private final Comparator<Actor> comparator = new Comparator<Actor>() {
        @Override public int compare(Actor o1, Actor o2) {
            SubView s1 = actorMap.get(o1);
            SubView s2 = actorMap.get(o2);
            return s2.getPriority() - s1.getPriority();
        }
    };
    private float offsetY;
    private float offsetX;
    public Function<ViewController, Group> layerSelector = OBJECT_LAYER;

    public WorldObjectView() {
        setSize(ViewController.CELL_SIZE, ViewController.CELL_SIZE);
    }

    @Override public float getHeight() {
        return ViewController.CELL_SIZE;
    }

    @Override public float getWidth() {
        return ViewController.CELL_SIZE;
    }

    private String animationName = "idle";

    private final ArrayMap<Object, SubView> map = new ArrayMap<Object, SubView>();
    private final ArrayMap<Actor, SubView> actorMap = new ArrayMap<Actor, SubView>();

    public void addSubView(SubView subView) {
        addSubView(subView, subView);
    }

    public void addSubView(Object key, SubView view) {
        map.put(key, view);
        addActor(view.getActor());
        actorMap.put(view.getActor(), view);
        view.play(animationName);
        getChildren().sort(comparator);
    }

    public void play(String animationName) {
        if (animationName == null)
            throw new IllegalArgumentException("animation name can't be null");
        this.animationName = animationName;
        for (SubView subView : map.values()) {
            subView.play(animationName);
        }
    }

    @Override public void clearChildren() {
        super.clearChildren();
        map.clear();
        actorMap.clear();
    }

    public void removeSubView(Object key) {
        SubView value = map.get(key);
        if (value == null)
            return;
        removeSubView(key, value);
    }

    public void removeSubView(SubView subView) {
        removeSubView(subView, subView);
    }

    private void removeSubView(Object key, SubView view) {
        map.removeKey(key);
        view.getActor().remove();
        actorMap.removeKey(view.getActor());
    }

    @Override public void setX(float x) {
        super.setX(x + offsetX);
    }

    @Override public void setY(float y) {
        super.setY(y + offsetY);
    }

    @Override public void setPosition(float x, float y) {
        super.setPosition(x + offsetX, y + offsetY);
    }

    public SubView getSubView(Object key) {
        return map.get(key);
    }

    public void setOffsetX(float offsetX) {
        if (offsetX == this.offsetX)
            return;
        float prevOffsetX = this.offsetX;
        this.offsetX = offsetX;
        setX(getX() - prevOffsetX);
    }

    public void setOffsetY(float offsetY) {
        if (offsetY == this.offsetY)
            return;
        float prevOffsetY = this.offsetY;
        this.offsetY = offsetY;
        setY(getY() - prevOffsetY);
    }

    public Iterable<? extends SubView> subViews() {
        return map.values();
    }

    public Group selectLayer(ViewController viewController) {
        return layerSelector.apply(viewController);
    }
}
