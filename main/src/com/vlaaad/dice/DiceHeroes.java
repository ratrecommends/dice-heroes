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

package com.vlaaad.dice;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.vlaaad.common.gdx.App;
import com.vlaaad.common.tutorial.Tutorial;
import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.Option;
import com.vlaaad.dice.achievements.AchievementManager;
import com.vlaaad.dice.achievements.events.EventType;
import com.vlaaad.dice.achievements.events.imp.EarnEvent;
import com.vlaaad.dice.achievements.events.imp.EndLevelEvent;
import com.vlaaad.dice.api.IMobileApi;
import com.vlaaad.dice.api.services.multiplayer.GameSession;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.levels.PvpLevelDescription;
import com.vlaaad.dice.game.config.pvp.PvpMode;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.tutorial.DiceTutorial;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.game.world.players.Fraction;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.tutorial.UserDataTutorialProvider;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.states.*;
import com.vlaaad.dice.ui.windows.BlockingWindow;
import com.vlaaad.dice.ui.windows.SignInWindow;
import com.vlaaad.dice.util.DicePreferences;

/**
 * Created 06.10.13 by vlaaad
 */
public class DiceHeroes extends App {

    private final UserDataHelper userDataHelper = new UserDataHelper();
    public UserData userData;
    public GameMapState gameMapState;

    private final ConflictResolver saveConflictResolver = new ConflictResolver(this);

    void applyNewUserData(UserData data) {
        userDataHelper.saveUserData(data);
        start();
    }

    public boolean firstSession;

    public DiceHeroes(IMobileApi mobileApi) {
        super(1);
        Config.mobileApi = mobileApi;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
        Config.clearRegions();
        Config.shapeRenderer = new ShapeRenderer();
        Config.assetManager = new AssetManager();
        Config.preferences = new DicePreferences(Gdx.app.getPreferences("com.vlaaad.dice.preferences"), this);
        Tutorial.killAll();

        setState(new IntroState(new IntroState.Callback() {
            @Override public void onEnded() {
                setScale(Config.preferences.getScale());
                setState(new LoadGameResourcesState(new LoadGameResourcesState.Callback() {
                    @Override
                    public void onResourcesLoaded() {
                        start();
                    }
                }));
            }
        }));
    }

    public void start() {
        userData = userDataHelper.loadUserData();

        Logger.log("user data: " + UserData.serialize(userData));

        if (!userData.tutorialCompleted) {
            firstSession = true;
            new Tutorial(Tutorial.resources().with("app", this), DiceTutorial.mainTutorialTasks()).start();
        }

        Config.mobileApi.services().dispatcher().clearListeners();
        saveConflictResolver.dispatcher().clearListeners();

        Config.mobileApi.setPurchaseListener(new PurchaseListener(this));

        gameMapState = new GameMapState(userData, new GameMapStateCallback(this, saveConflictResolver));

        Config.achievements = new AchievementManager(this, userData);

        setState(gameMapState);

        Config.mobileApi.services().dispatcher().addListener(new IStateDispatcher.Listener<ServicesState>() {
            @Override public void onChangedState(ServicesState newState) {
                if (newState == ServicesState.CONNECTED) {
                    Config.achievements.visualizer(Config.mobileApi.services().gameAchievements());
                    Config.mobileApi.services().cloudSave().sync(userData, saveConflictResolver);
//                    Config.mobileApi.services().cloudSave().load(cloudLoadCallback, saveConflictResolver);
                    Config.mobileApi.services().multiplayer().currentSession().addListener(new IStateDispatcher.Listener<Option<GameSession>>() {
                        @Override public void onChangedState(Option<GameSession> option) {
//                            Logger.debug("dh: current session changed to " + option);
                            if (option.isDefined()) {
                                GameSession session = option.get();
                                BaseLevelDescription metaLevel = session.getMode().metaLevel;
                                if (metaLevel.canBeStarted(userData)) {
//                                    Logger.debug("starting session");
                                    startPvpLevel(session);
                                } else {
                                    //disconnect
                                    if (!userData.tutorialCompleted || Tutorial.hasRunningTutorials()) {
                                        //silently
                                        Logger.debug("disconnect silently, because has running tutorials or tutorial not completed");
                                        session.disconnect(false);
                                    } else {
                                        session.disconnect(false, Config.thesaurus.localize("disconnect-level-not-reached-yet"));
                                        if (getState() == gameMapState) {
                                            gameMapState.centerOnLevel(metaLevel, false);
                                        }
                                    }
                                }
                            }
                        }
                    }, true);
                } else {
                    Config.achievements.visualizer(null);
                }
            }
        }, true);

        Config.achievements.fire(EventType.startup);
    }

