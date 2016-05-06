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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.vlaaad.common.util.Logger;
import com.vlaaad.dice.game.user.UserData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created 20.05.14 by vlaaad
 */
class UserDataHelper {
    private byte[] key;

    UserData loadUserData() {
        FileHandle data = Gdx.files.local("game.save");
        boolean newUserData = false;
        InputStream inputStream;
        if (!data.exists()) {
            data = Gdx.files.internal("initial-game-data.yml");
            newUserData = true;
            inputStream = data.read();
        } else {
            if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                UserData result = UserData.deserialize(new Yaml().load(data.read()));
                if (!result.tutorialCompleted) {
                    result = UserData.deserialize(new Yaml().load(Gdx.files.internal("initial-game-data.yml").read()));
                }
                return result;
            }
            //file exists, decode
            loadKey();
            if (key == null) {
                inputStream = data.read();
            } else {
                try {
                    byte[] stringData = decrypt(key, data.readBytes());
                    inputStream = new ByteArrayInputStream(stringData);
                } catch (Exception e) {
                    Logger.error("failed to decrypt data", e);
                    inputStream = data.read();
                }
            }
        }
        Object raw = new Yaml().load(inputStream);
        if (raw == null) {
            return UserData.deserialize(new Yaml().load(Gdx.files.internal("initial-game-data.yml").read()));
        }
        UserData result = UserData.deserialize(raw);
        if (!newUserData && !result.tutorialCompleted) {
            result = UserData.deserialize(new Yaml().load(Gdx.files.internal("initial-game-data.yml").read()));
        }
        return result;
    }

    private void loadKey() {
        if (key != null)
            return;
        FileHandle keyFile = Gdx.files.local("app.key");
        if (!keyFile.exists()) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(128);
                key = keyGenerator.generateKey().getEncoded();
                keyFile.writeBytes(key, false);
            } catch (Exception e) {
                Logger.log("failed to load key", e);
            }
        } else {
            key = keyFile.readBytes();
        }
    }

    void saveUserData(UserData userData) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String toSave = new Yaml(options).dump(UserData.serialize(userData));
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            Gdx.files.local("game.save").writeString(toSave, false);
            return;
        }
        try {
            loadKey();
            byte[] bytes = encrypt(key, toSave.getBytes("UTF-8"));
            Gdx.files.local("game.save").writeBytes(bytes, false);
        } catch (Exception e) {
            Gdx.files.local("game.save").writeString(toSave, false);
        }
    }

    private byte[] encrypt(byte[] key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec spec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        return cipher.doFinal(data);
    }

    private byte[] decrypt(byte[] key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec spec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, spec);
        return cipher.doFinal(data);
    }
}
