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

package com.vlaaad.dice.game.world.players.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.FractionRelation;
import com.vlaaad.dice.game.world.players.Player;

/**
 * Created 09.07.14 by vlaaad
 */
public class PlayerColors {

    private final ObjectMap<String, Color> colors = new ObjectMap<String, Color>();

    public void putColor(FractionRelation relation, Color color) {
        colors.put(relation.name(), color);
    }

    public void putColor(FractionRelation relation, Fraction fraction, Color color) {
        colors.put(relation.name() + "-" + fraction.name, color);
        if (!colors.containsKey(relation.name())) {
            putColor(relation, color);
        }
    }

    public Color getColor(Player viewer, Player target) {
        FractionRelation relation = viewer.getFractionRelation(target);
        Color result = colors.get(relation.name() + "-" + target.fraction.name);
        if (result == null) {
            result = colors.get(relation.name());
        }
        if (result == null)
            throw new IllegalStateException("there is no color from " + viewer + " to " + target + ": " + colors);
        return result;
    }
}
