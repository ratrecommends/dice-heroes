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
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.game.actions.results.imp.DeathResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.controllers.ViewController;

/**
 * Created 03.02.14 by vlaaad
 */
public class PoisonEffect extends CreatureEffect {
    private final Creature poisoner;
    private World world;

    public PoisonEffect(Creature poisoner, Ability ability, int turnCount) {
        super(ability, "poison", turnCount);
        this.poisoner = poisoner;
    }

    @Override public void apply(Creature creature) {
        world = creature.world;
        creature.set(Attribute.poisoned, Boolean.TRUE);
    }

    @Override public IFuture<Void> remove(final Creature creature) {
        if (!creature.get(Attribute.poisoned)) {
            return null;
        }
        creature.set(Attribute.poisoned, Boolean.FALSE);
        final DeathResult result = new DeathResult(poisoner, creature);
        return world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
            @Override public void onHappened(Void aVoid) {
                result.apply(world);
            }
        });
    }

    @Override public String getIconName() {
        return "effect-icon/poison";
    }

    @Override public String getUiIconName() {
        return "effect-icon/ui-poison";
    }

    @Override public String locDescKey() {
        return "ui-effect-icon-poison";
    }
}
