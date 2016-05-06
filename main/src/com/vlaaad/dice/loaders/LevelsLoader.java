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

package com.vlaaad.dice.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.common.util.MapHelper;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.levels.BaseLevelDescription;
import com.vlaaad.dice.game.config.levels.PvpLevelDescription;
import com.vlaaad.dice.game.config.levels.LevelDescription;
import com.vlaaad.dice.game.config.levels.Levels;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 06.10.13 by vlaaad
 */
public class LevelsLoader extends AsynchronousAssetLoader<Levels, AssetLoaderParameters<Levels>> {
    public static final ObjectMap<String, Class<? extends BaseLevelDescription>> types = new ObjectMap<String, Class<? extends BaseLevelDescription>>();
    static {
        types.put("level", LevelDescription.class);
        types.put("pvp", PvpLevelDescription.class);
    }

    public LevelsLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    private Levels levels;

    @Override public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<Levels> parameter) {
        return null;
    }

    @Override @SuppressWarnings("unchecked") public void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Levels> parameter) {
        Yaml yaml = new Yaml();
        ObjectMap<String, BaseLevelDescription> data = new ObjectMap<String, BaseLevelDescription>();
        for (Object o : yaml.loadAll(resolve(fileName).read())) {
            HashMap<String, Object> value = (HashMap<String, Object>) o;
            String type = MapHelper.get(value, "type", "level");
            try {
                BaseLevelDescription desc = types.get(type).getConstructor(Map.class).newInstance(value);
                data.put(desc.name, desc);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        levels = new Levels(data);
        Config.levels = levels;
    }

    @Override public Levels loadSync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Levels> parameter) {
        return levels;
    }
}
