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

package com.vlaaad.dice.ui.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.world.view.Tile;

/**
 * Created 09.03.14 by vlaaad
 */
public class CraftingPane extends Table {
    private static final Vector2 tmp = new Vector2();
    private static final ObjectIntMap<Item> tmp2 = new ObjectIntMap<Item>();
    private static final ObjectIntMap<Item> tmp3 = new ObjectIntMap<Item>();

    private final int count;
    private final Array<Ability> recipes;
    private final ObjectMap<Actor, ItemIcon> inputs;
    private final Image output = new Image(Config.skin, "ui-crafting-slot");
    public final ObjectIntMap<Item> res = new ObjectIntMap<Item>();
    private AbilityIcon outputResult;


    public CraftingPane(int elementsCount, Array<Ability> recipes) {
        super(Config.skin);
        this.inputs = new ObjectMap<Actor, ItemIcon>(elementsCount);
        this.count = elementsCount;
        this.recipes = recipes;
//        defaults().pad(2);
        for (int i = 0; i < elementsCount; i++) {
            Image image = new Image(Config.skin, "ui-crafting-slot");
            image.setName("slot#" + i);
            inputs.put(image, null);
            add(image).size(26);
            if (i != elementsCount - 1) {
                add(new Tile("ui-plus")).pad(1);
            } else {
                add(new Tile("ui-equals")).pad(1);
            }
        }
        add(output).size(26);
        setTouchable(Touchable.enabled);
    }

    private void updateResult() {
        if (outputResult != null) {
            outputResult.remove();
            outputResult = null;
        }
        res.clear();
        for (ItemIcon i : inputs.values()) {
            if (i != null) {
                res.getAndIncrement(i.item, 0, 1);
            }
        }
        Ability ability = null;
        for (Ability check : recipes) {
            ObjectIntMap<Item> cost = check.ingredients;
            if (inputMatches(res, cost)) {
                ability = check;
                break;
            }
        }
        if (ability != null) {
            outputResult = new AbilityIcon(ability);
            addActor(outputResult);
            outputResult.setPosition(
                output.getX() + (output.getWidth() - outputResult.getWidth()) * 0.5f,
                output.getY() + (output.getHeight() - outputResult.getHeight()) * 0.5f
            );
        }
    }

    private boolean inputMatches(ObjectIntMap<Item> in, ObjectIntMap<Item> req) {
        ObjectIntMap<Item> cost = tmp2;
        cost.clear();
        cost.putAll(req);
        ObjectIntMap<Item> resources = tmp3;
        resources.clear();
        resources.putAll(in);
        for (Item item : resources.keys()) {
            int required = cost.get(item, 0);
            int existed = resources.get(item, 0);
            if (required > existed)
                return false;
            if (required == 0)
                continue;
            cost.remove(item, 0);
            resources.getAndIncrement(item, 0, -required);
        }
        if (cost.size == 0)
            return true;
        for (Item unsatisfied : cost.keys()) {
            if (unsatisfied.type != Item.Type.anyIngredient)
                return false;
            int required = cost.get(unsatisfied, 0);
            for (Item item : resources.keys()) {
                int withdrawCount = Math.min(resources.get(item, 0), required);
                if (withdrawCount == 0)
                    continue;
                required -= withdrawCount;
                resources.getAndIncrement(item, 0, -withdrawCount);
                cost.getAndIncrement(unsatisfied, 0, -withdrawCount);
                if (required == 0)
                    break;
            }
        }
        for (Item unsatisfied : cost.keys()) {
            if (cost.get(unsatisfied, 0) != 0)
                return false;
        }
        return true;
    }

    public boolean putIngredient(ItemIcon icon) {
        Vector2 stageCoordinates = icon.localToStageCoordinates(tmp.set(icon.getWidth() / 2, icon.getHeight() / 2));
        Vector2 local = this.stageToLocalCoordinates(stageCoordinates);
        Actor actor = hit(local.x, local.y, true);
        if (actor != null && inputs.containsKey(actor)) {
            ItemIcon added = new ItemIcon(icon.item);
            addActor(added);
            added.setTouchable(Touchable.disabled);
            added.setPosition(actor.getX() + (actor.getWidth() - added.getWidth()) / 2, actor.getY() + (actor.getHeight() - added.getHeight()) / 2f);
            ItemIcon prev = inputs.put(actor, added);
            if (prev != null) {
                prev.setTouchable(Touchable.enabled);
                prev.remove();
                fire(new IngredientReplaced(prev));
            }
            updateResult();
            return true;
        } else {
            return false;
        }
    }

    public ItemIcon removeIngredient(float stageX, float stageY) {
        Vector2 local = this.stageToLocalCoordinates(tmp.set(stageX, stageY));
        Actor actor = hit(local.x, local.y, true);
        if (actor != null && inputs.containsKey(actor)) {
            ItemIcon res = inputs.put(actor, null);
            if (res != null) {
                res.setTouchable(Touchable.enabled);
                res.remove();
            }
            updateResult();
            return res;
        }
        return null;
    }

    public boolean swapIngredients(ItemIcon icon, float stageX, float stageY) {
        if (icon == null)
            return false;
        Vector2 prevLocal = this.stageToLocalCoordinates(tmp.set(stageX, stageY));
        Actor ps = hit(prevLocal.x, prevLocal.y, true);

        if (ps == null || !inputs.containsKey(ps))
            return false;
        Vector2 stageCoordinates = icon.localToStageCoordinates(tmp.set(icon.getWidth() / 2, icon.getHeight() / 2));
        Vector2 newLocal = this.stageToLocalCoordinates(stageCoordinates);
        Actor ns = hit(newLocal.x, newLocal.y, true);

        if (ns == null || !inputs.containsKey(ns))
            return false;

        ItemIcon toNewSlot = new ItemIcon(icon.item);
        toNewSlot.setTouchable(Touchable.disabled);
        toNewSlot.setPosition(ns.getX() + (ns.getWidth() - toNewSlot.getWidth()) / 2, ns.getY() + (ns.getHeight() - toNewSlot.getHeight()) / 2f);
        addActor(toNewSlot);
        ItemIcon toPrevSlot = inputs.get(ns);
        inputs.put(ns, toNewSlot);
        if (toPrevSlot != null) {
            inputs.put(ps, toPrevSlot);
            toPrevSlot.setPosition(ps.getX() + (ps.getWidth() - toPrevSlot.getWidth()) / 2, ps.getY() + (ps.getHeight() - toPrevSlot.getHeight()) / 2f);
        }
        updateResult();
        return true;
    }

    public void clean() {
        Array<Actor> keys = inputs.keys().toArray();
        for (ItemIcon icon : inputs.values()) {
            if (icon != null) {
                icon.remove();
            }
        }
        for (Actor actor : keys) {
            inputs.put(actor, null);
        }
        updateResult();
    }

    public static class IngredientReplaced extends Event {
        private final ItemIcon icon;

        public IngredientReplaced(ItemIcon icon) {
            this.icon = icon;
        }
    }

    public abstract static class Listener implements EventListener {

        @Override public boolean handle(Event event) {
            if (event instanceof IngredientReplaced) {
                onIngredientReplaced(((IngredientReplaced) event).icon.item);
            }
            return false;
        }

        protected abstract void onIngredientReplaced(Item item);
    }
}
