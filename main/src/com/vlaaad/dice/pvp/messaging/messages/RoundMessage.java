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

package com.vlaaad.dice.pvp.messaging.messages;

import com.vlaaad.dice.Config;
import com.vlaaad.dice.pvp.messaging.IPvpMessage;

import java.util.Comparator;

/**
 * Created 29.07.14 by vlaaad
 */
public class RoundMessage extends IPvpMessage {

    public static final Comparator<? super RoundMessage> COMPARATOR = new Comparator<RoundMessage>() {
        @Override public int compare(RoundMessage o1, RoundMessage o2) {
            return o1.i - o2.i;
        }
    };

    public static void register() {
        Config.json.addClassTag("rm", RoundMessage.class);
        Config.json.addClassTag("turn", TurnMessage.class);
    }

    public static class TurnMessage {
        public String action;
        public Object data;

        public TurnMessage() {
        }

        public TurnMessage(String action, Object data) {
            this.action = action;
            this.data = data;
        }
        @Override public String toString() {
            return "TurnMessage{" +
                "action='" + action + '\'' +
                ", data=" + data +
                '}';
        }
    }

    /**
     * request
     */
    public String q;

    /**
     * idx
     */
    public int i;

    /**
     * answer for request
     */
    public Object a;

    @Override public String toString() {
        return "RoundMessage{" +
            "request='" + q + '\'' +
            ", index=" + i +
            ", response=" + a +
            '}';
    }
}
