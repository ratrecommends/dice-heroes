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

package com.vlaaad.common.tutorial;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;

/**
 * Created 07.11.13 by vlaaad
 */
public class Tutorial {

    private static final ObjectSet<Tutorial> runningTutorials = new ObjectSet<Tutorial>();
    private static final Array<Runnable> runnables = new Array<Runnable>();

    public static void cancel(String tutorialName) {
        for (Tutorial tutorial : runningTutorials) {
            if (tutorial.name.equals(tutorialName)) {
                tutorial.cancel();
                break;
            }
        }
    }

    public static void killAll() {
        if (runningTutorials.size == 0)
            return;
        ObjectSet<Tutorial> tuts = new ObjectSet<Tutorial>(runningTutorials);
        for (Tutorial tutorial : tuts) {
            try {
                tutorial.cancel();
            } catch (UnsupportedOperationException e) {
                Logger.debug("failed to cancel tutorial", e);
            }
        }
    }

    public static void whenAllTutorialsEnded(Runnable runnable) {
        if (runningTutorials.size == 0) {
            runnable.run();
        } else {
            runnables.add(runnable);
        }
    }

    public static boolean hasRunningTutorials() {
        return runningTutorials.size > 0;
    }

    private static void checkTutorials() {
//        Logger.debug("running tutorials: \n - " + runningTutorials.toString("\n - "));
        while (runningTutorials.size == 0 && runnables.size > 0) {
            runnables.removeIndex(0).run();
        }
    }


    protected final TutorialResources resources;
    private final String name;
    protected final Array<TutorialTask> tasks;
    protected final TutorialTask.Callback callback = new TutorialTask.Callback() {
        @Override public void taskEnded() {
            start();
        }
    };
    private TutorialTask currentTask;
    private Future<Tutorial> future = new Future<Tutorial>();

    public Tutorial(TutorialResources resources, Array<TutorialTask> tasks) {
        this("unnamed", resources, tasks);
    }

    public Tutorial(String name, TutorialResources resources, Array<TutorialTask> tasks) {
        this.name = name;
        this.resources = resources;
        this.tasks = tasks;
    }

    /**
     * @return future of successfully ended tutorial
     */
    public IFuture<Tutorial> start() {
        runningTutorials.add(this);
        if (tasks.size == 0) {
            runningTutorials.remove(this);
            checkTutorials();
//            Logger.log("end tutorial " + Integer.toHexString(hashCode()));
            if (currentTask != null) {
                future.happen(this);
            }
            currentTask = null;
            return future;
        }
        currentTask = tasks.removeIndex(0);
        currentTask.initialize(this, resources);
//        Logger.log("start tutorial task " + currentTask.getClass().getSimpleName());
        currentTask.start(callback);
        return future;
    }

    public void cancel() {
        runningTutorials.remove(this);
        checkTutorials();
        if (currentTask != null) {
            currentTask.cancel();
        }
        tasks.clear();
    }

    // ------------ helper methods ------------ //


    public static TutorialResources resources() {
        return new TutorialResources();
    }

    public static Tasks tasks() {
        return new Tasks();
    }

    // ------------ helper classes ------------ //

    public static class Tasks extends Array<TutorialTask> {
        public Tasks with(TutorialTask task) {
            add(task);
            return this;
        }
    }

    public static class TutorialResources {
        private final ObjectMap<String, Object> data = new ObjectMap<String, Object>();

        public TutorialResources with(String key, Object value) {
            data.put(key, value);
            return this;
        }

        public TutorialResources put(String key, Object value) {
            return with(key, value);
        }


        public <T> T get(String key) {
            return get(key, null);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key, T defaultValue) {
            Object result = data.get(key);
            if (result == null)
                result = defaultValue;
            if (result == null)
                throw new IllegalStateException("resource request not satisfied: " + key);
            return (T) result;
        }

        public <T> T getIfExists(String key) {
            return has(key) ? this.<T>get(key) : null;
        }

        public boolean has(String key) {
            return data.containsKey(key);
        }

        @SuppressWarnings("unchecked")
        public <T> T remove(String key) {
            Object result = data.remove(key);
            if (result == null)
                throw new IllegalStateException("resource request not satisfied: " + key);
            return (T) result;
        }
    }
}
