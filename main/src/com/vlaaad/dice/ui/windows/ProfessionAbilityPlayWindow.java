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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.effects.CooldownEffect;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 28.01.14 by vlaaad
 */
public class ProfessionAbilityPlayWindow extends GameWindow<ProfessionAbilityPlayWindow.Params> {


    private boolean doUse;
    private Callback callback;

    @Override protected void doShow(Params params) {
        callback = params.callback;
        Table content = new Table(Config.skin);
        content.defaults().pad(2);
        content.setBackground("ui-creature-info-background");
        content.setTouchable(Touchable.enabled);
        table.add(content);

        Image image = new Image(Config.skin, "ability/" + params.ability.name + "-icon");
        Image line = new Image(Config.skin, "ui-creature-info-line");
        TextButton use = new LocTextButton("ui-use-ability");

        Thesaurus.Params dp = Thesaurus.params();
        params.ability.fillDescriptionParams(dp, params.creature);
        Label desc = new LocLabel(params.ability.locDescKey(), dp);
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        content.add(image).size(image.getWidth() * 2, image.getHeight() * 2).padBottom(-6).padTop(0).row();
        content.add(new LocLabel(params.ability.locNameKey())).padBottom(3).row();
        content.add(line).width(50).row();
        content.add(desc).width(120).row();
        content.add(use).padBottom(6).padTop(6).width(90).row();

        Thesaurus.LocalizationData reason = new Thesaurus.LocalizationData();
        if (!params.ability.action.canBeApplied(params.creature, reason)) {
            use.setDisabled(true);
            CooldownEffect cooldown = params.creature.get(Attribute.cooldownFor(params.ability.name));
            LocLabel label;
            if (cooldown == null) {
                label = new LocLabel(reason.key, reason.params, StoreWindow.INACTIVE);
            } else {
                Thesaurus.Params p = Thesaurus.params()
                    .with("desc", cooldown.locDescKey())
                    .with("turn-count", String.valueOf(cooldown.getTurnCount()))
                    .with("die", params.creature.description.nameLocKey());
                params.ability.fillDescriptionParams(p, params.creature);
                label = new LocLabel("ui-creature-info-window-effect-description", p, StoreWindow.INACTIVE);
            }
            label.setWrap(true);
            label.setAlignment(Align.center);
            content.add(label).padLeft(4).padRight(4).padBottom(3).width(120).row();
        } else {
            use.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    doUse = true;
                    hide();
                }
            });
        }


    }

    @Override protected void onHide() {
        if (doUse) {
            callback.onUseAbility();
        }
    }

    public static class Params {
        private final Ability ability;
        private final Creature creature;
        private final Callback callback;

        public Params(Ability ability, Creature creature, Callback callback) {
            this.ability = ability;
            this.creature = creature;
            this.callback = callback;
        }
    }

    public static interface Callback {
        public void onUseAbility();
    }
}
