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

package com.vlaaad.dice;

import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.vlaaad.dice.services.LocalMultiplayer;

import java.io.IOException;

/**
 * Created 06.10.13 by vlaaad
 */
public class DesktopLauncher {
    public static void main(String[] args) throws IOException {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        //min
        config.width = 320 * 2;
        config.height = 426 * 2;

        //avg
//        config.width = 360;
//        config.height = 640;

        //max
//        config.width = 800;
//        config.height = 1280;

        // 7 inch tablet
//        config.width = 1200 / 2;
//        config.height = 1920 / 2;

        //nexus 10
//        config.width = 1600 / 2;
//        config.height = 2560 / 2;

//        config.width = 640;
//        config.height = 800;

//        config.width = 800;
//        config.height = 800;

        config.title = "dice heroes";
        config.resizable = false;
        new LwjglApplication(new DiceHeroes(new LocalMobileApi()), config);
    }
}
