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

package com.vlaaad.dice.pvp;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Logger;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.pvp.PvpMode;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.pvp.messaging.messages.*;
import com.vlaaad.dice.pvp.messaging.objects.PlacedCreature;
import com.vlaaad.dice.states.PvpPlayState;

import java.util.Comparator;

/**
 * Created 28.07.14 by vlaaad
 */
public class ServerMessageListener extends ClientMessageListener {

    public ServerMessageListener(PvpPlayState state) {
        super(state);
    }

    @Override protected void init(final PvpPlayState state) {
        super.init(state);
        initMatch(state);
    }

    private void initMatch(PvpPlayState state) {
        Logger.debug("server: init match "+this.state.session.getMode().name);
        PvpMode mode = this.state.session.getMode();
        LevelDescription level = mode.levels.random();
        String levelName = level.name;
        Array<Fraction> fractions = new Array<Fraction>(level.fractions.size);
        for (Fraction fraction : level.fractions) {
            fractions.add(fraction);
        }
        Array<String> ids = this.state.session.getAll().keys().toArray();
        ids.shuffle();
        ObjectMap<String, String> fMap = new ObjectMap<String, String>();
        for (int i = 0; i < ids.size; i++) {
            fMap.put(ids.get(i), fractions.get(i).name);
        }
        int seed = MathUtils.random.nextInt();
        awaitSpawning(state);
        sendToAll(new Init(levelName, fMap, seed));
    }

    private void awaitSpawning(final PvpPlayState state) {
        Logger.debug("server: await for spawning of other clients"/*, new RuntimeException()*/);
        final ObjectMap<IParticipant, Spawned> data = new ObjectMap<IParticipant, Spawned>();
        register(SpawnedToServer.class, new IMessageProcessor<SpawnedToServer>() {
            int toSpawn = session.getAll().size;
            @Override public void receive(IParticipant from, SpawnedToServer message) {
                Player player = state.participantsToPlayers.get(from);
                data.put(from, new Spawned(message.dice, message.potions, player.fraction));
                ObjectIntMap<Item> rolled = session.getMode().metaLevel.drop.roll();
                applyDrop(rolled, message.dice);
                if (data.size == toSpawn) {
                    for (ObjectMap.Entry<String, ? extends IParticipant> entry : session.getAll().entries()) {
                        IParticipant spawnedParticipant = entry.value;
                        Spawned spawned = data.get(spawnedParticipant);
                        for (IParticipant toNotify : session.getAll().values()) {
                            if (spawnedParticipant == toNotify) continue;
                            Logger.debug("server: notify " + toNotify + " about spawned " + spawned);
                            sendTo(toNotify, spawned);
                        }
                    }
                    Array<? extends Creature> creatures = state.world.byType(Creature.class);
                    creatures.sort(Ability.INITIATIVE_COMPARATOR);
                    unregister(SpawnedToServer.class);
                    Logger.debug("server: start!");
                    awaitRestart();
                    sendToAll(new Start(creatures));
                }
            }
        });
    }
    private void awaitRestart() {
        register(RestartRequest.class, new IMessageProcessor<RestartRequest>() {
            int toRestart = session.getAll().size;
            @Override public void receive(IParticipant from, RestartRequest message) {
                toRestart--;
                if (toRestart <= 0) {
                    unregister(RestartRequest.class);
                    sendToAll(new RestartGame());
//                    restartGame();
                }
            }
        });
    }

    private void applyDrop(ObjectIntMap<Item> droppedItems, Array<PlacedCreature> dice) {
        for (PlacedCreature c : dice) {
            c.drop.clear();
        }
        final ObjectIntMap<PlacedCreature> weights = new ObjectIntMap<PlacedCreature>();
        int total = 0;
        for (PlacedCreature creature : dice) {
            int cost = creature.exp;
            weights.put(creature, cost);
            total += cost;
        }
        dice.sort(new Comparator<PlacedCreature>() {
            @Override public int compare(PlacedCreature o1, PlacedCreature o2) {
                return weights.get(o2, 0) - weights.get(o1, 0);
            }
        });

        ObjectMap<PlacedCreature, ObjectIntMap<Item>> drop = new ObjectMap<PlacedCreature, ObjectIntMap<Item>>();
        for (PlacedCreature creature : dice) {
            drop.put(creature, new ObjectIntMap<Item>());
        }

        for (Item item : droppedItems.keys()) {
            int count = droppedItems.get(item, 1);
            float factor = count / (float) total;
            int distributedCount = 0;
            for (PlacedCreature creature : dice) {
                int creatureItemCount = (int) (weights.get(creature, 0) * factor);
                drop.get(creature).put(item, creatureItemCount);
                distributedCount += creatureItemCount;
            }
            if (distributedCount > count)
                throw new IllegalStateException("OMG! distributed " + item + " more than should! drop: " + drop + ", to distribute: " + droppedItems);
            if (distributedCount < count) {
                while (distributedCount < count) {
                    PlacedCreature random = dice.random();
                    drop.get(random).getAndIncrement(item, 0, 1);
                    distributedCount++;
                }
            }
        }
        for (PlacedCreature creature : drop.keys()) {
            for (ObjectIntMap.Entry<Item> e : drop.get(creature).entries()) {
                creature.drop.put(e.key.id, e.value);
            }
        }
    }
}
