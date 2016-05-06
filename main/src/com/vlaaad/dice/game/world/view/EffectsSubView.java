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

package com.vlaaad.dice.game.world.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;

/**
 * Created 17.11.13 by vlaaad
 */
public class EffectsSubView implements SubView {
    private final Table table = new Table(Config.skin);
    private final Creature creature;
    private final Array<CreatureEffect> effects = new Array<CreatureEffect>();

    public EffectsSubView(Creature creature) {
        this.creature = creature;
        table.setSize(7, ViewController.CELL_SIZE);
        table.align(Align.top);
        table.padTop(1);
        for (int i = 0; i < creature.effects.size; i++) {
            addEffect(creature.effects.get(i));
        }
    }

    public void addEffect(CreatureEffect effect) {
        if (effect.isHidden())
            return;
        effects.add(effect);
        refresh();
    }

    public void removeEffect(CreatureEffect effect) {
        if (effect.isHidden())
            return;
        effects.removeValue(effect, true);
        refresh();
    }

    private void refresh() {
        table.clearChildren();
        for (int i = 0; i < effects.size; i++) {
            if (i >= 3) // no more than 3 items can fit...
                break;
            CreatureEffect effect = effects.get(i);
            String drawableName = (i == 2 && effects.size > 3) ? "effect-icon/more" : effect.getIconName();
            Image image = new Image(Config.skin.getDrawable(drawableName));
            table.add(image).row();
        }
    }

    @Override public int getPriority() {
        return -100;
    }

    @Override public void play(String animationName) {
    }

    @Override public Actor getActor() {
        return table;
    }
}
