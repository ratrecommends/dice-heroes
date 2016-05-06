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

package com.vlaaad.common.gdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * Created 14.06.13 by vlaaad
 */
public abstract class App implements ApplicationListener {
    private float scale = 1;
    private State state;
    private final SnapshotArray<AppListener> listeners = new SnapshotArray<AppListener>(AppListener.class);

    protected App(float scale) {
        this.scale = scale;
    }

    public void setScale(float value) {
        scale = value;
        if (state != null) {
            state.doResize(scale);
        }
    }

    @Override
    public void dispose() {
        if (state != null) state.doDispose(false);
    }

    @Override
    public void pause() {
        if (state != null) state.doPause(false, null);
    }

    @Override
    public void resume() {
        if (state != null) state.doResume(false, scale);
    }

    @Override
    public void render() {
        if (state != null) state.doRender(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        if (state == null)
            return;
        state.doResize(scale);
    }

    /**
     * Sets the current state. {@link State#pause(boolean, com.badlogic.gdx.scenes.scene2d.Stage)} is called on any old state, and {@link State#resume(boolean)} is called on the new
     * state, if any.
     */
    public void setState(State state) {
        if (this.state == state)
            return;
//        Logger.debug("set app state to " + state, new RuntimeException());
        if (this.state != null) {
            AppListener[] items = listeners.begin();
            for (int i = 0, n = listeners.size; i < n; i++) {
                items[i].exitState(this.state);
            }
            listeners.end();
            this.state.doPause(true, state.stage);
            if (this.state.disposeOnSwitch()) {
                this.state.dispose(true, state.stage);
            }
        }
        this.state = state;
        if (this.state != null) {
            this.state.doResume(true, scale);
            AppListener[] items = listeners.begin();
            for (int i = 0, n = listeners.size; i < n; i++) {
                items[i].enterState(this.state);
            }
            listeners.end();
        }
    }

    /**
     * @return the currently active {@link State}.
     */
    public State getState() {
        return state;
    }

    public void addListener(AppListener listener) {
        if (!listeners.contains(listener, true))
            listeners.add(listener);
    }

    public void removeListener(AppListener listener) {
        listeners.removeValue(listener, true);
    }

    public static interface AppListener {

        public void enterState(State state);

        public void exitState(State state);

        public static class Adapter implements AppListener {
            @Override public void enterState(State state) {}
            @Override public void exitState(State state) {}
        }
    }
}
