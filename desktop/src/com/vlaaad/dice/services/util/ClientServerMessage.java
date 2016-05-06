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

package com.vlaaad.dice.services.util;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Created 27.07.14 by vlaaad
 */
public class ClientServerMessage {

    public static final Json json = new Json(JsonWriter.OutputType.json);
    static {
        json.setUsePrototypes(false);
        json.setTypeName(null);
        json.setSerializer(ClientServerMessage.class, new Json.Serializer<ClientServerMessage>() {
            @Override public void write(Json json, ClientServerMessage object, Class knownType) {
                json.writeObjectStart();
                json.writeValue("type", object.type.toString());
                if (object.participantId != null) json.writeValue("participant", object.participantId);
                if (object.data != null) json.writeValue("data", object.data);
                json.writeObjectEnd();
            }

            @Override public ClientServerMessage read(Json json, JsonValue jsonData, Class kind) {
                Type type = Type.valueOf(jsonData.getString("type"));
                String participant = jsonData.getString("participant", null);
                String data = jsonData.getString("data", null);
                return new ClientServerMessage(participant, type, data);
            }
        });
    }

    public static enum Type {
        loadInvites, loadPlayersToInvite, disconnect, invitePlayer, acceptInvite, declineInvite, startSession, sessionMessage, endSession
    }

    public final String participantId;
    public final Type type;
    public final String data;

    public ClientServerMessage(String participantId, Type type, String data) {
        this.participantId = participantId;
        this.type = type;
        this.data = data;
    }

    public ClientServerMessage(String participantId, Type type) {
        this(participantId, type, null);
    }

    @Override public String toString() {
        return "ClientServerMessage{" +
            "participantId='" + participantId + '\'' +
            ", type=" + type +
            ", data='" + data + '\'' +
            '}';
    }
}
