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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;

/**
 * Created 09.03.14 by vlaaad
 */
public class ItemCountIcon extends ItemIcon {
    private int count;
    private Label label = new Label("", Config.skin, "default", "inventory-counter");

    public ItemCountIcon(Item item, int count) {
        super(item);
        setCount(count);
        label.setSize(23, 24);
        label.setAlignment(Align.bottom | Align.right);
    }

    public void setCount(int count) {
        this.count = count;
        if (count != 0) {
            label.setText(String.valueOf(count));
        } else {
            label.setText(null);
        }
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        if (count == 0) {
            super.draw(batch, parentAlpha * 0.5f);
        } else {
            super.draw(batch, parentAlpha);
        }
        label.setPosition(getX(), getY());
        label.draw(batch, parentAlpha);
    }

    public int getCount() {
        return count;
    }
}
