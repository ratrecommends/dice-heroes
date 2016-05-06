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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.thesaurus.Thesaurus;
import com.vlaaad.dice.game.objects.Creature;
import com.vlaaad.dice.game.world.World;
import com.vlaaad.dice.game.world.WorldController;
import com.vlaaad.dice.game.world.events.EventListener;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.ui.components.ProfessionAbilityIcon;
import com.vlaaad.dice.ui.scene2d.LocLabel;
import com.vlaaad.dice.ui.windows.CreatureQueueWindow;

/**
 * Created 27.01.14 by vlaaad
 */
public class UiController extends WorldController {

    private final Table table = new Table(Config.skin);
    public final Table abilities = new Table(Config.skin);
    private final Container potions = new Container(new Actor());
    private final Label currentDie = new Label("", Config.skin);
    public final LocLabel nextDie = new LocLabel("");
    private final CreatureQueueWindow queueWindow = new CreatureQueueWindow();
    private final Table content;
    private boolean shouldUpdateNext;
    public final Button potionsButton = new Button(Config.skin, "game-potions");

    public UiController(World world) {
        super(world);

        abilities.defaults().padLeft(1).padRight(1).padTop(2);
        potions.padLeft(1).padRight(1).padTop(2).top().setFillParent(true);

        table.setFillParent(true);
        table.top();

        content = new Table(Config.skin);
        table.add(content).align(Align.top).expandX().fillX();

        currentDie.setAlignment(Align.center);

        content.left();
        content.defaults().padLeft(2).padRight(2);

        content.add(currentDie).padTop(-1).width(ViewController.CELL_SIZE);
        content.add(abilities);
//        content.add(potions);
        content.add(nextDie).align(Align.right).expandX().right();

        nextDie.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                showQueueWindow();
            }
        });
    }

    @Override protected void start() {
        if (world.viewer.hasPotions()) {
            potions.setActor(potionsButton);
        }
        world.dispatcher.add(RoundController.PRE_START, new EventListener<RoundController>() {
            @Override public void handle(EventType<RoundController> type, RoundController controller) {
                world.dispatcher.remove(RoundController.PRE_START, this);
                shouldUpdateNext = controller.queue.size > 2;
            }
        });
        world.stage.addActor(table);
        world.stage.addActor(potions);
        world.dispatcher.add(RoundController.TURN_STARTED, onTurn);
        world.dispatcher.add(RoundController.TURN_ENDED, onTurn);
        world.dispatcher.add(RoundController.PRE_START, onPreStart);
        world.dispatcher.add(RoundController.TURN_STARTED, onTurnStarted);
    }

    @Override protected void stop() {
        table.remove();
        potions.remove();
        world.dispatcher.remove(RoundController.TURN_STARTED, onTurn);
        world.dispatcher.remove(RoundController.TURN_ENDED, onTurn);
        world.dispatcher.remove(RoundController.PRE_START, onPreStart);
        world.dispatcher.remove(RoundController.TURN_STARTED, onTurnStarted);
    }

    private final ObjectMap<Ability, ProfessionAbilityIcon> abilityIconMap = new ObjectMap<Ability, ProfessionAbilityIcon>();

    public ProfessionAbilityIcon getProfessionIcon(Ability ability) {
        return abilityIconMap.get(ability);
    }

    private final EventListener<Creature> onTurn = new EventListener<Creature>() {
        @Override public void handle(EventType<Creature> type, Creature creature) {
            for (ProfessionAbilityIcon icon : abilityIconMap.values()) {
                abilityIconPool.free(icon);
            }
            abilities.clearChildren();
            abilityIconMap.clear();
            if (creature == null)
                currentDie.setText("");
            else
                currentDie.setText(Config.thesaurus.localize(creature.description.nameLocKey()));
            if (creature == null)
                return;
            Touchable touchable = creature.player == world.viewer ? Touchable.childrenOnly : Touchable.disabled;
            abilities.setTouchable(touchable);
            potions.setTouchable(touchable);
            potionsButton.setDisabled(touchable == Touchable.disabled || !creature.get(Attribute.canUsePotions) || MapHelper.isEmpty(world.viewer.potions));
            potionsButton.clearListeners();
            potionsButton.addListener(potionsButton.getClickListener());
            Array<Ability> professionAbilities = creature.description.profession.getAvailableAbilities(creature.getCurrentLevel());
            if (professionAbilities.size > 0) {
                for (Ability ability : professionAbilities) {
                    ProfessionAbilityIcon icon = getIcon(creature, ability);
                    icon.clearListeners();
                    if (creature.player != world.viewer || !ability.action.canBeApplied(creature)) {
                        icon.getColor().a = 0.5f;
                    }
                    abilityIconMap.put(ability, icon);
                    abilities.add(icon);
                }
            }
        }
    };

    private final EventListener<RoundController> onPreStart = new EventListener<RoundController>() {
        @Override public void handle(EventType<RoundController> type, RoundController controller) {
            updateNext();
        }
    };

    private final EventListener<Creature> onTurnStarted = new EventListener<Creature>() {
        @Override public void handle(EventType<Creature> type, Creature creature) {
            updateNext();
        }
    };

    private void updateNext() {
        if (!shouldUpdateNext)
            return;
        Creature next = world.getController(RoundController.class).getNextCreature();
        nextDie.setKey("ui-next-creature");
        nextDie.setParams(Thesaurus.params().with("die", next.description.nameLocKey()));
    }


    private void showQueueWindow() {
        RoundController roundController = world.getController(RoundController.class);
        Array<Creature> queue = new Array<Creature>(roundController.queue);
        translateQueue(queue, roundController.idx);
        queueWindow.show(queue);
    }

    private void translateQueue(Array<Creature> queue, int idx) {
        while (idx > 0) {
            queue.add(queue.removeIndex(0));
            idx--;
        }
    }

    private ProfessionAbilityIcon getIcon(Creature creature, Ability ability) {
        return abilityIconPool.obtain().set(creature, ability);
    }

    private final Pool<ProfessionAbilityIcon> abilityIconPool = new Pool<ProfessionAbilityIcon>() {
        @Override protected ProfessionAbilityIcon newObject() {
            return new ProfessionAbilityIcon();
        }
    };

    public void disableActionButtons() {
        abilities.setTouchable(Touchable.disabled);
        potions.setTouchable(Touchable.disabled);
    }
}
