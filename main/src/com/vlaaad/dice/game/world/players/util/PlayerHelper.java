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
public class PlayerHelper {
    public static final Fraction protagonist = Fraction.valueOf("protagonist");
    public static final Fraction antagonist = Fraction.valueOf("antagonist");

    public static final ObjectMap<Fraction, FractionRelation> defaultProtagonistRelations = new ObjectMap<Fraction, FractionRelation>();
    static {
        defaultProtagonistRelations.put(antagonist, FractionRelation.enemy);
    }
    public static final ObjectMap<Fraction, FractionRelation> defaultAntagonistRelations = new ObjectMap<Fraction, FractionRelation>();
    static {
        defaultAntagonistRelations.put(protagonist, FractionRelation.enemy);
    }

    public static Player protagonist() {
        return new Player(protagonist, defaultProtagonistRelations);
    }

    public static Player antagonist() {
        return new Player(antagonist, defaultAntagonistRelations);
    }


    public static final Player defaultProtagonist = protagonist();
    public static final Player defaultAntagonist = antagonist();

    public static final PlayerColors defaultColors = new PlayerColors();
    static {
        defaultColors.putColor(FractionRelation.ally, Color.WHITE);
        defaultColors.putColor(FractionRelation.enemy, new Color(0.964705882f, 0.509803922f, 0.478431373f, 1));
    }
}
