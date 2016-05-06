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

import com.vlaaad.common.util.IStateDispatcher;
import com.vlaaad.common.util.StateDispatcher;
import com.vlaaad.dice.api.services.cloud.IConflictResolver;
import com.vlaaad.dice.api.services.cloud.IConflictResolverCallback;
import com.vlaaad.dice.game.user.UserData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 24.05.14 by vlaaad
 */
public class ConflictResolver implements IConflictResolver {

    private IConflictResolverCallback callback;
    private Map serverData;

    public static enum ResolverState {
        hasConflict, noConflicts
    }

    private StateDispatcher<ResolverState> dispatcher = new StateDispatcher<ResolverState>(ResolverState.noConflicts);

    private final DiceHeroes app;

    public ConflictResolver(DiceHeroes app) {
        this.app = app;
    }

    public IStateDispatcher<ResolverState> dispatcher() {
        return dispatcher;
    }

    public Map getServerData() {
        if (dispatcher.getState() == ResolverState.noConflicts)
            throw new IllegalStateException("no conflicts exist!");
        return serverData;
    }

    public void resolve(boolean useLocal) {
        if (dispatcher.getState() == ResolverState.noConflicts)
            throw new IllegalStateException("no conflicts exist!");
        callback.onResolved(useLocal);
        dispatcher.setState(ResolverState.noConflicts);
        serverData = null;
    }

    @Override public void resolveConflict(final Map serverData, final IConflictResolverCallback callback) {
        serverData.remove("uuid");
        final Map localData = UserData.serialize(app.userData);
        localData.remove("uuid");
        if (serverData.equals(localData)) {
            callback.onResolved(true);
            dispatcher.setState(ResolverState.noConflicts);
            return;
        }
        this.callback = callback;
        this.serverData = serverData;
        dispatcher.setState(ResolverState.hasConflict);
    }
}
