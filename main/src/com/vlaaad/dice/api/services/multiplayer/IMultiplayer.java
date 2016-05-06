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

package com.vlaaad.dice.api.services.multiplayer;

import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.Option;
import com.vlaaad.common.util.StateDispatcher;
import com.vlaaad.common.util.futures.IFuture;

/**
 * Created 24.07.14 by vlaaad
 */
public interface IMultiplayer {
    public IStateDispatcher<Integer> invites();
    public IStateDispatcher<Option<GameSession>> currentSession();
    public IFuture<Void> inviteFriends(int playersToInvite, int variant);
    public IFuture<Void> displayInvitations();
    public IFuture<Void> quickMatch(int playersToInvite, int variant);
}
