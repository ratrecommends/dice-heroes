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

package com.vlaaad.common.gdx.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created 24.10.13 by vlaaad
 */
public class TabPane extends WidgetGroup {

    private final Array<Actor> headers = new Array<Actor>();
    private final Array<Actor> contents = new Array<Actor>();
    private final ObjectMap<Actor, EventListener> listeners = new ObjectMap<Actor, EventListener>();

    private int selectedIndex = -1;

    private TabPaneStyle style;
    private float contentHeight;
    private float headerWidth;
    private float headersHeight;
    private float headersBetweenOffset;
    private float headersLeftOffset;
    private float headersRightOffset;

    public TabPane() {
        this(new TabPaneStyle());
    }

    public TabPane(Skin skin, String styleName) {
        this(skin.get(styleName, TabPaneStyle.class));
    }

    public TabPane(Skin skin) {
        this(skin.get(TabPaneStyle.class));
    }

    public TabPane(TabPaneStyle style) {
        if (style == null)
            throw new IllegalArgumentException("style can't be null");
        this.style = style;
        setTransform(false);
    }

    public void addTab(Actor header, Actor content) {
        if (listeners.containsKey(header))
            throw new IllegalArgumentException("header already exists");
        headers.add(header);
        contents.add(content);
        super.addActor(header);
        if (selectedIndex == -1)
            setSelectedIndex(0);
        invalidate();

        final int index = headers.size - 1;
        EventListener listener = new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                setSelectedIndex(index);
                event.cancel();
            }
        };
        header.addListener(listener);
        listeners.put(header, listener);
    }

    public void setSelectedIndex(int i) {
        if (selectedIndex == i)
            return;
        if (i < 0 || i >= headers.size)
            throw new IndexOutOfBoundsException();
        if (selectedIndex != -1) {
            contents.get(selectedIndex).remove();
        }
        selectedIndex = i;
        super.addActor(contents.get(selectedIndex));
        invalidateHierarchy();
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        if (selectedIndex == -1)
            return;
        validate();
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        drawContentBackground(batch);
        drawLeftBorder(batch);
        drawRightBorder(batch);

        for (int i = 0; i < headers.size; i++) {
            if (i != headers.size - 1)
                drawGap(i, batch);
            Drawable headerBackground;
            if (selectedIndex == i) {
                headerBackground = style.active;
            } else {
                headerBackground = style.inactive;
            }
            if (headerBackground != null) {
                headerBackground.draw(
                    batch,
                    getX() + headerWidth * i + headersLeftOffset + i * headersBetweenOffset - 0.5f,
                    getY() + contentHeight,
                    headerWidth + 1f,
                    getHeight() - contentHeight
                );
            }
        }
        super.draw(batch, parentAlpha);
    }

    private void drawGap(int i, Batch batch) {
        Drawable headerGap;
        if (i == selectedIndex) {
            //current active
            headerGap = style.activeBorderRight;
        } else if (i == selectedIndex - 1) {
            //next active
            headerGap = style.activeBorderLeft;
        } else {
            headerGap = style.inactiveBorder;
        }
        if (headerGap != null) {
            headerGap.draw(
                batch,
                getX() + headerWidth * (i + 1) + headersLeftOffset + i * headersBetweenOffset,
                getY() + contentHeight,
                headersBetweenOffset,
                getHeight() - contentHeight
            );
        }
    }

    private void drawContentBackground(Batch batch) {
        if (style.content != null) {
            style.content.draw(batch, getX(), getY(), getWidth(), contentHeight);
        }
    }

    private void drawRightBorder(Batch batch) {
        Drawable rightBorder;
        if (selectedIndex == headers.size - 1) {
            rightBorder = style.activeBorderRightEdge;
        } else {
            rightBorder = style.inactiveBorderRightEdge;
        }

        if (rightBorder != null) {
            float x = getX() + getWidth() - headersRightOffset;
            float y = getY() + contentHeight;
            rightBorder.draw(batch, x, y, headersRightOffset, getHeight() - contentHeight);
        }
    }

    private void drawLeftBorder(Batch batch) {
        Drawable leftBorder;
        if (selectedIndex == 0) {
            leftBorder = style.activeBorderLeftEdge;
        } else {
            leftBorder = style.inactiveBorderLeftEdge;
        }
        if (leftBorder != null) {
            leftBorder.draw(batch, getX(), getY() + contentHeight, headersLeftOffset, getHeight() - contentHeight);
        }
    }

    @Override public void layout() {
        if (selectedIndex == -1)
            return;
        // left offset
        float bordersHeight = 0f;
        headersLeftOffset = 0f;
        if (selectedIndex == 0) {
            if (style.activeBorderLeftEdge != null) {
                headersLeftOffset = style.activeBorderLeftEdge.getMinWidth();
                bordersHeight = style.activeBorderLeftEdge.getMinHeight();
            }
        } else {
            if (style.inactiveBorderLeftEdge != null) {
                headersLeftOffset = style.inactiveBorderLeftEdge.getMinWidth();
                bordersHeight = Math.max(bordersHeight, style.inactiveBorderLeftEdge.getMinHeight());
            }
        }
        //right offset
        headersRightOffset = 0f;
        if (selectedIndex == headers.size - 1) {
            if (style.activeBorderRightEdge != null) {
                headersRightOffset = style.activeBorderRightEdge.getMinWidth();
                bordersHeight = Math.max(bordersHeight, style.activeBorderRightEdge.getMinHeight());
            }
        } else {
            if (style.inactiveBorderRightEdge != null) {
                headersRightOffset = style.inactiveBorderRightEdge.getMinWidth();
                bordersHeight = Math.max(bordersHeight, style.inactiveBorderRightEdge.getMinHeight());
            }
        }
        //size between headers
        headersBetweenOffset = 0f;
        if (style.inactiveBorder != null) {
            headersBetweenOffset = style.inactiveBorder.getMinWidth();
            bordersHeight = Math.max(bordersHeight, style.inactiveBorder.getMinHeight());
        }
        if (style.activeBorderLeft != null) {
            headersBetweenOffset = Math.max(style.activeBorderLeft.getMinWidth(), headersBetweenOffset);
            bordersHeight = Math.max(bordersHeight, style.activeBorderLeft.getMinHeight());
        }
        if (style.activeBorderRight != null) {
            headersBetweenOffset = Math.max(style.activeBorderRight.getMinWidth(), headersBetweenOffset);
            bordersHeight = Math.max(bordersHeight, style.activeBorderRight.getMinHeight());
        }
        headersHeight = bordersHeight;
        for (Actor header : headers) {
            headersHeight = Math.max(headersHeight, header instanceof Layout ? ((Layout) header).getPrefHeight() : header.getHeight());
        }
        headerWidth = (getWidth() - headersLeftOffset - headersRightOffset - headersBetweenOffset * (headers.size - 1)) / headers.size;
        int i = 0;
        contentHeight = getHeight() - headersHeight;
        for (Actor header : headers) {
            header.setWidth(headerWidth);
            header.setHeight(headersHeight);
            header.setPosition(i * headerWidth + headersLeftOffset + headersBetweenOffset * i, contentHeight);
            if (header instanceof Layout) {
                ((Layout) header).layout();
            }
            i++;
        }
        Actor content = contents.get(selectedIndex);
        content.setWidth(getWidth());
        content.setHeight(contentHeight);
        content.setPosition(0, 0);
        if (content instanceof Layout)
            ((Layout) content).layout();
    }

    @Override public float getPrefWidth() {
        if (selectedIndex == -1)
            return 0;
        validate();
        float headersWidth = 0;
        float maxWidth = 0f;
        for (Actor header : headers) {
            maxWidth = Math.max(header instanceof Layout ? ((Layout) header).getPrefWidth() : header.getWidth(), maxWidth);
        }
        headersWidth += maxWidth * headers.size;
        headersWidth += headersLeftOffset;
        headersWidth += headersRightOffset;
        headersWidth += headersBetweenOffset * (headers.size - 1);
        float contentsWidth = 0;
        for (Actor content : contents) {
            contentsWidth = Math.max(contentsWidth, content instanceof Layout ? ((Layout) content).getPrefWidth() : content.getWidth());
        }
        return Math.max(contentsWidth, headersWidth);
    }

    @Override public float getPrefHeight() {
        if (selectedIndex == -1)
            return 0;
//        validate();
        // left offset
        float headersHeight = 0f;
        if (selectedIndex == 0) {
            if (style.activeBorderLeftEdge != null) {
                headersHeight = style.activeBorderLeftEdge.getMinHeight();
            }
        } else {
            if (style.inactiveBorderLeftEdge != null) {
                headersHeight = Math.max(headersHeight, style.inactiveBorderLeftEdge.getMinHeight());
            }
        }
        //right offset
        if (selectedIndex == headers.size - 1) {
            if (style.activeBorderRightEdge != null) {
                headersHeight = Math.max(headersHeight, style.activeBorderRightEdge.getMinHeight());
            }
        } else {
            if (style.inactiveBorderRightEdge != null) {
                headersHeight = Math.max(headersHeight, style.inactiveBorderRightEdge.getMinHeight());
            }
        }
        //size between headers
        if (style.inactiveBorder != null) {
            headersHeight = Math.max(headersHeight, style.inactiveBorder.getMinHeight());
        }
        if (style.activeBorderLeft != null) {
            headersHeight = Math.max(headersHeight, style.activeBorderLeft.getMinHeight());
        }
        if (style.activeBorderRight != null) {
            headersHeight = Math.max(headersHeight, style.activeBorderRight.getMinHeight());
        }
        for (Actor header : headers) {
            headersHeight = Math.max(headersHeight, header instanceof Layout ? ((Layout) header).getPrefHeight() : header.getHeight());
        }

        float contentsHeight = 0;
        for (Actor content : contents) {
            contentsHeight = Math.max(contentsHeight, content instanceof Layout ? ((Layout) content).getPrefHeight() : content.getHeight());
        }
        return headersHeight + contentsHeight;
    }

    public void addActor(Actor actor) {
        throw new UnsupportedOperationException("Use TabPane#addTab.");
    }

    public void addActorAt(int index, Actor actor) {
        throw new UnsupportedOperationException("Use TabPane#addTab.");
    }

    public void addActorBefore(Actor actorBefore, Actor actor) {
        throw new UnsupportedOperationException("Use TabPane#addTab.");
    }

    @Override public void addActorAfter(Actor actorAfter, Actor actor) {
        throw new UnsupportedOperationException("Use TabPane#addTab.");
    }

    public TabPaneStyle getStyle() { return style; }

    public void setStyle(TabPaneStyle style) {
        if (style == null)
            throw new IllegalArgumentException("style can't be null");
        this.style = style;
    }

    public static class TabPaneStyle {
        public Drawable
            content,
            active,
            inactive,
            activeBorderLeft,
            activeBorderRight,
            activeBorderLeftEdge,
            activeBorderRightEdge,
            inactiveBorder,
            inactiveBorderLeftEdge,
            inactiveBorderRightEdge;
    }
}
