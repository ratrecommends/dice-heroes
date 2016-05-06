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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Created 06.10.13 by vlaaad
 */
public class Generator {
    public static void main(String[] args) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.combineSubdirectories = true;
        TexturePacker.process(settings, "generator/gfx", "android/assets", "gfx");
        TexturePacker.process(settings, "generator/world-map", "android/assets", "world-map");
//        renamePrefixes("generator/gfx/ui/level-icon", "ui-level-icon-", "");
    }

    private static void renamePrefixes(String path, String prefix, String newPrefix) {
        Files files = new LwjglFiles();
        for (FileHandle fileHandle : files.local(path).list()) {
            if (fileHandle.name().startsWith(prefix)) {
                String newName = newPrefix + fileHandle.name().substring(prefix.length());
                println(fileHandle.name() + " -> " + newName);
                fileHandle.sibling(newName).write(fileHandle.read(), false);
                fileHandle.delete();
            }
        }
    }

    private static void println(Object o) {
        System.out.println(o);
    }


}
