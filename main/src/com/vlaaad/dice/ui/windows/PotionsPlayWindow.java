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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.ui.GameWindow;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.CreatureAction;
import com.vlaaad.dice.game.actions.imp.Potion;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.view.Tile;
import com.vlaaad.dice.ui.components.AbilityIconCounter;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.scene2d.LocTextButton;

/**
 * Created 12.03.14 by vlaaad
 */
public class PotionsPlayWindow extends GameWindow<PotionsPlayWindow.Params> {
    private static Vector2 tmp = new Vector2();
    private static Thesaurus.Params params = Thesaurus.params();
    private Potion.ActionType action;
    private Ability selected;
    private Params windowParams;

    @Override protected void doShow(Params params) {
        windowParams = params;
        final Table content = new Table(Config.skin);
        content.defaults().pad(2);
        content.setBackground("ui-creature-info-background");
        content.setTouchable(Touchable.enabled);
        Container container = new Container(content).top();
        table.add(container).size(162, 218);

        Array<Ability> potions = new Array<Ability>();
        for (Ability ability : params.creature.player.potions()) {
            if (params.creature.player.getPotionCount(ability) > 0)
                potions.add(ability);
        }
        potions.sort(Ability.COST_COMPARATOR);

        Table viewsList = new Table();
        Image selection = new Image(Config.skin.getDrawable("selection/turn"));
        viewsList.addActor(selection);

        LocLabel name = new LocLabel("");
        LocLabel desc = new LocLabel("");
        desc.setAlignment(Align.center /*| Align.top*/);
        desc.setWrap(true);

        Table buttons = new Table();
        buttons.top().defaults().pad(2);

        Table viewRow = new Table();
        viewsList.add(viewRow).row();
        viewRow.defaults().pad(2);
        int i = 0;
        ActorGestureListener l = null;
        for (Ability ability : potions) {
            AbilityIconCounter icon = new AbilityIconCounter(ability, params.creature.player.getPotionCount(ability));
            icon.image.setOrigin(icon.image.getWidth() / 2, icon.image.getHeight() / 2);
            ActorGestureListener listener = createPotionTapListener(icon, ability, selection, viewsList, name, desc, buttons);
            if (l == null) {
                l = listener;
            }
            icon.addListener(listener);
            i++;
            viewRow.add(icon);
            if (i % 5 == 0) {
                viewRow = new Table();
                viewRow.defaults().pad(2);
                viewsList.add(viewRow).row();
            }
        }

        content.add(viewsList).row();
        content.add(new Tile("ui-creature-info-line")).size(100, 1).row();
        content.add(name).padBottom(0).padTop(-1).row();
//        Tile line = new Tile("ui-creature-info-line");
//        line.setColor(1, 1, 1, 0.5f);
//        content.add(line).padBottom(0).padTop(4).size(50, 1).row();
        content.add(desc).size(130, 44).padTop(0).row();
//        content.add(new Tile("ui-creature-info-line")).size(100, 1).row();
        content.add(buttons)/*.height(68)*/.row();

        if (l != null) {
            table.invalidate();
            table.validate();
            l.tap(null, 0, 0, 0, 0);
        }
        getStage().addActor(new Actor() {
            float x = -1;
            float y = -1;

            @Override public void act(float delta) {
                if (content.getWidth() != x || content.getHeight() != y) {
                    x = content.getWidth();
                    y = content.getHeight();
                }
            }
        });
    }

    @Override protected void onHide() {
        if (selected != null) {
            windowParams.callback.usePotion(selected, action);
        }
        selected = null;
    }

    private ActorGestureListener createPotionTapListener(final AbilityIconCounter icon, final Ability potion, final Image selection, final Table viewsList, final LocLabel name, final LocLabel desc, final Table buttons) {
        return new ActorGestureListener() {
            @Override public void tap(InputEvent event, float x, float y, int tapCount, int button) {
                Vector2 pos = icon.localToAscendantCoordinates(viewsList, tmp.set(0, 0));
                selection.setPosition(pos.x, pos.y);
                name.setKey(potion.locNameKey());
                desc.setKey(potion.locDescKey());
                desc.setParams(potion.fillDescriptionParams(params, null));
                buttons.clearChildren();
                if (potion.action instanceof Potion) {
                    Potion potionAction = (Potion) potion.action;
                    if (potionAction.drink != null) {
                        TextButton drinkButton = new LocTextButton("ui-drink-potion");
                        drinkButton.addListener(new ChangeListener() {
                            @Override public void changed(ChangeEvent event, Actor actor) {
                                action = Potion.ActionType.drink;
                                selected = potion;
                                selected = potion;
                                hide();
                            }
                        });
                        buttons.add(drinkButton).width(100).row();
                        checkCanUse(drinkButton, buttons, potionAction.drink);

                    }
                    if (potionAction.throwToCreature != null) {
                        TextButton throwButton = new LocTextButton("ui-throw-potion");
                        throwButton.addListener(new ChangeListener() {
                            @Override public void changed(ChangeEvent event, Actor actor) {
                                selected = potion;
                                action = Potion.ActionType.throwToCreature;
                                hide();
                            }
                        });
                        buttons.add(throwButton).width(100).row();
                        checkCanUse(throwButton, buttons, potionAction.throwToCreature);
                    }
                } else {
                    TextButton use = new LocTextButton("ui-use-ability");
                    use.addListener(new ChangeListener() {
                        @Override public void changed(ChangeEvent event, Actor actor) {
                            selected = potion;
                            hide();
                        }
                    });
                    buttons.add(use).width(100).row();
                    checkCanUse(use, buttons, potion.action);
                }
            }
        };
    }

    private void checkCanUse(TextButton button, Table buttons, CreatureAction action) {
        Thesaurus.LocalizationData data = Thesaurus.data();
        if (!action.canBeApplied(windowParams.creature, data)) {
            Label label = new LocLabel(data, StoreWindow.INACTIVE);
            label.setWrap(true);
            label.setAlignment(Align.center);
            button.setDisabled(true);
            buttons.add(label).pad(2).width(120).padTop(-2).row();
        }
    }

    public static class Params {
        private final Creature creature;
        private final World world;
        private final Callback callback;

        public Params(Creature creature, World world, Callback callback) {
            this.creature = creature;
            this.world = world;
            this.callback = callback;
        }
    }

    public static interface Callback {
        void usePotion(Ability potion, Potion.ActionType action);
    }
}
