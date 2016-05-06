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

package com.vlaaad.dice.game.objects;

import com.badlogic.gdx.utils.*;
import com.vlaaad.common.util.StringHelper;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.actions.results.IActionResult;
import com.vlaaad.dice.game.config.abilities.Ability;
import com.vlaaad.dice.game.config.attributes.Attribute;
import com.vlaaad.dice.game.config.attributes.modifiers.AttributeModifier;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.effects.CreatureEffect;
import com.vlaaad.dice.game.objects.events.EffectEvent;
import com.vlaaad.dice.game.user.Die;
import com.vlaaad.dice.game.world.controllers.RandomController;
import com.vlaaad.dice.game.world.controllers.ViewController;
import com.vlaaad.dice.game.world.events.EventType;
import com.vlaaad.dice.game.world.players.Player;
import com.vlaaad.dice.game.world.players.PlayerRelation;
import com.vlaaad.dice.game.world.players.util.PlayerColors;
import com.vlaaad.dice.game.world.view.*;

import java.util.Iterator;

/**
 * Created 06.10.13 by vlaaad
 */
public class Creature extends WorldObject implements Iterable<Ability> {
    private static int nextId;

    public static final EventType<EffectEvent> ADD_EFFECT = new EventType<EffectEvent>();
    public static final EventType<EffectEvent> REMOVE_EFFECT = new EventType<EffectEvent>();

    public boolean isKilled() {
        return killed;
    }

    public static enum CreatureRelation {
        any, ally, enemy, self, allyExceptSelf, anyExceptSelf, none
    }

    public boolean inRelation(CreatureRelation relation, Creature that) {
        switch (relation) {
            case none:
                return false;
            case any:
                return true;
            case ally:
                return this.player.inRelation(that.player, PlayerRelation.allyOrSelf);
            case enemy:
                return this.player.inRelation(that.player, PlayerRelation.enemy);
            case self:
                return this == that;
            case allyExceptSelf:
                return this.player.inRelation(that.player, PlayerRelation.allyOrSelf) && this != that;
            case anyExceptSelf:
                return this != that;
            default:
                throw new RuntimeException("unknown relation: " + relation);
        }
    }

    public final Player initialPlayer;
    public Player player;
    public Player viewer;

    public final String name;
    public final ProfessionDescription profession;
    public final Die description;

    public final Array<Ability> abilities = new Array<Ability>();
    public final FloatArray probabilities = new FloatArray();
    public final SnapshotArray<CreatureEffect> effects = new SnapshotArray<CreatureEffect>(CreatureEffect.class);

    private final ObjectMap<Attribute, Object> attributes = new ObjectMap<Attribute, Object>();
    private final ObjectMap<Attribute, Array<AttributeModifier>> modifiers = new ObjectMap<Attribute, Array<AttributeModifier>>();

    public int gainedExp;
    private int currentIndex = 0;
    private boolean killed;
    public final ObjectIntMap<Item> drop = new ObjectIntMap<Item>(0);
    public final String id;

    public Creature(Die description, Player player, String id) {
        super(description.profession.name);
        set(Attribute.defaultActionPoints, description.profession.defaultActionPoints);
        set(Attribute.actionPoints, description.profession.defaultActionPoints);
        for (ObjectMap.Entry<Attribute, Object> entry : description.profession.attributes) {
            set(entry.key, entry.value);
        }
        this.initialPlayer = player;
        this.description = description;
        this.profession = description.profession;
        this.player = player;
        this.name = description.name;
        this.id = id;
        for (Ability ability : description.abilities) {
            addAbility(ability);
        }
        while (abilities.size < 6) {
            addAbility("skip-turn");
        }
        this.viewPriority = 1;
    }

    public Creature(Die description, Player player) {
        this(description, player, player.fraction + "@" + (nextId++));
    }

    // -------------------- ATTRIBUTES -------------------- //

    @SuppressWarnings("unchecked")
    public <T> T get(Attribute<T> attribute) {
        Object v = attributes.get(attribute);
        T value = v == null ? attribute.defaultValue : (T) v;
        Array<AttributeModifier<T>> attributeModifiers = (Array<AttributeModifier<T>>) (Object) modifiers.get(attribute);
        if (attributeModifiers == null)
            return value;
        for (AttributeModifier<T> modifier : attributeModifiers) {
            value = modifier.apply(value);
        }
        return value;
    }

    public <T> void set(Attribute<T> attribute, T value) {
        attributes.put(attribute, value);
    }


    // -------------------- ATTRIBUTE MODIFIERS -------------------- //

