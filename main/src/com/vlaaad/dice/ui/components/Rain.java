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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.vlaaad.dice.Config;

/**
 * Created 04.01.14 by vlaaad
 */
public class Rain extends Widget {

    private final RainStyle style;
    private final Array<Row> rows = new Array<Row>(1);

    public Rain() {
        this(new RainStyle());
    }

    public Rain(RainStyle style) {
        this.style = style;
        initialize();
    }

    private void initialize() {
        if (style.color == null) style.color = new Color(1, 1, 1, 0.05f);
    }

    @Override public float getPrefWidth() {
        return style.pad * 2 + style.dropWidth;
    }

    @Override public float getPrefHeight() {
        return style.maxDropDistance + style.maxDropHeight;
    }

    @Override public void act(float delta) {
        super.act(delta);
        float dy = delta * style.speed;
        for (Row row : rows) {
            row.y -= dy;
            updateRow(row);
        }
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        validate();
        batch.end();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        ShapeRenderer renderer = Config.shapeRenderer;
        renderer.setProjectionMatrix(batch.getProjectionMatrix());
        renderer.setTransformMatrix(batch.getTransformMatrix());

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(style.color.r, style.color.g, style.color.b, style.color.a * parentAlpha);
        float usedWidth = getWidth() - style.pad;
        int count = (int) (usedWidth / (style.dropWidth + style.pad));
        if (count == 0)
            return;
        float step = usedWidth / ((float) count);
        float x = style.pad;
        for (int i = 0, n = rows.size; i < n; i++) {
            Row row = rows.get(i);
            drawRow(x, row);
            x += step;
        }
        renderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
    }

    private void drawRow(float x, Row row) {
        float y = row.y;
        Drop drop = row.root;
        while (drop != null) {
            if (y + drop.height < getY()) {
                y += drop.totalHeight();
                drop = drop.next;
                continue;
            }
            drawDrop(x, y, drop);
            y += drop.totalHeight();
            drop = drop.next;
        }
    }

    private void drawDrop(float x, float y, Drop drop) {
        float minY = MathUtils.clamp(y, getY(), getY() + getHeight());
        float maxY = MathUtils.clamp(y + drop.height, minY, getY() + getHeight());
        Config.shapeRenderer.rect(x, minY, style.dropWidth, maxY - minY);
    }

    @Override public void layout() {
        float usedWidth = getWidth() - style.pad;
        int count = (int) (usedWidth / (style.dropWidth + style.pad));
        if (count == rows.size)
            return;
        if (count > rows.size) {
            //add rows
            for (int i = 0, n = count - rows.size; i < n; i++) {
                addRow();
            }
        } else {
            for (int i = 0, n = rows.size - count; i < n; i++) {
                removeRow();
            }
        }

    }

    private void removeRow() {
        Row row = rows.pop();
        Drop drop = row.root;
        while (drop != null) {
            Drop next = drop.next;
            dropPool.free(drop);
            drop = next;
        }
        rowPool.free(row);
    }

    private void addRow() {
        Row row = rowPool.obtain();
        Drop root = dropPool.obtain();
        initDrop(root);
        row.root = root;
        row.y = -MathUtils.random(root.totalHeight());
        rows.add(row);
        updateRow(row);
    }

    private void updateRow(Row row) {
        //remove too low drops
        while (-row.y > row.root.totalHeight() && row.root.next != null) {
            row.y += row.root.totalHeight();
            Drop prevRoot = row.root;
            row.root = prevRoot.next;
            dropPool.free(prevRoot);
        }
        //add new drops
        float y = row.y;
        Drop drop = row.root;
        y += drop.totalHeight();
        while (y < getHeight()) {
            if (drop.next == null) {
                Drop next = dropPool.obtain();
                initDrop(next);
                drop.next = next;
            }
            drop = drop.next;
            y += drop.totalHeight();
        }
    }

    private void initDrop(Drop drop) {
        drop.height = MathUtils.random(style.minDropHeight, style.maxDropHeight);
        drop.distanceToNext = MathUtils.random(style.minDropDistance, style.maxDropDistance);
    }

    public static class RainStyle {
        public Color color;
        public float minDropHeight = 30;
        public float maxDropHeight = 70;
        public float minDropDistance = 30;
        public float maxDropDistance = 70;
        public float dropWidth = 5;
        public float pad = 5;
        public float speed = 100;

        public RainStyle() {
        }
    }

    private static final Pool<Row> rowPool = new Pool<Row>(16, 32) {
        @Override protected Row newObject() {
            return new Row();
        }
    };

    private static final Pool<Drop> dropPool = new Pool<Drop>(16, 32) {
        @Override protected Drop newObject() {
            return new Drop();
        }
    };

    private static class Row implements Pool.Poolable {
        public float y;
        public Drop root;

        @Override public void reset() {
            root = null;
        }
    }

    private static class Drop implements Pool.Poolable {
        public float height;
        public float distanceToNext;
        public Drop next;

        public float totalHeight() {
            return height + distanceToNext;
        }

        @Override public void reset() {
            next = null;
        }
    }
}
