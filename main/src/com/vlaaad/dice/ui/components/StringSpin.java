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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.ThesaurusData;

/**
 * Created 27.09.14 by vlaaad
 */
public class StringSpin extends Group {
    private final String letters;
    private final float lineHeight;
    private final Array<Label> labels;

    public StringSpin(int length, int letterWidth, String initialText) {
        ThesaurusData data = Config.thesaurus.getData("alphabet");
        labels = new Array<Label>(length);
        letters = String.valueOf(shuffleArray((data.en + " " + data.ru).toCharArray())).toUpperCase();
        Array<String> arr = new Array<String>(letters.length());
        arr.addAll(letters.split(""));

        String newLined = arr.toString("\n");
        String doubled = newLined + "\n" + newLined;
        Label.LabelStyle style = Config.skin.get("default", Label.LabelStyle.class);

        lineHeight = style.font.getLineHeight();

        for (int i = 0; i < length; i++) {
            addColumn(i, letterWidth, doubled);
        }
        setSize(letterWidth * length, lineHeight);
        setTextImmediately(initialText);
    }

    public void setTextImmediately(String text) {
        for (int i = 0; i < labels.size; i++) {
            String letter = i < text.length() ? String.valueOf(text.charAt(i)).toUpperCase() : " ";
            int idx = letters.indexOf(letter.toUpperCase());
            if (idx == -1) idx = letters.indexOf(" ");
            if (idx == -1) throw new IllegalStateException();
            Label label = labels.get(i);
            label.clearActions();
            label.setY(-(letters.length() - idx - 1) * lineHeight);
        }
    }

    public void setText(String text, Runnable callback) {
        setText(text, 200f, 1f, 0.5f, callback);
    }

    private void setText(String text, final float speed, final float spinTime, float endTime, Runnable callback) {
        for (int i = 0; i < labels.size; i++) {
            String letter = i < text.length() ? String.valueOf(text.charAt(i)).toUpperCase() : " ";
            int idx = letters.indexOf(letter.toUpperCase());
            if (idx == -1) idx = letters.indexOf(" ");
            if (idx == -1) throw new IllegalStateException();
            final Label label = labels.get(i);
            label.clearActions();
            addActions(label, i, idx, speed, spinTime, endTime, callback);
        }
    }
    private void addActions(final Label label, final int i, final int idx, final float speed, final float spinTime, final float endTime, final Runnable callback) {
        label.addAction(new Action() {
            private float totalTime = spinTime + i * endTime;
            @Override public boolean act(float delta) {
                totalTime -= delta;
                label.moveBy(0, -speed * delta);
                boolean finished = totalTime <= 0;
                if (finished) {
                    label.setY(-(letters.length() - idx - 1) * lineHeight);
                    if (i == labels.size - 1) {
                        callback.run();
                    }
                } else {
                    while (label.getY() < -letters.length() * lineHeight) {
                        label.setY(label.getY() + letters.length() * lineHeight);
                    }
                }
                return finished;
            }
        });
    }

    @Override public Actor hit(float x, float y, boolean touchable) {
        if (touchable && this.getTouchable() != Touchable.enabled) return null;
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight() ? this : null;
    }
    private void addColumn(int index, int width, String text) {
        Label label = new Label(text, Config.skin);
        label.setAlignment(Align.center);
        label.setWidth(width);
        label.setX(width * index);

        addActor(label);
        labels.add(label);
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        batch.flush();
        if (clipBegin(getX(), getY() + 2, getWidth(), getHeight())) {
            super.draw(batch, parentAlpha);
            batch.flush();
            clipEnd();
        }
    }
    static char[] shuffleArray(char[] ar) {
        for (int i = ar.length - 1; i > 0; i--) {
            int index = MathUtils.random(i);
            char a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }
}