    @SuppressWarnings("unchecked")
    public <T> void addModifier(Attribute<T> attribute, AttributeModifier<T> modifier) {
        Array<AttributeModifier> o = modifiers.get(attribute);
        Array<AttributeModifier<T>> list;
        if (o != null) {
            list = (Array<AttributeModifier<T>>) (Object) o;
        } else {
            list = new Array<AttributeModifier<T>>();
            modifiers.put(attribute, (Array<AttributeModifier>) (Object) list);
        }
        list.add(modifier);
        ((Array<AttributeModifier>) (Object) list).sort(AttributeModifier.COMPARATOR);
    }

    @SuppressWarnings("unchecked")
    public <T> void removeModifier(Attribute<T> attribute, AttributeModifier<T> modifier) {
        Array<AttributeModifier> o = modifiers.get(attribute);
        Array<AttributeModifier<T>> list;
        if (o == null) {
            return;
        }
        list = (Array<AttributeModifier<T>>) (Object) o;
        list.removeValue(modifier, true);
    }

    // -------------------- ABILITIES -------------------- //

    private void addAbility(String name) {
        addAbility(Config.abilities.get(name));
    }

    private void addAbility(Ability ability) {
        if (ability == null) {
            ability = Config.abilities.get("skip-turn");
        }
        if (abilities.size >= 6)
            throw new IllegalStateException("too much abilities!");
        if (!description.profession.ignoreRequirements && !ability.requirement.isSatisfied(description))
            throw new IllegalArgumentException("ability requirement " + ability.requirement + " not satisfied: " + description);
        abilities.add(ability);
        probabilities.add(1f);
    }

    public Ability rollAbility() {
        float total = 0f;
        for (int i = 0; i < probabilities.size; i++) {
            total += probabilities.get(i);
        }
        float random = world.getController(RandomController.class).random(total);
        int idx;
        for (idx = 0; idx < probabilities.size; idx++) {
            random -= probabilities.get(idx);
            // warning! do not use "<=" here
            // we need it in SetCurrentRolled.java to force next rolled ability.
            // random is zero inclusive, so if we rolled zero,
            // and needed ability is not first,
            // we can fail.
            if (random < 0)
                break;
        }
        for (int i = 0; i < probabilities.size; i++) {
            if (i == idx) {
                probabilities.set(i, 1f);
            } else {
                probabilities.set(i, probabilities.get(i) + 4f);
            }
        }
        currentIndex = idx;
        return abilities.get(idx);
    }

    @Override
    public void onAdded() {
        viewer = world.viewer;
        world.creaturesById.put(id, this);
    }

    @Override
    public void afterAdded() {
        if (profession.applyOnCreate.isDefined()) {
            profession.applyOnCreate.get().action.apply(this, world).addListener(new IFutureListener<IActionResult>() {
                @Override
                public void onHappened(final IActionResult result) {
                    world.getController(ViewController.class).visualize(result).addListener(new IFutureListener<Void>() {
                        @Override
                        public void onHappened(Void aVoid) {
                            result.apply(world);
                        }
                    });
                }
            });
        }
    }

    public int getCurrentAbilityIndex() {
        return currentIndex;
    }


    public Ability getCurrentAbility() {
        return abilities.get(currentIndex);
    }

    // -------------------- EFFECTS -------------------- //

    public boolean skipsTurn() {
        return get(Attribute.frozen) || get(Attribute.transformedToObstacle);
    }

    private final Future<Void> updateEffectsFuture = new Future<Void>();

    public boolean hasEffect(Class<? extends CreatureEffect> effectClass) {
        for (CreatureEffect effect : effects) {
            if (effect.is(effectClass))
                return true;
        }
        return false;
    }

    public IFuture<Void> updateEffects(boolean all) {
        updateEffectsFuture.reset();
        CreatureEffect[] items = effects.begin();
        int i = 0;
        int n = effects.size;
        updateEffect(i, n, items, updateEffectsFuture, all);
        return updateEffectsFuture;
    }

    private void updateEffect(final int updateIndex, final int size, final CreatureEffect[] effects, final Future<Void> future, final boolean all) {
        if (updateIndex >= size) {
            this.effects.end();
            future.happen();
            return;
        }
        final CreatureEffect effect = effects[updateIndex];
        if (!all && effect.getType() != CreatureEffect.EffectType.util) {
            updateEffect(updateIndex + 1, size, effects, future, all);
            return;
        }
        effect.decreaseTurnCount();
        if (effect.shouldEnd()) {
            this.effects.removeValue(effect, true);
            IFuture<Void> removeFuture = effect.remove(this);
            if (world != null)
                world.dispatcher.dispatch(REMOVE_EFFECT, new EffectEvent(Creature.this, effect));
            if (removeFuture != null) {
                removeFuture.addListener(new IFutureListener<Void>() {
                    @Override
                    public void onHappened(Void aVoid) {
                        updateEffect(updateIndex + 1, size, effects, future, all);
                    }
                });
                return;
            }
        }
        updateEffect(updateIndex + 1, size, effects, future, all);
    }

