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

package com.vlaaad.dice.ui.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.rewards.results.RewardResult;
import com.vlaaad.dice.game.config.rewards.results.RewardResult.AddedAbility;
import com.vlaaad.dice.game.config.rewards.results.RewardResult.AddedDie;
import com.vlaaad.dice.game.config.rewards.results.RewardResult.AddedItems;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.user.UserData;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.players.util.PlayerHelper;
import com.vlaaad.dice.ui.components.AbilityIcon;
import com.vlaaad.dice.ui.scene2d.LocLabel;

public class RewardViewHelper {

    public static final Color VICTORY_TEXT_COLOR = new Color(57 / 255f, 79 / 255f, 88 / 255f, 1);
    public static final Color UNIQUE_DARK = Color.valueOf("cf9b2e");

    public static Table createRewardsView(Iterable<RewardResult> rewards, ObjectIntMap<Item> earnedItems, UserData userData) {
        Table rewardList = new Table(Config.skin);
        rewardList.setTransform(true);
        rewardList.padTop(18).padBottom(14);

        Array<AddedDie> dieRewards = new Array<AddedDie>();
        Array<AddedAbility> abilityRewards = new Array<AddedAbility>();
        ObjectIntMap<Item> earned = new ObjectIntMap<Item>(earnedItems);

        for (RewardResult reward : rewards) {
            if (reward instanceof AddedItems) {
                final AddedItems addedItems = (AddedItems) reward;
                earned.getAndIncrement(addedItems.item, 0, addedItems.addedCount);
            } else if (reward instanceof AddedDie) {
                dieRewards.add(((AddedDie) reward));
            } else if (reward instanceof AddedAbility) {
                abilityRewards.add(((AddedAbility) reward));
            } else {
                throw new IllegalArgumentException("unknown reward: " + reward);
            }
        }

        if (earned.size > 0) {
            Table items = new Table();
            items.setTransform(true);
            Array<Item> sorted = earned.keys().toArray();
            sorted.sort(Item.ORDER_COMPARATOR);
            for (Item item : sorted) {
                Table rewardView = new Table(Config.skin);
                Image image = new Image(Config.skin, "item/" + item.name);
                Label counter = new Label(String.valueOf(earned.get(item, 0)), Config.skin, "default", VICTORY_TEXT_COLOR);
                counter.setFontScale(2f);
                rewardView.add(image).size(image.getPrefWidth() * 2, image.getPrefHeight() * 2).pad(-6);
                rewardView.add(counter).padTop(-5);
                items.add(rewardView);
            }
            rewardList.add(items).padTop(-10).row();
        }

        if (dieRewards.size > 0) {
            Label joins = new LocLabel("ui-reward-window-new-die", VICTORY_TEXT_COLOR);
            joins.setWrap(true);
            joins.setAlignment(Align.center);
            rewardList.add(joins).width(80).row();
            Table dice = new Table();
            dice.setTransform(true);
            for (AddedDie reward : dieRewards) {
                dice.add(ViewController.createView(new Creature(
                    reward.die,
                    PlayerHelper.defaultProtagonist
                )));
            }
            rewardList.add(dice).padTop(15).row();
        }

        if (abilityRewards.size > 0) {
            HorizontalGroup abilities = new HorizontalGroup();
            abilities.center().padTop(15).space(10).padLeft(-4);
            for (AddedAbility reward : abilityRewards) {
                final AbilityIcon icon = new AbilityIcon(reward.ability);
                icon.setColor(UNIQUE_DARK);
                abilities.addActor(new Container<AbilityIcon>(icon)
                    .size(icon.getPrefWidth() * 2, icon.getPrefHeight() * 2)
                    .pad(-12)
                );
                abilities.addActor(ViewController.createView(new Creature(reward.die, PlayerHelper.defaultProtagonist)));
            }

            final VerticalGroup group = new VerticalGroup();
            final Label label = new Label(
                Thesaurus.Util.enumerate(
                    Config.thesaurus,
                    abilityRewards,
                    new Thesaurus.Util.Stringifier<AddedAbility>() {
                        @Override public String toString(AddedAbility info) {
                            return Config.thesaurus.localize(
                                "die-gets-ability",
                                Thesaurus.params()
                                    .with("die", info.die.nameLocKey())
                                    .with("ability", info.ability.locNameKey() + ".acc")
                            );
                        }
                    }),
                Config.skin,
                "default",
                VICTORY_TEXT_COLOR
            );
            label.setAlignment(Align.center);
            group.addActor(label);
            group.addActor(abilities);
            rewardList.add(group).padTop(10).row();
        }
        return rewardList;
    }
}
