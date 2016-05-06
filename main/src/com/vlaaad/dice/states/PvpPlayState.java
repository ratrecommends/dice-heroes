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

package com.vlaaad.dice.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.gdx.State;
import com.vlaaad.common.util.*;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;
import com.vlaaad.dice.game.actions.imp.Potion;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.behaviours.BehaviourRequest;
import com.vlaaad.dice.game.world.behaviours.responces.TurnResponse;
import com.vlaaad.dice.game.world.controllers.*;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.pvp.ClientMessageListener;
import com.vlaaad.dice.pvp.ServerMessageListener;
import com.vlaaad.dice.pvp.messaging.messages.*;
import com.vlaaad.dice.pvp.messaging.objects.PlacedCreature;
import com.vlaaad.dice.ui.components.CountDownLabel;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.windows.BlockingWindow;
import com.vlaaad.dice.ui.windows.PauseWindow;

import static com.vlaaad.common.util.Option.*;

/**
 * Created 28.07.14 by vlaaad
 */
public class PvpPlayState extends State {

    static {
        //messages
        Init.register();
        RestartGame.register();
        RestartRequest.register();
        RoundMessage.register();
        Spawned.register();
        SpawnedToServer.register();
        Start.register();
        UpdateNeeded.register();
        //objects
        PlacedCreature.register();
    }

    private final UserData userData;
    public final GameSession session;
    private final PauseWindow pauseWindow = new PauseWindow();
    private final PauseWindow.Params params = new PauseWindow.Params(new PauseWindow.Callback() {
        @Override public void onRestart() { throw new IllegalStateException("restart is not allowed here!"); }
        @Override public void onCancel() { callback.onCancel(session, Tuple3.make(false, Option.<String>none(), Option.<Throwable>none()), option(world), option(playersToParticipants)); }
    }, false);
    private final Callback callback;
    public World world;
    public ClientMessageListener listener;
    public IParticipant server;
    private int sessionActionIndex = 0;
    public final ObjectMap<IParticipant, Player> participantsToPlayers = new ObjectMap<IParticipant, Player>();
    public final ObjectMap<Player, IParticipant> playersToParticipants = new ObjectMap<Player, IParticipant>();
    private Array<RoundMessage> roundMessages = new Array<RoundMessage>();
    private Array<Future> messageWaiters = new Array<Future>();
    public LevelDescription level;

    private final RestartCallback restartCallback = new RestartCallback() {
        @Override public void onRestart() {
            sessionActionIndex = 0;
            participantsToPlayers.clear();
            playersToParticipants.clear();
            roundMessages.clear();
            messageWaiters.clear();
            level = null;
            world.destroy();
            world = null;
            showPrepareWindow();
            listener.sendToServer(new RestartRequest());
        }
    };
    private Future<Void> prepareFuture;

    public PvpPlayState(UserData userData, GameSession session, Callback callback) {
        super();
        this.userData = userData;
        this.session = session;
        this.callback = callback;
    }

    @Override protected void init() {
        Config.mobileApi.keepScreenOn(true);
        session.disconnectFuture().addListener(new IFutureListener<Tuple3<Boolean, Option<String>, Option<Throwable>>>() {
            @Override public void onHappened(Tuple3<Boolean, Option<String>, Option<Throwable>> reason) {
                callback.onCancel(session, reason, option(world), option(playersToParticipants));
            }
        });
        Array<String> ids = new Array<String>(session.getAll().size);
        for (IParticipant participant : session.getAll().values()) ids.add(participant.getId());
        ids.sort();
        listener = null;
        server = session.getAll().get(ids.first());
        showPrepareWindow();
        if (server == session.getMe()) {
            Logger.debug("I am a server!");
            listener = new ServerMessageListener(this);
        } else {
            Logger.debug("I am a client!");
            listener = new ClientMessageListener(this);
        }
        session.setMessageListener(listener);
    }
    private void showPrepareWindow() {
        new BlockingWindow() {
            @Override protected void doShow(IFuture<?> future) {
                super.doShow(future);
                table.setTouchable(Touchable.enabled);
                table.row();
                table.add(new LocLabel("ui-waiting-another-player"));
            }
            @Override public boolean handleBackPressed() {
                if (isShown()) {
                    callback.onCancel(session, Tuple3.make(false, Option.<String>none(), Option.<Throwable>none()), option(world), option(playersToParticipants));
                    hide();
                }
                return true;
            }
            @Override protected boolean canBeClosed() {
                return true;
            }
        }.show(prepareFuture = new Future<Void>());
    }