    public void onResurrected() {
        if (!killed) {
            com.vlaaad.common.util.Logger.error(this + " is not killed, so can't be resurrected!");
        }
        killed = false;
    }

    public void onKilled() {
        if (killed)
            return;
        killed = true;
        CreatureEffect[] items = effects.begin();
        for (int i = 0, n = effects.size; i < n; i++) {
            CreatureEffect effect = items[i];
            if (effect.isRemovedOnDeath() && effects.removeValue(effect, true)) {
                effect.remove(this);
                if (world != null) world.dispatcher.dispatch(REMOVE_EFFECT, new EffectEvent(this, effect));
            }
        }
        effects.end();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
    }

    public IFuture<Void> removeEffect(CreatureEffect effect) {
        IFuture<Void> result = null;
        if (effects.removeValue(effect, true)) {
            result = effect.remove(this);
            if (world != null)
                world.dispatcher.dispatch(REMOVE_EFFECT, new EffectEvent(this, effect));

        }
        return result;
    }

    public IFuture<Void> removeEffect(String group) {
        for (CreatureEffect effect : effects) {
            if (effect.effectGroup.equals(group)) {
                return removeEffect(effect);
            }
        }
        return Future.completed();
    }

    public IFuture<Void> removeEffect(Class<? extends CreatureEffect> effectClass) {
        for (CreatureEffect effect : effects) {
            if (effect.is(effectClass)) {
                return removeEffect(effect);
            }
        }
        return Future.completed();
    }

    public void addEffect(CreatureEffect effect) {
        if (effects.contains(effect, true))
            throw new IllegalArgumentException("effect already added");
        Iterator<CreatureEffect> it = effects.iterator();
        while (it.hasNext()) {
            CreatureEffect e = it.next();
            if (e.effectGroup.equals(effect.effectGroup)) {
                it.remove();
                e.remove(this);
                if (world != null)
                    world.dispatcher.dispatch(REMOVE_EFFECT, new EffectEvent(this, e));
            }
        }
        effects.add(effect);
        effect.apply(this);
        if (world != null)
            world.dispatcher.dispatch(ADD_EFFECT, new EffectEvent(this, effect));
    }

    // -------------------- VIEW -------------------- //

    @Override
    public ArrayMap<Object, SubView> createSubViews(Player viewer, PlayerColors colors) {
        ArrayMap<Object, SubView> result = new ArrayMap<Object, SubView>();
        Array<String> frames = new Array<String>(6);
        for (Ability description : abilities) {
            frames.add(description.name);
        }
        Ability main = abilities.first();
        for (int i = 1; i < abilities.size; i++) {
            Ability check = abilities.get(i);
            if (check.cost > main.cost) {
                main = check;
            }
        }
        result.put(this, new DieSubView(this, viewer, 2));
        result.put(Ability.class, new AbilitiesSubView(main.name, frames));
        result.put("name", new NameSubView(this, viewer, colors, description.nameLocKey()));
        result.put(CreatureEffect.class, new EffectsSubView(this));
        return result;
    }

    // --------------------- EXP --------------------- //

    public void addExp(int exp) {
        gainedExp += exp;
    }

    public int getCurrentLevel() {
        return description.profession.getLevel(description.exp + gainedExp);
    }

    public int getCurrentExp() {
        return description.exp + gainedExp;
    }

    // -------------------- OTHER -------------------- //

    @Override
    public String toString() {
        return profession + " " + description.name + ": " + StringHelper.toCamelCase(player.toString()) + " at " + getX() + ", " + getY();
    }

    @Override
    public Iterator<Ability> iterator() {
        return abilities.iterator();
    }


    public Array<Ability> getAvailableAbilities() {
        Array<Ability> res = new Array<Ability>();
        for (Ability ability : profession.getAvailableAbilities(getCurrentLevel())) {
            if (ability.action.canBeApplied(this)) {
                res.add(ability);
            }
        }
        return res;
    }

    public void setDrop(ObjectIntMap<Item> drop) {
        this.drop.clear();
        if (drop == null) {
            return;
        }
        for (Item item : drop.keys()) {
            int count = drop.get(item, 0);
            if (count > 0) {
                this.drop.put(item, count);
            }
        }
    }
}
