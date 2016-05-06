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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.vlaaad.common.gdx.scene2d.events.ResizeEvent;
import com.vlaaad.common.ui.WindowManager;

/**
 * Created 14.06.13 by vlaaad
 */
public abstract class State {
    private boolean initialized = false;
    private boolean shown = false;
    public final ScreenViewport viewport = new ScreenViewport();
    public final Stage stage = new Stage(viewport);

    private final EventListener inputListener = new InputListener() {
        @Override
        public boolean keyUp(InputEvent event, int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                if (!WindowManager.instance.handleBackPressed()) {
                    onBackPressed();
                }
            } else if (keycode == Input.Keys.MENU) {
                onMenuPressed();
            }
            return super.keyUp(event, keycode);
        }
    };

    final void doResume(boolean isStateChange, float scale) {
        if (!initialized) {
            initialized = true;
            doResize(scale);
            init();
        }
        if (!shown) {
            shown = true;
            Gdx.input.setInputProcessor(stage);
            WindowManager.instance.setActiveStage(stage);
            stage.addListener(inputListener);
            resume(isStateChange);
        }
    }

    final void doResize(float scale) {
        viewport.setUnitsPerPixel(1 / scale);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        stage.getRoot().fire(new ResizeEvent());
    }

    protected void onMenuPressed() {
    }

    protected void onBackPressed() {
        Gdx.app.exit();
    }

    final void doRender(float delta) {
        Color color = getBackgroundColor();
        Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        render(delta);
    }

    final void doPause(boolean isStateChange, Stage stage) {
        if (shown) {
            shown = false;
            pause(isStateChange, stage);
        }
    }

    final void doDispose(boolean isStateChange) {
        if (!initialized)
            return;
        if (shown) {
            shown = false;
            pause(isStateChange, null);
        }

        dispose(isStateChange, null);
    }

    /**
     * Called before first resume
     */
    protected abstract void init();

    /**
     * @param isStateChange - indicator of if it was system resume or because of state changed
     * @see com.badlogic.gdx.ApplicationListener#resume()
     */
    protected abstract void resume(boolean isStateChange);

    /**
     * Called when the state should render itself.
     *
     * @param delta The time in seconds since the last render.
     */
    protected void render(float delta) {
    }

    /**
     * @param isStateChange - indicator of if it was system pause or because of state changed
     * @param stage
     * @see com.badlogic.gdx.ApplicationListener#pause()
     */
    protected abstract void pause(boolean isStateChange, Stage stage);

    /**
     * Called when this state is no longer the current state for a {@link com.vlaaad.common.gdx.App}.
     *
     * @param isStateChange - indicator of if it was system dispose or because of state changed
     * @param stage         - optional stage of new state (if switching to new state)
     */
    protected abstract void dispose(boolean isStateChange, Stage stage);

    protected boolean disposeOnSwitch() {
        return false;
    }

    protected Color getBackgroundColor() {
        return Color.GRAY;
    }

}
