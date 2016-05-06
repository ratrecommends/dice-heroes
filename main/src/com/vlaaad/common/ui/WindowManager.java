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

package com.vlaaad.common.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

/**
 * Created 11.10.13 by vlaaad
 */
public class WindowManager {
    public static final WindowManager instance = new WindowManager();

    private final Array<GameWindow> windows = new Array<GameWindow>();
    private Stage activeStage;
    private Array<Block> toShow = new Array<Block>();

    private WindowManager() {
    }

    public void setActiveStage(Stage value) {
        activeStage = value;
        if (activeStage != null) {
            for (Block block : toShow) {
                block.show();
            }
            toShow.clear();
        }
    }

    void add(GameWindow window) {
        if (windows.contains(window, true))
            return;
        Group parent;
        if (window.getTargetParent() != null) {
            if (window.getTargetParent().getStage() == null)
                throw new IllegalStateException("no stage in target parent to show window!");
            parent = window.getTargetParent();
        } else {
            if (activeStage == null)
                throw new IllegalStateException("no active stage to show window!");
            parent = activeStage.getRoot();
        }
        windows.add(window);
        parent.addActor(window);
    }

    void remove(GameWindow window) {
        windows.removeValue(window, true);
    }

    public boolean handleBackPressed() {
        if (activeStage == null)
            throw new IllegalStateException("no stage to handle back pressed!");
        for (int i = windows.size - 1; i >= 0; i--) {
            GameWindow window = windows.get(i);
            if (window.getStage() != activeStage)
                continue;
            if (window.handleBackPressed())
                return true;
        }
        return false;
    }

    public boolean isShown(Class<? extends GameWindow> type) {
        for (GameWindow window : windows) {
            if (window.getStage() != activeStage)
                continue;
            if (window.getClass().equals(type))
                return true;
        }
        return false;
    }

    public Stage getActiveStage() {
        return activeStage;
    }

    public <I> void show(GameWindow<I> window, I params) {
        if (activeStage != null || (window.getTargetParent() != null && window.getTargetParent().getStage() != null))
            window.show(params);
        else
            toShow.add(new Block<I>(window, params));
    }

    @SuppressWarnings("unchecked")
    public <T extends GameWindow> T find(Class<T> windowClass) {
        for (GameWindow window : windows) {
            if (window.getStage() != activeStage)
                continue;
            if (windowClass.isInstance(window))
                return (T) window;
        }
        return null;
    }

    public GameWindow getCurrentWindow() {
        for (int i = windows.size - 1; i >= 0; i--) {
            GameWindow window = windows.get(i);
            if (window.getStage() != activeStage)
                continue;
            return window;
        }
        return null;
    }

    private class Block<I> {
        private final GameWindow<I> window;
        private final I params;

        public Block(GameWindow<I> window, I params) {
            this.window = window;
            this.params = params;
        }

        public void show() {
            window.show(params);
        }
    }
}