    @Override protected void resume(boolean isStateChange) {}

    @Override protected void pause(boolean isStateChange, Stage stage) {}

    @Override protected void dispose(boolean isStateChange, Stage stage) {
        Config.mobileApi.keepScreenOn(false);
        if (stage != null)
            SoundManager.instance.stopMusicBeautifully("ambient-battle", stage);
        else
            SoundManager.instance.stopMusic("ambient-battle");
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                Logger.debug("disconnect because disposing");
                session.disconnect(false);
                PvpPlayState.this.stage.dispose();
            }
        });
    }

    @Override protected boolean disposeOnSwitch() {
        return true;
    }

    public void prepare(LevelDescription level, ObjectMap<IParticipant, Fraction> fractions, int seed) {
        if (prepareFuture != null) prepareFuture.happen();
        this.level = level;
        ObjectMap<Fraction, Player> players = new ObjectMap<Fraction, Player>();
        Player viewer = null;
        for (ObjectMap.Entry<IParticipant, Fraction> e : fractions.entries()) {
            Fraction f = e.value;
            Player player = new Player(f, level.relations.get(f));
            if (e.key == session.getMe()) {
                viewer = player;
            }
            participantsToPlayers.put(e.key, player);
            playersToParticipants.put(player, e.key);
            players.put(f, player);
        }
        if (viewer == null)
            throw new IllegalStateException("WTF! viewer is null!");
        viewer.setPotions(userData.potions);
        for (Die die : userData.dice()) {
            viewer.addDie(die);
        }
        world = new World(viewer, players, PlayerHelper.defaultColors, level, stage);
        world.addController(ViewController.class);
        world.addController(CreatureInfoController.class);
        world.init();
        world.addController(PvpLoadLevelController.class);
        world.addController(SpawnController.class);
        world.addController(new RandomController(world, seed));
        world.dispatcher.add(SpawnController.START, new EventListener<Void>() {
            @Override public void handle(EventType<Void> type, Void aVoid) {
                world.removeController(SpawnController.class);
                showPrepareWindow();
                listener.sendToServer(new SpawnedToServer(world.viewer));
            }
        });
    }
    public void startRound(final Array<Creature> creatures) {
        if (prepareFuture != null) prepareFuture.happen();
        Logger.debug("pvp: start round!");
        world.dispatcher.add(RoundController.PRE_START, new EventListener<RoundController>() {
            @Override public void handle(EventType<RoundController> type, RoundController roundController) {
                world.dispatcher.remove(RoundController.PRE_START, this);
                RoundController c = world.getController(RoundController.class);
                c.queue.clear();
                c.queue.addAll(creatures);
            }
        });
        world.addController(new PvpBehaviourController(world, this), BehaviourController.class);
        world.addController(UiController.class);
        world.dispatcher.add(RoundController.WIN, new EventListener<LevelResult>() {
            @Override public void handle(EventType<LevelResult> type, LevelResult levelResult) {
                callback.onWin(session.getMode().metaLevel, levelResult, playersToParticipants, restartCallback);
            }
        });
        world.dispatcher.add(RoundController.LOSE, new EventListener<LevelResult>() {
            @Override public void handle(EventType<LevelResult> type, LevelResult levelResult) {
                callback.onLose(session.getMode().metaLevel, levelResult, restartCallback);
            }
        });
        world.addController(RoundController.class);
    }

    @SuppressWarnings("unchecked")
    public void sendRoundMessage(BehaviourRequest request, Object response) {
        sessionActionIndex++;
        RoundMessage message = new RoundMessage();
        message.i = sessionActionIndex;
        message.q = request.name;
        if (request == BehaviourRequest.TURN) {
            TurnResponse r = ((TurnResponse) response);
            Object data;
            if (r.action == TurnResponse.TurnAction.MOVE) {
                data = r.data;
            } else if (r.action == TurnResponse.TurnAction.PROFESSION_ABILITY) {
                data = ((Ability) r.data).id;
            } else if (r.action == TurnResponse.TurnAction.POTION) {
                Tuple2<Ability, Potion.ActionType> tuple = (Tuple2<Ability, Potion.ActionType>) r.data;
                data = Array.with(tuple._1.id, tuple._2 != null ? tuple._2.ordinal() : -1);
            } else if (r.action == TurnResponse.TurnAction.SKIP) {
                data = 0;
            } else {
                throw new IllegalStateException("unknown turn action type: " + r.action);
            }
            message.a = new RoundMessage.TurnMessage(r.action.name, data);
        } else if (request == BehaviourRequest.ABILITY) {
            message.a = ((Ability) response).id;
        } else if (request == BehaviourRequest.COORDINATE) {
            message.a = response;
        } else if (request == BehaviourRequest.CREATURE) {
            message.a = ((Creature) response).id;
        } else {
            throw new IllegalStateException("unknown request: " + request);
        }
        listener.sendToOthers(message);
    }

    public void receiveRoundMessage(RoundMessage message) {
        sessionActionIndex = message.i;
        roundMessages.add(message);
        roundMessages.sort(RoundMessage.COMPARATOR);
        checkRoundMessages();
    }

    @SuppressWarnings("unchecked")
    private void checkRoundMessages() {
        if (roundMessages.size == 0 || messageWaiters.size == 0)
            return;
        RoundMessage message = roundMessages.removeIndex(0);
        Future future = messageWaiters.removeIndex(0);
        BehaviourRequest request = BehaviourRequest.valueOf(message.q);
        if (request == BehaviourRequest.ABILITY) {
            String s = (String) message.a;
            future.happen(Config.abilities.getById(s));
        } else if (request == BehaviourRequest.COORDINATE) {
            Array<Number> coordinate = (Array<Number>) message.a;
            future.happen(new Grid2D.Coordinate(coordinate.get(0).intValue(), coordinate.get(1).intValue()));
        } else if (request == BehaviourRequest.CREATURE) {
            future.happen(world.creaturesById.get((String) message.a));
        } else if (request == BehaviourRequest.TURN) {
            RoundMessage.TurnMessage m = (RoundMessage.TurnMessage) message.a;
            TurnResponse.TurnAction action = TurnResponse.TurnAction.valueOf(m.action);
            if (action == TurnResponse.TurnAction.MOVE) {
                Array<Number> coordinateArr = (Array<Number>) m.data;
                Grid2D.Coordinate coordinate = new Grid2D.Coordinate(coordinateArr.get(0).intValue(), coordinateArr.get(1).intValue());
                future.happen(new TurnResponse(action, coordinate));
            } else if (action == TurnResponse.TurnAction.PROFESSION_ABILITY) {
                future.happen(new TurnResponse(action, Config.abilities.getById((String) m.data)));
            } else if (action == TurnResponse.TurnAction.POTION) {
                Array<Object> data = (Array<Object>) m.data;
                String id = (String) data.get(0);
                int typeOrdinal = ((Number) data.get(1)).intValue();
                future.happen(new TurnResponse(
                    action,
                    Tuple2.make(
                        Config.abilities.getById(id),
                        typeOrdinal == -1 ? null : Potion.ActionType.values()[typeOrdinal]
                    )
                ));
            } else if (action == TurnResponse.TurnAction.SKIP) {
                future.happen(new TurnResponse<Void>(TurnResponse.TurnAction.SKIP, null));
            } else {
                throw new IllegalStateException("unknown potion action type: " + action);
            }
        } else {
            throw new IllegalStateException("unknown request: " + request);
        }
        checkRoundMessages();
    }

    @Override protected Color getBackgroundColor() {
        return (level == null || level.backgroundColor == null) ? PvePlayState.BACKGROUND : level.backgroundColor;
    }
    @Override protected void onBackPressed() {
        pauseWindow.show(params);
    }

    public <T> IFuture<T> waitForRoundMessage() {
        final Future<T> future = new Future<T>();
        messageWaiters.add(future);
        checkRoundMessages();
        return future;
    }

    public static interface Callback {
        void onWin(BaseLevelDescription level, LevelResult result, ObjectMap<Player, IParticipant> playersToParticipants, RestartCallback callback);
        void onLose(BaseLevelDescription level, LevelResult result, RestartCallback callback);
        void onCancel(GameSession session, Tuple3<Boolean, Option<String>, Option<Throwable>> reason, Option<World> world, Option<ObjectMap<Player, IParticipant>> participants);
    }

    public static interface RestartCallback {
        void onRestart();
    }
}
