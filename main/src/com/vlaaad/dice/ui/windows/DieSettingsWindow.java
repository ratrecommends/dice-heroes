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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Scaling;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.config.thesaurus.ThesaurusData;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.components.RefreshEvent;
import com.vlaaad.dice.ui.components.StringSpin;
import com.vlaaad.dice.ui.scene2d.LocLabel;

/**
 * Created 25.08.14 by vlaaad
 */
public class DieSettingsWindow extends GameWindow<DieSettingsWindow.Params> {

    private final Group diceWindowGroup;

    public DieSettingsWindow(Group diceWindowGroup) {
        this.diceWindowGroup = diceWindowGroup;
    }

    @Override protected void doShow(final Params params) {
        Table content = new Table(Config.skin);
        content.setBackground("ui-store-window-background");
        content.defaults().pad(4);
        content.setTouchable(Touchable.enabled);


        final LocLabel infoLabel = new LocLabel(
            "ui-renames-left",
            Thesaurus.params()
                .with("count", String.valueOf(params.die.renames))
                .with("free-renames", "free-renames." + Thesaurus.Util.countForm(params.die.renames))
            ,
            StoreWindow.INACTIVE);
        infoLabel.setWrap(true);
        infoLabel.setAlignment(Align.center);

        final StringSpin nameSpin = new StringSpin(4, 10, Config.thesaurus.localize(params.die.nameLocKey()));
        Container inner = new Container(nameSpin);
        inner.padTop(3);
        final Container nameContainer = new Container(inner);
        nameContainer.setBackground(Config.skin.getDrawable("ui/button/name-change-background"));
        final Button changeNameButton = new Button(Config.skin);
        final Thesaurus thesaurus = Config.assetManager.get("names.yml", Thesaurus.class);
        final Array<ThesaurusData> values = thesaurus.values();
        final Item coin = Config.items.get("coin");
        updateChangeNameButton(changeNameButton, params);
        changeNameButton.addListener(new ChangeListener() {
            @SuppressWarnings("unchecked")
            @Override public void changed(ChangeEvent event, Actor actor) {
                ObjectSet<ThesaurusData> existingNames = Pools.obtain(ObjectSet.class);
                for (Die die : params.userData.dice()) {
                    existingNames.add(thesaurus.getData(die.name.toLowerCase()));
                }
                ThesaurusData newName = values.random();
                while (existingNames.contains(newName)) {
                    newName = values.random();
                }
                existingNames.clear();
                Pools.free(existingNames);
                final ThesaurusData chosenName = newName;
                changeNameButton.setDisabled(true);
                nameSpin.setText(Config.thesaurus.localize(newName.key), new Runnable() {
                    @Override public void run() {
                        params.die.name = chosenName.key;
                        if (params.die.renames > 0) {
                            params.die.renames -= 1;
                            infoLabel.setParams(Thesaurus.params()
                                    .with("count", String.valueOf(params.die.renames))
                                    .with("free-renames", "free-renames." + Thesaurus.Util.countForm(params.die.renames))
                            );
                        } else {
                            params.userData.incrementItemCount(coin, -1);
                        }
                        updateChangeNameButton(changeNameButton, params);
                        fire(RefreshEvent.INSTANCE);
                    }
                });
            }
        });

        content.add(new LocLabel("ui-select-die-name")).row();
        content.add(new Tile("ui-creature-info-line")).width(80).padTop(0).row();
        content.add(nameContainer).size(70, 21).row();
        content.add(changeNameButton).size(100, 21).padBottom(0).row();
        content.add(infoLabel).width(110).row();

        table.add(content);
    }
    private void updateChangeNameButton(Button button, Params params) {
        final Item coin = Config.items.get("coin");
        button.setDisabled(params.die.renames == 0 && params.userData.getItemCount(coin) == 0);
        button.clearChildren();
        if (params.die.renames == 0) {
            button.add(new LocLabel("ui-change-name-for")).padLeft(4);
            Image image = new Image(Config.skin, "item/coin");
            image.setScaling(Scaling.none);
            button.add(image).padTop(0).padBottom(-4);
            button.add("1").padRight(4);
        } else {
            button.add(new LocLabel("ui-change-name"));
        }
    }

    public static class Params {
        private final Die die;
        private final UserData userData;
        public Params(Die die, UserData userData) {
            this.die = die;
            this.userData = userData;
        }
    }


    @Override public Group getTargetParent() {
        return diceWindowGroup;
    }

    @Override public boolean useParentSize() {
        return true;
    }
}
