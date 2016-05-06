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

package com.vlaaad.dice.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.vlaaad.dice.DiceHeroes;
import com.vlaaad.dice.api.services.cloud.ICloudSave;
import com.vlaaad.dice.api.services.cloud.IConflictResolver;
import com.vlaaad.dice.api.services.cloud.IConflictResolverCallback;
import com.vlaaad.dice.game.user.UserData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Created 20.05.14 by vlaaad
 */
public class LocalCloudSave implements ICloudSave {

    public static final String SAVE_FILE_NAME = "dice.save";
    public static final String DIR = ".prefs/";
    public static final String CONFLICT_FILE_NAME = "conflict.save";

    @Override public void sync(UserData userData, IConflictResolver resolver) {
        FileHandle dir = Gdx.files.external(DIR);
        dir.mkdirs();
        final FileHandle saveFile = dir.child(SAVE_FILE_NAME);
        final FileHandle conflicted = dir.child(CONFLICT_FILE_NAME);
        if (saveFile.exists()) {
            DiceHeroes app = (DiceHeroes) Gdx.app.getApplicationListener();
            if (app.firstSession) {
                if (!conflicted.exists())
                    conflicted.write(saveFile.read(), false);
            }
        }
        toFile(userData, saveFile);
        if (conflicted.exists()) {
            final Map local = fromFile(saveFile);
            final Map server = fromFile(conflicted);
            resolver.resolveConflict(server, new IConflictResolverCallback() {
                @Override public void onResolved(boolean useLocal) {
                    conflicted.delete();
                    toFile(useLocal ? local : server, saveFile);
                }
            });
        }
    }

    private Map fromFile(FileHandle fileHandle) {
        return (Map) new Yaml().load(fileHandle.read());
    }

    private void toFile(UserData userData, FileHandle fileHandle) {
        toFile(UserData.serialize(userData), fileHandle);
    }

    private void toFile(Map data, FileHandle fileHandle) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        String toSave = new Yaml(options).dump(data);
        fileHandle.writeString(toSave, false);
    }
}
