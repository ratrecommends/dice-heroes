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

package com.vlaaad.dice.ui.windows;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.rewards.Reward;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.LevelResult;
import com.vlaaad.dice.managers.SoundManager;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;
import com.vlaaad.dice.ui.util.RewardViewHelper;
import com.vlaaad.dice.util.SoundHelper;

/**
 * Created 02.08.14 by vlaaad
 */
public class PvpWinWindow extends GameWindow<PvpWinWindow.Params> {

    private Params params;
    private boolean restart = false;
    private TextButton replay;

    public void disableRestart() {
        replay.setDisabled(true);
        restart = false;
    }

    @Override protected void initialize() {
        background.setDrawable(Config.skin.getDrawable("ui-reward-window-background"));
    }

    @Override protected float backgroundMaxAlpha() {
        return 0.75f;
    }

    @Override protected void doShow(final Params params) {
        this.params = params;

        Image back = new Image(Config.skin, "ui-levelup-window-background");
        back.setTouchable(Touchable.disabled);
        back.setPosition(getStage().getWidth() / 2 - back.getWidth() / 2, getStage().getHeight() / 2 - back.getHeight() / 2);
        table.addActor(back);
        back.setOrigin(back.getWidth() / 2f, back.getHeight() / 2f);
        back.addAction(Actions.forever(Actions.rotateBy(-360, 6f, Interpolation.linear)));

        ImageButton share = new ImageButton(Config.skin, "share");
        SoundHelper.initButton(share);
        share.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Config.mobileApi.share(params.shareText);
            }
        });
        TextButton ok = new LocTextButton("ui-lose-window-to-map");
        ok.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                restart = false;
                hide();
            }
        });
        replay = new LocTextButton("ui-replay-game");
        replay.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                restart = true;
                hide();
            }
        });

        Label victory = new LocLabel("ui-reward-window-victory", RewardViewHelper.VICTORY_TEXT_COLOR);
        victory.setFontScale(3f);

        table.add(victory).colspan(2).row();
        table.add(RewardViewHelper.createRewardsView(params.rewards, params.levelResult.viewer.earnedItems, params.userData)).colspan(2).row();
        table.add(replay).width(96).colspan(2).padBottom(10).row();
        table.add(ok).width(70).padRight(7).right();
        table.add(share).size(19).left().row();

        SoundManager.instance.playMusicAsSound("win");
    }

    @Override public boolean handleBackPressed() {
        return true;
    }

    @Override protected boolean canBeClosed() {
        return false;
    }

    @Override protected void onHide() {
        if (restart) {
            restart = false;
            params.callback.onRestart();
        } else {
            params.callback.onClose();
        }
        params = null;
    }

    public static final class Params {
        private final Array<RewardResult> rewards;
        private final String shareText;
        private final LevelResult levelResult;
        private final Callback callback;
        private final UserData userData;

        public Params(Array<RewardResult> rewards, String shareText, LevelResult levelResult, Callback callback, UserData userData) {
            this.rewards = rewards;
            this.shareText = shareText;
            this.levelResult = levelResult;
            this.callback = callback;
            this.userData = userData;
        }
    }

    public interface Callback {
        void onClose();
        void onRestart();
    }
}
