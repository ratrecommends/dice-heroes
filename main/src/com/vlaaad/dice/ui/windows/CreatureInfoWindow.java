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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.gdx.scene2d.TabPane;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.view.WorldObjectView;
import com.vlaaad.dice.ui.components.AbilityIcon;
import com.vlaaad.dice.ui.components.ExpBar;
import com.vlaaad.dice.ui.components.ProfessionAbilityIcon;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 12.10.13 by vlaaad
 */
public class CreatureInfoWindow extends GameWindow<CreatureInfoWindow.Params> {

    private static final Color DESCRIPTION = new Color(1, 1, 1, 0.4f);

    private Params params;

    @Override protected void doShow(final Params params) {
        this.params = params;
        table.clearChildren();
        Table info = new Table(Config.skin);
        info.setBackground(Config.skin.getDrawable("ui-creature-info-background"));


        Ability current = params.creature.getCurrentAbility();
        LocLabel itemDescriptionLabel = new LocLabel(
            "ui-creature-info-window-ability-description",
            Thesaurus.params()
                .with("name", current.locNameKey())
                .with("desc", current.locDescKey()),
            DESCRIPTION
        );
        itemDescriptionLabel.setWrap(true);
        itemDescriptionLabel.setAlignment(Align.center);

        LocLabel effectDescriptionLabel = new LocLabel(
            "ui-creature-info-window-no-effects",
            Thesaurus.params().with("name", params.creature.description.nameLocKey()),
            DESCRIPTION
        );
        effectDescriptionLabel.setWrap(true);
        effectDescriptionLabel.setAlignment(Align.center);

        Image itemSelection = new Image(Config.skin.getDrawable("ui-creature-info-ability-selection"));
        Table abilityIcons = new Table();
        abilityIcons.padTop(5);
        boolean selected = false;
        int i = 0;
        for (Ability ability : params.creature) {
            AbilityIcon image = new AbilityIcon(ability);
            Group imageGroup = new Group();
            imageGroup.setSize(image.getWidth(), image.getHeight());
            imageGroup.addActor(image);
            abilityIcons.add(imageGroup);
            ClickListener listener = createListener(imageGroup, itemSelection, itemDescriptionLabel, ability, params.creature);
            if (!selected && i == params.creature.getCurrentAbilityIndex()) {
                selected = true;
                listener.clicked(null, 0, 0);
            }
            image.addListener(listener);
            if (i == 3) {
                abilityIcons.row();
                abilityIcons.add();
            }
            i++;
        }

        Image effectSelection = new Image(Config.skin.getDrawable("ui-creature-info-ability-selection"));
        Table effectsIcons = new Table();
        effectsIcons.padTop(5);
        i = 0;
        boolean hasEffects = params.creature.effects.size > 0;
        for (CreatureEffect effect : params.creature.effects) {
            if (effect.isHidden())
                continue;
            Image effectIcon = new Image(Config.skin, effect.getUiIconName());
            Group imageGroup = new Group();
            imageGroup.setSize(24, 24);
            imageGroup.addActor(effectIcon);
            effectIcon.setPosition(imageGroup.getWidth() / 2 - effectIcon.getWidth() / 2, imageGroup.getHeight() / 2 - effectIcon.getHeight() / 2);
            effectsIcons.add(imageGroup);
            ClickListener listener = createListener(imageGroup, effectSelection, effectDescriptionLabel, effect, params.creature);
            if (i == 0) {
                listener.clicked(null, 0, 0);
            }
            effectIcon.addListener(listener);
            if (i == 3) {
                abilityIcons.row();
            }
            i++;
        }

        WorldObjectView view = ViewController.createView(params.world.viewer, params.world.playerColors, params.creature);
        view.removeSubView("name");
        view.removeSubView(CreatureEffect.class);

        Table headerLabels = new Table();
        headerLabels.align(Align.left);
        headerLabels.add(new LocLabel("ui-creature-info-name", Thesaurus.params()
            .with("name", params.creature.description.nameLocKey())
            .with("profession", params.creature.profession.locKey())
        )).expandX().fillX().row();
        if (params.creature.player == params.world.viewer) {
            headerLabels.add(new LocLabel("ui-creature-info-ally-info", Thesaurus.params()
                .with("level", String.valueOf(params.creature.getCurrentLevel()))
            )).padTop(-4).expandX().fillX().row();
        } else {
            headerLabels.add(new LocLabel("ui-creature-info-enemy-info")).expandX().fillX().padTop(-4).row();
        }

        Table header = new Table(Config.skin);
        header.align(Align.left);
        header.add(view).padTop(7).padRight(4).left();
        header.add(headerLabels).left().row();
        if (params.creature.player == params.world.viewer) {
            float progress = params.creature.description.profession.getLevelProgress(params.creature.getCurrentExp());
            header.add(new ExpBar(progress)).width(ViewController.CELL_SIZE * 4).padLeft(3).padTop(4).left().colspan(2).row();
        }
        Array<Ability> professionAbilities = params.creature.profession.getAvailableAbilities();
        Table pa = new Table();
        pa.defaults().padTop(4);
        for (Ability a : professionAbilities) {
            ProfessionAbilityIcon icon = new ProfessionAbilityIcon(params.creature, a);
            pa.add(icon).row();
            icon.addListener(createProfessionAbilityListener(params.creature, a));
            if (!a.requirement.isSatisfied(params.creature.description)) {
                icon.getColor().a = 0.3f;
            }
        }

        Table headerTable = new Table();
        headerTable.add(header);
        headerTable.add(pa).expandX().right().padRight(3).padTop(2);

        info.add(headerTable).expandX().fillX().left().row();

        final TabPane tabPane = new TabPane(Config.skin);

        Table itemsHeader = new Table();
        itemsHeader.align(Align.left);
        itemsHeader.add(new Image(Config.skin, "ui-creature-info-window-icon-items")).padRight(2);
        itemsHeader.add(new LocLabel("ui-creature-info-items")).padTop(-4).expandX().fillX().align(Align.left);

        Table effectsHeader = new Table();
        effectsHeader.align(Align.left);
        effectsHeader.add(new Image(Config.skin, "ui-creature-info-window-icon-effects")).padRight(2);
        effectsHeader.add(new LocLabel("ui-creature-info-effects")).padTop(-4).expandX().fillX().align(Align.left);

        Table itemsTab = new Table();

        itemsTab.add(abilityIcons).padTop(5).row();
        itemsTab.add(new Image(Config.skin.getDrawable("ui-creature-info-line"))).width(ViewController.CELL_SIZE * 4 - 16).padTop(5).row();
        itemsTab.add(itemDescriptionLabel).width(ViewController.CELL_SIZE * 4 + 32).height(50).pad(5);

        Table effectsTab = new Table();
        if (hasEffects) {
            effectsTab.add(effectsIcons).minHeight(48).padTop(10).row();
            effectsTab.add(new Image(Config.skin.getDrawable("ui-creature-info-line"))).width(ViewController.CELL_SIZE * 4 - 16).padTop(5).row();
        }
        effectsTab.add(effectDescriptionLabel).width(ViewController.CELL_SIZE * 4 + 32).height(50).pad(5);

        tabPane.addTab(itemsHeader, itemsTab);
        tabPane.addTab(effectsHeader, effectsTab);

        info.add(tabPane).padTop(5).padBottom(-2).padLeft(-2).padRight(-2);
        info.setTouchable(Touchable.enabled);

        table.add(info);
    }

