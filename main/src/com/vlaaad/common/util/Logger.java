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

package com.vlaaad.common.util;

import com.badlogic.gdx.Gdx;

/**
 * Created 14.06.13 by vlaaad
 */
public class Logger {

    private static final String TAG = "com.vlaaad.dice";

    /**
     * Logs a message to the console or logcat
     */
    public static void log(Object message) {
        Gdx.app.log(TAG, String.valueOf(message));
    }

    /**
     * Logs a message to the console or logcat
     */
    public static void log(Object message, Exception exception) {
        Gdx.app.log(TAG, String.valueOf(message), exception);
    }

    /**
     * Logs an error message to the console or logcat
     */
    public static void error(Object message) {
        Gdx.app.error(TAG, String.valueOf(message));
    }

    /**
     * Logs an error message to the console or logcat
     */
    public static void error(Object message, Throwable exception) {
        Gdx.app.error(TAG, String.valueOf(message), exception);
    }

    /**
     * Logs a debug message to the console or logcat
     */
    public static void debug(Object message) {
        Gdx.app.debug(TAG, String.valueOf(message));
    }

    /**
     * Logs a debug message to the console or logcat
     */
    public static void debug(Object message, Throwable exception) {
        Gdx.app.debug(TAG, String.valueOf(message), exception);
    }
}
