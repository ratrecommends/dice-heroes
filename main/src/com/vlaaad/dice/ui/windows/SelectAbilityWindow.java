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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.ui.components.AbilityIcon;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 04.02.14 by vlaaad
 */
public class SelectAbilityWindow extends GameWindow<SelectAbilityWindow.Params> {


    private static final float PARTICLE_OFFSET = 10;
    private static final Color DESCRIPTION = new Color(1, 1, 1, 0.4f);
    private Ability selected;
    private Params params;
    private ParticleActor particles;

    @Override protected void doShow(Params params) {
        this.params = params;
        Table content = new Table(Config.skin);
        content.defaults().pad(2);
        content.setBackground("ui-inventory-ability-window-background");
        table.add(content);

        TextButton button = new LocTextButton("ui-select");

        Image itemSelection = new Image(Config.skin.getDrawable("ui-creature-info-ability-selection"));


        LocLabel itemDescriptionLabel = new LocLabel(
            "ui-creature-info-window-ability-description",
            Thesaurus.params()
                .with("name", params.abilities.first().locNameKey())
                .with("desc", params.abilities.first().locDescKey()),
            DESCRIPTION
        );
        itemDescriptionLabel.setWrap(true);
        itemDescriptionLabel.setAlignment(Align.center);

        Label label = new LocLabel("ui-select-ability-for-" + params.ability.name, Thesaurus.params().with("die", params.creature.description.nameLocKey()));
        label.setWrap(true);
        label.setAlignment(Align.center);
        content.add(label).width(110).row();
        content.add(new Image(Config.skin, "ui-creature-info-line")).width(80).row();

        int i = 0;
        Table row = new Table();
        content.add(row).row();
        for (Ability ability : params.abilities) {
            Group group = new Group();
            AbilityIcon icon = new AbilityIcon(ability);
            group.addActor(icon);
            group.setSize(icon.getPrefWidth(), icon.getPrefHeight());
            row.add(group);
            ClickListener clickListener = createListener(ability, button, itemSelection, group, itemDescriptionLabel);
            icon.addListener(clickListener);
            if (i == 0) {
                clickListener.clicked(null, 0, 0);
            }
            if (i % 3 == 2 && i != params.abilities.size - 1) {
                row = new Table();
                content.add(row).row();
            }
            i++;
        }

        content.add(new Image(Config.skin, "ui-creature-info-line")).width(80).row();
        content.add(itemDescriptionLabel).width(ViewController.CELL_SIZE * 4 + 32).height(50).row();

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

    @Override protected void onHide() {
        if (selected == null)
            throw new IllegalStateException("Can't close without selecting ability!");
        params.callback.onSelected(selected);
        selected = null;
    }

    private ClickListener createListener(final Ability ability, final TextButton button, final Image itemSelection, final Group icon, final LocLabel itemDescriptionLabel) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                selected = ability;
                icon.addActor(itemSelection);
                itemSelection.toBack();
                button.setDisabled(false);
                itemDescriptionLabel.setParams(Thesaurus.params().with("name", ability.locNameKey()).with("desc", ability.locDescKey()));
            }
        };
    }

    @Override protected boolean canBeClosed() {
        return false;
    }

    @Override public boolean handleBackPressed() {
        return true;
    }

    public static class Params {
        private final Ability ability;
        private final Creature creature;
        private final Array<Ability> abilities;
        private final Callback callback;

        public Params(Ability ability, Creature creature, Array<Ability> abilities, Callback callback) {
            this.ability = ability;
            this.creature = creature;
            this.abilities = abilities;
            this.callback = callback;
        }
    }

    public static interface Callback {
        public void onSelected(Ability ability);
    }

}
