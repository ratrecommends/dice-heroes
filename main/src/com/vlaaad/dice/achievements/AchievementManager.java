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

package com.vlaaad.dice.achievements;

import com.badlogic.gdx.utils.*;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.DiceHeroes;
import com.vlaaad.dice.achievements.conditions.AchievementCondition;
import com.vlaaad.dice.achievements.conditions.imp.IncrementalCondition;
import com.vlaaad.dice.api.services.achievements.IGameAchievements;
import com.vlaaad.dice.achievements.conditions.AchievementConditionFactory;
import com.vlaaad.dice.achievements.events.Event;
import com.vlaaad.dice.achievements.events.EventType;
import com.vlaaad.dice.game.user.UserData;

import java.util.Iterator;
import java.util.Map;

/**
 * Created 15.05.14 by vlaaad
 */
public class AchievementManager implements Iterable<Achievement> {

//    private static final ObjectSet<EventType> tmp = new ObjectSet<EventType>();

    private final DiceHeroes app;
    private final UserData userData;
    private final ObjectMap<String, Achievement> achievements = new ObjectMap<String, Achievement>();
    private IGameAchievements visualizer = IGameAchievements.NONE;
    private final ObjectMap<EventType, DelayedRemovalArray<Achievement>> listeners = new ObjectMap<EventType, DelayedRemovalArray<Achievement>>();
    private final ObjectMap<Achievement, IncrementalCondition> incs = new ObjectMap<Achievement, IncrementalCondition>();

    @SuppressWarnings("unchecked")
    public AchievementManager(DiceHeroes app, UserData userData) {
        this.app = app;
        this.userData = userData;
        load(Config.assetManager.get("achievements.yml", Array.class));
    }

    @SuppressWarnings("unchecked")
    public void load(Array<Map> raw) {
        int order = 0;
        for (Map map : raw) {
            String name = MapHelper.get(map, "name");
            String id = MapHelper.get(map, "id");
            Iterable<String> events = MapHelper.get(map, "events");
            ObjectSet<EventType> eventTypes = new ObjectSet<EventType>();
            for (String eventName : events) {
                eventTypes.add(EventType.valueOf(eventName));
            }
            Achievement achievement = new Achievement(name, id, eventTypes, AchievementConditionFactory.create(map), order);
            Object o = userData.achievements.get(name);
            if (o != null) {
                achievement.load(o);
            }
            Array<AchievementCondition> conditions = achievement.condition.fill();
            for (AchievementCondition condition : conditions) {
                if (condition instanceof IncrementalCondition) {
                    incs.put(achievement, ((IncrementalCondition) condition));
                    break;
                }
            }
            achievements.put(achievement.name, achievement);
            if (!achievement.isUnlocked()) {
                for (EventType type : achievement.eventTypes) {
                    DelayedRemovalArray<Achievement> bucket = listeners.get(type);
                    if (bucket == null) {
                        bucket = new DelayedRemovalArray<Achievement>(false, 4);
                        listeners.put(type, bucket);
                    }
                    bucket.add(achievement);
                }
            }
            order++;
        }
    }

    public Achievement get(String name) {
        return achievements.get(name);
    }


    public <T extends Event> void fire(EventType<T> type) {
        fire(type, Pools.obtain(type.eventClass));
    }

    public <T extends Event> void fire(EventType<T> type, T event) {
        event.type(type).app(app).userData(userData);
        DelayedRemovalArray<Achievement> bucket = listeners.get(event.type());
        if (bucket == null) {
            Pools.free(event);
            return;
        }
        boolean hasRevealed = false;
        bucket.begin();
        for (Achievement achievement : bucket) {
            IncrementalCondition inc = incs.get(achievement);
            if (achievement.condition.isSatisfied(event)) {
                achievement.unlock();
                hasRevealed = true;
                for (EventType t : achievement.eventTypes) {
                    listeners.get(t).removeValue(achievement, true);
                }
                if (inc != null) visualizer.setCount(achievement, inc.getTotal());
                if (visualizer != null) visualizer.unlock(achievement);
            } else if (visualizer != null) {
                if (inc != null && inc.getCurrent() > 0) {
                    visualizer.setCount(achievement, inc.getCurrent());
                }
            }
        }
        bucket.end();
        if (bucket.size == 0) listeners.remove(event.type());
        Pools.free(event);
        if (hasRevealed) app.save();
    }

    public void save() {
        for (Achievement achievement : achievements.values()) {
            Object saved = achievement.save();
            if (saved != null) userData.achievements.put(achievement.name, achievement.save());
        }
    }

    public IGameAchievements visualizer() {
        return visualizer;
    }

    public void visualizer(IGameAchievements visualizer) {
        if (visualizer == null) visualizer = IGameAchievements.NONE;
        this.visualizer = visualizer;
        visualizer.start(achievements.values().toArray());
        for (ObjectMap.Entry<Achievement, IncrementalCondition> e : incs) {
            if (e.value.getCurrent() > 0) {
                visualizer.setCount(e.key, e.value.getCurrent());
            }
        }
    }

    @Override public Iterator<Achievement> iterator() {
        return achievements.values();
    }
}
