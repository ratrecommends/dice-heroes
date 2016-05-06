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

package com.vlaaad.dice.game.effects;

import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.players.Player;

/**
 * Created 11.05.14 by vlaaad
 */
public class EnthrallmentEffect extends CreatureEffect {

    private final Player player;

    public EnthrallmentEffect(Player player, Ability ability, int turns) {
        super(ability, "enthrallment", turns);
        this.player = player;
    }

    @Override public void apply(Creature creature) {
        creature.player = player;
    }

    @Override public IFuture<Void> remove(Creature creature) {
        creature.player = creature.initialPlayer;
        return null;
    }

    @Override public String getIconName() {
        return "effect-icon/enthrallment";
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-enthrallment";
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-enthrallment";
    }
}
