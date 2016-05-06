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

package com.vlaaad.dice.ui.windows;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.ParticleActor;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 06.02.14 by vlaaad
 */
public class SelectCreatureToResurrectWindow extends GameWindow<SelectCreatureToResurrectWindow.Params> {
    private static final int PARTICLE_OFFSET = 10;

    private Creature selected;
    private Future<Creature> future;
    private ParticleActor particles;

    @Override protected void doShow(SelectCreatureToResurrectWindow.Params params) {
        future = params.future;
        selected = params.creatures.random();
        Table content = new Table(Config.skin);
        content.setBackground("ui-inventory-ability-window-background");
        content.defaults().pad(2);
        table.add(content);
        Label label = new LocLabel("ui-resurrect-header", Thesaurus.params().with("die", params.creature.description.nameLocKey()));
        label.setWrap(true);
        label.setAlignment(Align.center);
        content.add(label).width(110).row();
        content.add(new Image(Config.skin, "ui-creature-info-line")).width(50).row();

        Image selection = new Image(Config.skin.getDrawable("selection/turn"));
        selection.setY(-2);
        int i = 0;
        Table row = new Table();
        content.add(row).padTop(16).row();
        TextButton button = new LocTextButton("ui-select");
        for (Creature creature : params.creatures) {
            WorldObjectView view = ViewController.createView(params.creature.world.viewer, params.creature.world.playerColors, creature);
            row.add(view).padLeft(2).padRight(2);
            view.addListener(createListener(creature, button, view, selection));
            if (i % 4 == 3 && i != params.creatures.size - 1) {
                row = new Table();
                content.add(row).padTop(16).row();
            }
            i++;
        }
        content.add(button).padTop(6).padBottom(6).width(70);
        button.setDisabled(true);
        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        particles = new ParticleActor(Config.particles.get("ability-concentration").obtain());
        table.addActor(particles);
        table.layout();
        particles.toBack();
        particles.setPosition(content.getX() + content.getPrefWidth() / 2, content.getY() + content.getPrefHeight() / 2);
        for (ParticleEmitter emitter : particles.effect.getEmitters()) {
            emitter.getSpawnWidth().setHigh(content.getPrefWidth() + PARTICLE_OFFSET * 2);
            emitter.getSpawnHeight().setHigh(content.getPrefHeight() + PARTICLE_OFFSET * 2);
        }
    }

    private EventListener createListener(final Creature creature, final TextButton button, final WorldObjectView view, final Image selection) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                view.addActor(selection);
                selection.toBack();
                selected = creature;
                button.setDisabled(false);
            }
        };
    }

    @Override protected void onHide() {
        if (selected == null)
            throw new IllegalStateException("nothing selected!");
        future.happen(selected);
        particles.remove();
        particles.effect.free();
    }

    @Override protected boolean canBeClosed() {
        return false;
    }

    @Override public boolean handleBackPressed() {
        return true;
    }

    public static class Params {
        private final Creature creature;
        private final Array<Creature> creatures;
        private final Future<Creature> future;

        public Params(Creature creature, Array<Creature> creatures, Future<Creature> future) {
            this.creature = creature;
            this.creatures = creatures;
            this.future = future;
        }
    }
}
