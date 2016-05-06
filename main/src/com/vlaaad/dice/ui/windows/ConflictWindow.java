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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.states.GameMapState;
import com.vlaaad.dice.ui.components.Hint;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 24.05.14 by vlaaad
 */
public class ConflictWindow extends GameWindow<ConflictWindow.Params> {

    private GameMapState.ConflictResolution resolution;
    private Callback callback;

    @Override protected void doShow(Params params) {
        callback = params.callback;
        Table content = new Table(Config.skin);
        content.defaults().pad(2);
        content.setTouchable(Touchable.enabled);
        content.setBackground("ui-store-window-background");

        LocLabel desc = new LocLabel("ui-conflict-description");
        desc.setWrap(true);
        desc.setAlignment(Align.center);

        Table diff = new Table(Config.skin);
        diff.defaults().uniform().pad(1);
        UserData local = params.localData;
        UserData server = params.serverData;


        diff.add(String.valueOf(local.numPassedLevels()), "default", AboutWindow.INACTIVE).expandX();
        final Tile levels = new Tile("ui/conflict-window/levels");
        Hint.make(levels, "ui-conflict-window-levels");
        diff.add(levels);
        diff.add(String.valueOf(server.numPassedLevels()), "default", AboutWindow.INACTIVE).expandX();
        diff.row();

        diff.add(String.valueOf(local.diceCount()), "default", AboutWindow.INACTIVE);
        final Tile dice = new Tile("ui/conflict-window/dice");
        Hint.make(dice, "ui-conflict-window-dice");
        diff.add(dice);
        diff.add(String.valueOf(server.diceCount()), "default", AboutWindow.INACTIVE);
        diff.row();

        Item coin = Config.items.get("coin");
        diff.add(String.valueOf(local.getItemCount(coin)), "default", AboutWindow.INACTIVE);
        final Tile coins = new Tile("ui/conflict-window/coins");
        Hint.make(coins, "ui-conflict-window-coins");
        diff.add(coins);
        diff.add(String.valueOf(server.getItemCount(coin)), "default", AboutWindow.INACTIVE);
        diff.row();

        diff.add(String.valueOf(local.potionsCount()), "default", AboutWindow.INACTIVE);
        final Tile potions = new Tile("ui/conflict-window/potions");
        Hint.make(potions, "ui-conflict-window-potions");
        diff.add(potions);
        diff.add(String.valueOf(server.potionsCount()), "default", AboutWindow.INACTIVE);
        diff.row();

        diff.add(String.valueOf(local.itemsCount(Item.Type.ingredient)), "default", AboutWindow.INACTIVE);
        final Tile ingredients = new Tile("ui/conflict-window/ingredients");
        Hint.make(ingredients, "ui-conflict-window-ingredients");
        diff.add(ingredients);
        diff.add(String.valueOf(server.itemsCount(Item.Type.ingredient)), "default", AboutWindow.INACTIVE);

        LocTextButton useLocal = new LocTextButton("ui-use-local");
        useLocal.addListener(listener(GameMapState.ConflictResolution.useLocal));
        LocTextButton useServer = new LocTextButton("ui-use-server");
        useServer.addListener(listener(GameMapState.ConflictResolution.useServer));

        Table buttons = new Table();
        buttons.defaults().uniformX();
        buttons.add(useLocal).expandX().fillX().padRight(3);
        buttons.add(useServer).expandX().fillX();

        content.add(new LocLabel("ui-conflict-header")).padBottom(0).row();
        content.add(desc).width(130).padTop(0).row();
        content.add(new Tile("ui-creature-info-line")).width(80).pad(4).row();
        content.add(diff).width(110).padBottom(3).row();
        content.add(buttons).expandX().fillX();

        table.add(content);
    }

    @Override protected void onHide() {
        if (resolution != null) {
            callback.onResult(resolution);
        }
    }

    private EventListener listener(final GameMapState.ConflictResolution resolution) {
        return new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                ConflictWindow.this.resolution = resolution;
                hide();
            }
        };
    }

    public static class Params {
        private final UserData localData;
        private final UserData serverData;
        private final Callback callback;

        public Params(UserData localData, UserData serverData, Callback callback) {
            this.localData = localData;
            this.serverData = serverData;
            this.callback = callback;
        }
    }

    public static interface Callback {
        public void onResult(GameMapState.ConflictResolution conflictResolution);
    }

}
