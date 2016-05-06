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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.common.util.Ref;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.imp.GiveExpResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.ui.components.ProfessionAbilityIcon;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 03.01.14 by vlaaad
 */
public class LevelUpWindow extends GameWindow<GiveExpResult> {
    private GiveExpResult result;

    @Override protected void initialize() {
        background.setDrawable(Config.skin.getDrawable("ui-reward-window-background"));
    }

    @Override protected void doShow(final GiveExpResult result) {
        this.result = result;
        Image back = new Image(Config.skin, "ui-levelup-window-background");
        back.setTouchable(Touchable.disabled);
        back.setPosition(getStage().getWidth() / 2 - back.getWidth() / 2, getStage().getHeight() / 2 - back.getHeight() / 2);
        table.addActor(back);
        back.setOrigin(back.getWidth() / 2f, back.getHeight() / 2f);
        rotateContinuously(back);

        // gather new abilities

        Array<Ability> newStoreAbilities = new Array<Ability>();
        Die creatureDie = result.creature.description;

        Die currentState = new Die(creatureDie.profession, creatureDie.name, result.creature.getCurrentExp(), creatureDie.abilities, creatureDie.inventory);
        Die nextState = new Die(creatureDie.profession, creatureDie.name, result.creature.getCurrentExp() + result.exp, creatureDie.abilities, creatureDie.inventory);

        Array<Ability> prevAbilities = currentState.getProfessionAbilities();
        final Array<Ability> newProfessionAbilities = nextState.getProfessionAbilities();
        newProfessionAbilities.removeAll(prevAbilities, true);

        for (Ability ability : Config.abilities.byType(Ability.Type.wearable)) {
            if (ability.cost >= 0 && ability.requirement.isSatisfied(nextState) && !ability.requirement.isSatisfied(currentState)) {
                newStoreAbilities.add(ability);
            }
        }

        // buttons

        LocTextButton continueButton = new LocTextButton("ui-level-up-continue");

        ImageButton share = new ImageButton(Config.skin, "share");
        SoundHelper.initButton(share);
        share.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Config.mobileApi.share(Config.thesaurus.localize("ui-level-up-share", Thesaurus.params()
                    .with("die", result.creature.description.nameLocKey())
                    .with("level", String.valueOf(result.creature.description.profession.getLevel(result.exp + result.creature.getCurrentExp())))
                ));
            }
        });


        Table buttons = new Table();
        buttons.defaults().pad(2);
        buttons.add(continueButton).width(67);
        buttons.add(share).size(19);


        final Table newItemsTable = new Table(Config.skin);


        final Table content = new Table(Config.skin);
        content.defaults().pad(3);
        content.setTouchable(Touchable.enabled);
        content.setBackground("ui-inventory-ability-window-background");

        content.add(new LocLabel("ui-level-up-title", Thesaurus.params()
            .with("die", result.creature.description.nameLocKey())
            .with("level", String.valueOf(result.creature.description.profession.getLevel(result.exp + result.creature.getCurrentExp())))
        )).row();
        content.add(new Image(Config.skin, "ui-creature-info-line")).width(66).row();
        content.add(newItemsTable).row();

        final Ref<Boolean> shownStore = new Ref<Boolean>(false);

        if (newStoreAbilities.size > 0) {
            addAbilities(newItemsTable, newStoreAbilities, false);
            shownStore.set(true);
        } else if (newProfessionAbilities.size > 0) {
            addAbilities(newItemsTable, newProfessionAbilities, true);
            content.setBackground("ui-creature-info-background");
            table.layout();
        }


        content.add(buttons);


        table.add(content).width(126);


        continueButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (shownStore.get() && newProfessionAbilities.size > 0) {
                    shownStore.set(false);
                    newItemsTable.clearChildren();
                    addAbilities(newItemsTable, newProfessionAbilities, true);
                    content.setBackground("ui-creature-info-background");
                    table.layout();
                } else {
                    hide();
                }
            }
        });
    }

    private void addAbilities(Table table, Array<Ability> abilities, boolean isProfession) {
        Table icons = new Table(Config.skin);
        abilities.sort(Ability.COST_COMPARATOR);
        for (Ability ability : abilities) {
            Actor icon;
            if (isProfession) {
                ProfessionAbilityIcon abilityIcon = new ProfessionAbilityIcon(result.creature, ability);
                abilityIcon.drawProgress = false;
                icon = abilityIcon;
                icon.setTouchable(Touchable.disabled);
            } else {
                Image image = new Image(Config.skin, "ability/" + ability.name + "-icon");
                image.setScaling(Scaling.stretch);
                icon = image;
            }
            icons.add(icon).size(icon.getWidth() * 2, icon.getHeight() * 2).pad(isProfession ? 0 : -8);
        }
        String enumData = Thesaurus.Util.enumerate(Config.thesaurus, abilities, new Thesaurus.Util.Stringifier<Ability>() {
            @Override public String toString(Ability ability) {
                return ability.locNameKey() + ".acc";
            }
        });

        LocLabel availableDescription = new LocLabel(isProfession ? "ui-level-up-profession-abilities" : "ui-level-up-abilities", Thesaurus.params()
            .with("enum", enumData)
            .with("grammatical-number", abilities.size > 1 ? "are" : "is")
        );
        availableDescription.setWrap(true);
        availableDescription.setAlignment(Align.center);
        table.add(icons).row();
        table.add(availableDescription).width(100).padTop(isProfession ? 0 : -4).row();
    }

    private void rotateContinuously(final Image back) {
        back.addAction(Actions.sequence(
            Actions.rotateBy(-360, 6f, Interpolation.linear),
            Actions.run(new Runnable() {
                @Override public void run() {
                    rotateContinuously(back);
                }
            })
        ));
    }
}
