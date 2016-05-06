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

import com.google.android.gms.games.multiplayer.Participant;
import com.vlaaad.dice.api.services.multiplayer.IParticipant;

/**
 * Created 26.07.14 by vlaaad
 */
public class MultiplayerParticipant implements IParticipant {
    public final Participant participant;

    public MultiplayerParticipant(Participant participant) {
        this.participant = participant;
    }

    @Override public String getDisplayedName() {
        return participant.getDisplayName();
    }
    @Override public String getImageUrl() {
        return participant.getIconImageUrl();
    }
    @Override public String getId() {
        return participant.getParticipantId();
    }
}
