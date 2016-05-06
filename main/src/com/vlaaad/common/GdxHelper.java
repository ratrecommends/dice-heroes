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

package com.vlaaad.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;

/**
 * Created 08.02.14 by vlaaad
 */
public class GdxHelper {
    public static void setBatchColor(Batch batch, Color color, float parentAlpha) {
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
    }

    private static final ObjectMap<Stage, EventListener> debugListeners = new ObjectMap<Stage, EventListener>();

    public static void showStageEvents(final Stage stage) {
        EventListener listener = new EventListener() {
            private final Vector2 tmp = new Vector2();
            private Actor actor = new Actor() {
                @Override public void draw(Batch batch, float parentAlpha) {
                    if (target == null)
                        return;
                    batch.end();
                    Config.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                    Config.shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
                    Gdx.gl.glLineWidth(6);
                    Config.shapeRenderer.setColor(Color.ORANGE);
                    Vector2 pos = target.localToStageCoordinates(tmp.set(0, 0));
                    float x = pos.x, y = pos.y;
                    Vector2 top = target.localToStageCoordinates(tmp.set(target.getWidth(), target.getHeight()));
                    float maxX = top.x, maxY = top.y;
                    Config.shapeRenderer.rect(x, y, maxX - x, maxY - y);

                    Config.shapeRenderer.end();
                    batch.begin();
                }
            };

            {
                stage.addActor(actor);
            }

            public Actor target;

            @Override public boolean handle(Event event) {
                target = event.getTarget();
                return false;
            }
        };
        stage.addListener(listener);
    }

    public static void hideStageEvents(Stage stage) {
        EventListener listener = debugListeners.remove(stage);
        if (listener != null)
            stage.removeListener(listener);
    }

}
