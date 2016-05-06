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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pool;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.effects.CooldownEffect;
import com.vlaaad.dice.game.objects.Creature;

/**
 * Created 27.01.14 by vlaaad
 */
public class ProfessionAbilityIcon extends Widget implements Pool.Poolable {

    private static final int PROGRESS_OFFSET = 1;

    private final ClickListener listener = new ClickListener();

    private final Drawable up;
    private final Drawable down;
    private Drawable icon;
    private Drawable progress;
    private CooldownEffect cooldownEffect;
    public boolean drawProgress = true;

    public ProfessionAbilityIcon(Creature creature, Ability ability) {
        this();
        set(creature, ability);
    }

    public ProfessionAbilityIcon() {
        addListener(listener);
        up = Config.skin.getDrawable("profession-ability-up");
        down = Config.skin.getDrawable("profession-ability-down");
        setSize(getPrefWidth(), getPrefHeight());
    }

    public ProfessionAbilityIcon set(Creature creature, Ability ability) {
        cooldownEffect = creature.get(Attribute.cooldownFor(ability.name));
        icon = Config.skin.getDrawable("ability/" + ability.name + "-icon");
        progress = Config.skin.getDrawable("profession-ability-" + creature.profession.name + "-progress");
        return this;
    }

    @Override public void reset() {
        clearListeners();
        addListener(listener);
        setColor(Color.WHITE);
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        up.draw(batch, getX(), getY(), getWidth(), getHeight());
        float progressValue = 1;
        if (cooldownEffect != null) {
            progressValue = 1 - ((float) cooldownEffect.getTurnCount()) / (float) cooldownEffect.totalTurnCount;
        }
        if (drawProgress) {
            progress.draw(
                batch,
                getX() + PROGRESS_OFFSET,
                getY() + PROGRESS_OFFSET,
                getWidth() - PROGRESS_OFFSET * 2,
                progressValue * (getHeight() - PROGRESS_OFFSET * 2)
            );
        }
        if (listener.isPressed()) {
            down.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
        icon.draw(
            batch,
            getX()/* + getWidth() / 2 - icon.getMinWidth() / 2*/,
            getY()/* + getHeight() / 2 - icon.getMinHeight() / 2*/,
            getWidth()/*icon.getMinWidth()*/,
            getHeight()/*icon.getMinHeight()*/
        );
    }

    @Override public float getPrefWidth() {
        return up.getMinWidth();
    }

    @Override public float getPrefHeight() {
        return up.getMinHeight();
    }
}