    private ClickListener createProfessionAbilityListener(final Creature creature, final Ability ability) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                new ProfessionAbilityDescriptionWindow().show(new ProfessionAbilityDescriptionWindow.Params(creature, ability));
            }
        };
    }

    private ClickListener createListener(final Group group, final Image selection, final LocLabel label, final CreatureEffect effect, final Creature creature) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                group.addActor(selection);
                final int turnCount = effect.getTurnCount();
                if (turnCount > 1000) {
                    //forever effect
                    label.setKey("ui-creature-info-window-forever-effect-description");
                    label.setParams(Thesaurus.params()
                            .with("desc", effect.locDescKey())
                            .with("die", creature.description.nameLocKey())
                    );
                } else {
                    label.setKey("ui-creature-info-window-effect-description");
                    label.setParams(Thesaurus.params()
                            .with("desc", effect.locDescKey())
                            .with("turn-count", String.valueOf(turnCount))
                            .with("die", creature.description.nameLocKey())
                    );
                }
            }
        };
    }

    private ClickListener createListener(final Group abilityIconGroup, final Image selection, final LocLabel label, final Ability ability, final Creature creature) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                abilityIconGroup.addActor(selection);
                label.setParams(
                    ability.fillDescriptionParams(Thesaurus.params(), creature)
                        .with("name", ability.locNameKey())
                        .with("desc", ability.locDescKey())
                );
            }
        };
    }

    @Override protected void onHide() {
    }

    public static class Params {
        private final Creature creature;
        private final World world;

        public Params(Creature creature, World world) {
            this.creature = creature;
            this.world = world;
        }
    }
}
