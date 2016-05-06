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

package com.vlaaad.dice.ui.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.Localizable;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 06.11.13 by vlaaad
 */
public class LocTextButton extends TextButton implements Localizable {
    private boolean useDisabledOffset;
    private int disabledOffsetX;
    private int disabledOffsetY;
    private String key;

    public LocTextButton(String key, String styleName) {
        super(key, Config.skin, styleName);
        this.key = key;
        SoundHelper.initButton(this);
    }

    public LocTextButton(String key) {
        this(key, "default");
    }

    public LocTextButton(String key, String styleName, int disabledOffsetX, int disabledOffsetY) {
        this(key, styleName);
        this.disabledOffsetX = disabledOffsetX;
        this.disabledOffsetY = disabledOffsetY;
        useDisabledOffset = true;
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        Color fontColor;
        TextButtonStyle style = getStyle();
        if (isDisabled() && style.disabledFontColor != null)
            fontColor = style.disabledFontColor;
        else if (isPressed() && style.downFontColor != null)
            fontColor = style.downFontColor;
        else if (isChecked() && style.checkedFontColor != null)
            fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor : style.checkedFontColor;
        else if (isOver() && style.overFontColor != null)
            fontColor = style.overFontColor;
        else
            fontColor = style.fontColor;
        if (fontColor != null) getLabel().getStyle().fontColor = fontColor;

        Drawable background = null;
        float offsetX = 0, offsetY = 0;
        if (isPressed() && !isDisabled()) {
            background = style.down == null ? style.up : style.down;
            offsetX = style.pressedOffsetX;
            offsetY = style.pressedOffsetY;
        } else {
            if (isDisabled() && style.disabled != null)
                background = style.disabled;
            else if (isChecked() && style.checked != null)
                background = (isOver() && style.checkedOver != null) ? style.checkedOver : style.checked;
            else if (isOver() && style.over != null)
                background = style.over;
            else
                background = style.up;
            if (useDisabledOffset && isDisabled()) {
                offsetX = disabledOffsetX;
                offsetY = disabledOffsetY;
            } else {
                offsetX = style.unpressedOffsetX;
                offsetY = style.unpressedOffsetY;
            }
        }
        setBackground(background);

//        if (background != null) {
//            Color color = getColor();
//            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
//            background.draw(batch, getX(), getY(), getWidth(), getHeight());
//        }

        Array<Actor> children = getChildren();
        for (int i = 0; i < children.size; i++)
            children.get(i).moveBy(offsetX, offsetY);

        if (isTransform()) {
            applyTransform(batch, computeTransform());
            drawBackground(batch, parentAlpha, 0, 0);
        } else {
            drawBackground(batch, parentAlpha, getX(), getY());
        }
        drawChildren(batch, parentAlpha);
        if (isTransform()) resetTransform(batch);

        for (int i = 0; i < children.size; i++)
            children.get(i).moveBy(-offsetX, -offsetY);
    }

    @Override protected void setStage(Stage stage) {
        if (stage == null && getStage() != null) {
            //unregister
            Config.thesaurus.unregister(this);
        } else if (stage != null && getStage() == null) {
            //register
            Config.thesaurus.register(this, key);
        }
        super.setStage(stage);
    }

    public void setKey(String value) {
        if (this.key.equals(value))
            return;
        key = value;
        if (getStage() != null) {
            Config.thesaurus.register(this, key);
        }
    }

    @Override public void localize(String localizedString) {
        getLabel().setText(localizedString);
    }
}
