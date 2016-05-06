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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.vlaaad.common.gdx.State;

/**
 * Created 07.12.13 by vlaaad
 */
public class IntroState extends State {

    private final Callback callback;
    private Texture textureImage;
    private Texture textureText;

    public static interface Callback {
        public void onEnded();
    }

    public IntroState(Callback callback) {
        this.callback = callback;
    }

    @Override protected void init() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        textureImage = new Texture("logo-image.png");
        textureText = new Texture("logo-text.png");

        final Image image = new Image(textureImage);
        image.setPosition(stage.getWidth() / 2 - image.getWidth() / 2, stage.getHeight() / 2 - image.getHeight() / 2);

        final Image text = new Image(textureText);
        text.setPosition(image.getX(), image.getY());

        stage.addActor(image);
        stage.addActor(text);
        image.getColor().a = 0;
        text.getColor().a = 0;

        image.addAction(Actions.sequence(
            Actions.alpha(1, 0.3f),
            Actions.run(new Runnable() {
                @Override public void run() {
                    text.addAction(
                        Actions.sequence(
                            Actions.alpha(1, 0.3f),
                            Actions.delay(1.5f),
                            Actions.run(new Runnable() {
                                @Override public void run() {
                                    image.addAction(Actions.alpha(0, 0.3f));
                                    text.addAction(Actions.sequence(
                                        Actions.alpha(0, 0.3f),
                                        Actions.run(new Runnable() {
                                            @Override public void run() {
                                                callback.onEnded();
                                            }
                                        })
                                    ));
                                }
                            })
                        )
                    );
                }
            })
        ));
    }

    @Override protected void resume(boolean isStateChange) {
    }

    @Override protected void pause(boolean isStateChange, Stage stage) {
    }

    @Override protected void dispose(boolean isStateChange, Stage stage) {
        textureImage.dispose();
        textureText.dispose();
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                IntroState.this.stage.dispose();
            }
        });
    }

    @Override protected boolean disposeOnSwitch() {
        return true;
    }

    @Override protected Color getBackgroundColor() {
        return Color.WHITE;
    }
}
