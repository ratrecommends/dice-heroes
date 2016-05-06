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

package com.vlaaad.dice.game.world.behaviours.responces;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.common.util.Tuple2;
import com.vlaaad.dice.game.actions.imp.Potion;
import com.vlaaad.dice.game.config.abilities.Ability;

/**
 * Created 14.01.14 by vlaaad
 */
public class TurnResponse<T> {

    public final TurnAction<T> action;
    public final T data;

    public TurnResponse(TurnAction<T> action, T data) {
        this.action = action;
        this.data = data;
    }

    public static class TurnAction<T> {
        private static final ObjectMap<String, TurnAction> values = new ObjectMap<String, TurnAction>();
        public static TurnAction valueOf(String name) {
            if (!values.containsKey(name))
                throw new IllegalStateException("there is no turn action with name " + name);
            return values.get(name);
        }

        public static final TurnAction<Grid2D.Coordinate> MOVE = new TurnAction<Grid2D.Coordinate>("mv");
        public static final TurnAction<Ability> PROFESSION_ABILITY = new TurnAction<Ability>("pr");
        public static final TurnAction<Void> SKIP = new TurnAction<Void>("-");
        public static final TurnAction<Tuple2<Ability, Potion.ActionType>> POTION = new TurnAction<Tuple2<Ability, Potion.ActionType>>("pn");

        public final String name;

        private TurnAction(String name) {
            this.name = name;
            values.put(name, this);
        }
    }
}
