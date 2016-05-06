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
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.abilities.Abilities;
import com.vlaaad.dice.game.config.abilities.Ability;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;

/**
 * Created 06.10.13 by vlaaad
 */
public class AbilitiesLoader extends AsynchronousAssetLoader<Abilities, AssetLoaderParameters<Abilities>> {

    private Abilities abilities;

    public AbilitiesLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<Abilities> parameter) {
        return null;
    }

    @Override @SuppressWarnings("unchecked") public void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Abilities> parameter) {
        Yaml yaml = new Yaml();
        ObjectMap<String, Ability> data = new ObjectMap<String, Ability>();
        for (Object o : yaml.loadAll(resolve(fileName).read())) {
            HashMap<String, Object> value = (HashMap<String, Object>) o;
            Ability description = new Ability(value);
            data.put(description.name, description);
        }
        abilities = new Abilities(data);
        Config.abilities = abilities;
    }

    @Override public Abilities loadSync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Abilities> parameter) {
        return abilities;
    }
}
