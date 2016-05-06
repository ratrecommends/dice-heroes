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
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;

/**
 * Created 15.05.14 by vlaaad
 */
public class YamlAllLoader extends AsynchronousAssetLoader<Array, AssetLoaderParameters<Array>> {

    private Array list;

    public YamlAllLoader(FileHandleResolver resolver) {super(resolver);}

    @Override public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<Array> parameter) {
        return null;
    }

    @Override public void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Array> parameter) {
        Iterable<Object> data = new Yaml().loadAll(file.read());
        Array<Object> list = new Array<Object>();
        for(Object o : data){
            list.add(o);
        }
        this.list = list;
    }

    @Override public Array loadSync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<Array> parameter) {
        return list;
    }
}
