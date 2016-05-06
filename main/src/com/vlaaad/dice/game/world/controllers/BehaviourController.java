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

package com.vlaaad.dice.game.world.controllers;

import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.game.world.behaviours.Behaviour;
import com.vlaaad.dice.game.world.behaviours.BehaviourBuilder;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;

/**
 * Created 08.10.13 by vlaaad
 */
public abstract class BehaviourController extends WorldController {

    protected final ObjectMap<Object, Behaviour> behaviours = new ObjectMap<Object, Behaviour>();

    public BehaviourController(World world) {
        super(world);
    }

    protected final void registerBehaviour(Object key, BehaviourBuilder builder) {
        registerBehaviour(key, builder.build());
    }

    protected final void registerBehaviour(Object key, Behaviour behaviour) {
        behaviours.put(key, behaviour);
    }

    protected abstract void initBehaviours();

    @Override protected void start() {
        initBehaviours();
    }

    @Override protected void stop() {
        behaviours.clear();
    }

    public Behaviour get(Creature creature) {
        if (behaviours.containsKey(creature)) {
            return behaviours.get(creature);
        }
        return getBehaviour(creature);
    }
    protected abstract Behaviour getBehaviour(Creature creature);

    public void put(Creature creature, Behaviour behaviour) {
        behaviours.put(creature, behaviour);
    }

    public void remove(Creature creature) {
        behaviours.remove(creature);
    }
}