    private void startPvpLevel(GameSession session) {
        setState(new PvpPlayState(userData, session, new PvpPlayStateCallback(this)));
    }

    public void play(final BaseLevelDescription baseLevel) {
        if (baseLevel.getClass() == LevelDescription.class) {
            playCampaignLevel(((LevelDescription) baseLevel));
        } else if (baseLevel.getClass() == PvpLevelDescription.class) {
            playPvpLevel(((PvpLevelDescription) baseLevel));
        }
    }

    private void playPvpLevel(PvpLevelDescription level) {
        PvpMode mode = level.getMode();
        PvpMode.Type type = mode.type;
        switch (type) {
            case friends:
                if (!Config.mobileApi.services().isSignedIn()) {
                    new SignInWindow().show("ui-multiplayer-sign-in");
                } else {
                    new BlockingWindow().show(Config.mobileApi.services().multiplayer().inviteFriends(level.players - 1, mode.variant));
                }
                break;
            case global:
                if (!Config.mobileApi.services().isSignedIn()) {
                    new SignInWindow().show("ui-multiplayer-sign-in");
                } else {
                    new BlockingWindow().show(Config.mobileApi.services().multiplayer().quickMatch(level.players - 1, mode.variant));
                }
                break;
            default:
                throw new IllegalStateException("unknown level mode: " + mode);
        }
    }

    private void playCampaignLevel(LevelDescription level) {

        Player protagonist = PlayerHelper.protagonist();
        for (Die die : userData.dice()) {
            protagonist.addDie(die);
        }
        protagonist.setPotions(userData.potions);
        protagonist.tutorialProvider = new UserDataTutorialProvider(userData);
        Player antagonist = PlayerHelper.antagonist();
        ObjectMap<Fraction, Player> players = new ObjectMap<Fraction, Player>();
        players.put(protagonist.fraction, protagonist);
        players.put(antagonist.fraction, antagonist);
        setState(new PvePlayState(this, protagonist, players, PlayerHelper.defaultColors, level, PvePlayState.GameState.PLACE, new PvePlayStateCallback(this, level)));
    }

    Array<RewardResult> applyLevelResult(BaseLevelDescription level, LevelResult result, boolean isWin) {
        Array<RewardResult> results = new Array<RewardResult>();
        if (isWin) {
            boolean isFirstPass = !userData.isPassed(level);
            Array<Reward> rewards = isFirstPass ? level.passRewards : level.confirmRewards;
            for (Reward reward : rewards) {
                results.add(reward.apply(userData));
            }
            userData.addLevelToPassed(level);
        }
        for (Die die : result.addedExperience.keys()) {
            die.exp += result.addedExperience.get(die, 0);
        }
        Config.achievements.fire(
            EventType.endLevel,
            Pools.obtain(EndLevelEvent.class).level(level).win(isWin).result(result)
        );
        for (Item item : result.viewer.earnedItems.keys()) {
            int count = result.viewer.earnedItems.get(item, 0);
            userData.incrementItemCount(item, count);
            Config.achievements.fire(EventType.earnItem, Pools.obtain(EarnEvent.class).item(item).count(count));
        }
        userData.setPotions(result.viewer.potions);
        save();
        return results;
    }

    public void save() {
        if (userData == null)
            return;
        preSave();
        userDataHelper.saveUserData(userData);
        postSave();
    }

    private void postSave() {
        if (Config.mobileApi.services().isSignedIn())
            Config.mobileApi.services().cloudSave().sync(userData, saveConflictResolver);
    }

    private void preSave() {
        if (Config.achievements != null)
            Config.achievements.save();
    }

    @Override
    public void dispose() {
        super.dispose();
        Config.assetManager.dispose();
        Config.clearRegions();
        if (userData != null) {
            save();
        }
        if (Config.shapeRenderer != null) {
            try {
                Config.shapeRenderer.dispose();
            } catch (Exception ignored) {
            }
        }
        SoundManager.instance.dispose();
    }
}
