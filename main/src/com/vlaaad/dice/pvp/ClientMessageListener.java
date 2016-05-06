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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.pvp.messaging.messages.*;
import com.vlaaad.dice.pvp.messaging.objects.PlacedCreature;
import com.vlaaad.dice.states.PvpPlayState;

/**
 * Created 28.07.14 by vlaaad
 */
public class ClientMessageListener extends PvpMessageListener {

    public ClientMessageListener(PvpPlayState state) {
        super(state);
    }

    @Override protected void init(final PvpPlayState state) {
        Logger.debug("client: wait for server to init");
        register(Init.class, new IMessageProcessor<Init>() {
            @Override public void receive(IParticipant from, Init message) {
                if (message.version > Config.mobileApi.getVersionCode()) {
                    sendToWithCallback(state.server, new UpdateNeeded()).addListener(new IFutureListener<Boolean>() {
                        @Override public void onHappened(Boolean aBoolean) {
                            session.disconnect(false, Config.thesaurus.localize("disconnect-server-needs-update"));
                        }
                    });
                    return;
                } else if (message.version < Config.mobileApi.getVersionCode()) {
                    session.disconnect(false, Config.thesaurus.localize("disconnect-update-needed"));
                    return;
                }
                LevelDescription level = (LevelDescription) Config.levels.get(message.level);
                ObjectMap<IParticipant, Fraction> fractions = new ObjectMap<IParticipant, Fraction>();
                for (ObjectMap.Entry<String, String> e : message.fractions.entries()) {
                    fractions.put(ClientMessageListener.this.state.session.getAll().get(e.key), Fraction.valueOf(e.value));
                }
                ClientMessageListener.this.state.prepare(level, fractions, message.seed);
                unregister(Init.class);
                awaitSpawns(state);
            }
        });
    }

    private void awaitSpawns(final PvpPlayState state) {
        Logger.debug("client: await of other players spawns");
        register(Spawned.class, new IMessageProcessor<Spawned>() {
            int toReceiveCount = session.getOthers().size;
            @Override public void receive(IParticipant from, Spawned message) {
                toReceiveCount--;
                Player player = state.world.players.get(message.fraction);
                ObjectIntMap<Ability> potions = new ObjectIntMap<Ability>();
                for (ObjectMap.Entry<String, Integer> p : message.potions.entries()) {
                    potions.put(Config.abilities.getById(p.key), p.value);
                }
                player.setPotions(potions);
                for (PlacedCreature c : message.dice) {
                    Array<Ability> abilities = new Array<Ability>(c.abilities.size);
                    for (String n : c.abilities) abilities.add(Config.abilities.getById(n));
                    Die die = new Die(Config.professions.getById(c.profession), c.name, c.exp, abilities, new ObjectIntMap<Ability>());
                    Creature creature = new Creature(die, player, c.id);
                    creature.setPosition(c.x, c.y);
                    player.addCreature(creature);

                    ObjectIntMap<Item> drop = new ObjectIntMap<Item>();
                    for (ObjectMap.Entry<String, Integer> e : c.drop.entries()) drop.put(Config.items.getById(e.key), e.value);
                    creature.setDrop(drop);

                    state.world.add(creature.getX(), creature.getY(), creature);
                }
                Logger.debug("client: received player with dice: " + player.creatures);
                if (toReceiveCount <= 0) {
                    Logger.debug("client: all spawned received");
                    unregister(Spawned.class);
                    awaitStart(state);
                }
            }
        });
    }

    private void awaitStart(final PvpPlayState state) {
        Logger.debug("client: awaiting for start");
        register(Start.class, new IMessageProcessor<Start>() {
            @Override public void receive(IParticipant from, Start message) {
                Array<Creature> creatures = new Array<Creature>(message.order.size);
                for (String id : message.order) {
                    creatures.add(state.world.creaturesById.get(id));
                }
                unregister(Start.class);
                play(state);
                state.startRound(creatures);
            }
        });
    }

    private void play(final PvpPlayState state) {
        Logger.debug("client: play!");
        register(RoundMessage.class, new IMessageProcessor<RoundMessage>() {
            @Override public void receive(IParticipant from, RoundMessage message) {
                state.receiveRoundMessage(message);
            }
        });
        register(RestartGame.class, new IMessageProcessor<RestartGame>() {
            @Override public void receive(IParticipant from, RestartGame message) {
                unregister(RoundMessage.class);
                unregister(RestartGame.class);
                restartGame();
            }
        });
    }

    public void restartGame() {
        init(state);
    }
}
