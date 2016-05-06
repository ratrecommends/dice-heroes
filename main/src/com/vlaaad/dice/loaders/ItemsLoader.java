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
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.vlaaad.dice.Config;
import com.vlaaad.dice.game.config.items.Item;
import com.vlaaad.dice.game.config.items.Items;
import com.vlaaad.dice.game.config.professions.ProfessionDescription;
import com.vlaaad.dice.game.config.professions.Professions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.nio.channels.AsynchronousChannel;

/**
 * Created 10.10.13 by vlaaad
 */
public class ItemsLoader extends AsynchronousAssetLoader<Items, AssetLoaderParameters<Items>> {
    private Items items;

    public ItemsLoader(FileHandleResolver resolver) {super(resolver);}

    @Override public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<Items> parameter) {
        return null;
    }

    @Override public void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Items> parameter) {
        Constructor constructor = new Constructor(Item.class);
        Yaml yaml = new Yaml(constructor);
        ObjectMap<String, Item> data = new ObjectMap<String, Item>();
        for (Object o : yaml.loadAll(resolve(fileName).read())) {
            Item item = (Item) o;
            data.put(item.name, item);
        }
        items = new Items(data);
        Config.items = items;
    }

    @Override public Items loadSync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Items> parameter) {
        return items;
    }
}
