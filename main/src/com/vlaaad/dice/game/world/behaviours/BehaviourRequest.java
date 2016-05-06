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

package com.vlaaad.dice.game.world.behaviours;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Grid2D;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.behaviours.params.AbilityAbilityParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCoordinatesParams;
import com.vlaaad.dice.game.world.behaviours.params.AbilityCreatureParams;
import com.vlaaad.dice.game.world.behaviours.params.TurnParams;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;

/**
 * Created 14.01.14 by vlaaad
 */
public final class BehaviourRequest<R, P> {
    private static final ObjectMap<String, BehaviourRequest> values = new ObjectMap<String, BehaviourRequest>();

    public static final BehaviourRequest<TurnResponse, TurnParams> TURN = new BehaviourRequest<TurnResponse, TurnParams>("tn");

    public static final BehaviourRequest<Creature, AbilityCreatureParams> CREATURE = new BehaviourRequest<Creature, AbilityCreatureParams>("cr");

    public static final BehaviourRequest<Grid2D.Coordinate, AbilityCoordinatesParams> COORDINATE = new BehaviourRequest<Grid2D.Coordinate, AbilityCoordinatesParams>("co");

    public static final BehaviourRequest<Ability, AbilityAbilityParams> ABILITY = new BehaviourRequest<Ability, AbilityAbilityParams>("ab");


    public final String name;

    private BehaviourRequest(String name) {
        this.name = name;
        values.put(name, this);
    }

    @Override public String toString() {
        return "#" + name + "#";
    }

    public static BehaviourRequest valueOf(String name) {
        BehaviourRequest request = values.get(name);
        if (request == null)
            throw new IllegalStateException("there is no such request: " + name);
        return request;
    }
}
