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

import com.vlaaad.common.util.Logger;
import com.vlaaad.dice.api.IMobileApi;
import com.vlaaad.dice.api.purchases.IPurchaseListener;
import com.vlaaad.dice.api.services.IGameServices;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.services.LocalGameServices;

/**
 * Created 15.10.13 by vlaaad
 */
public class LocalMobileApi implements IMobileApi {
    private IPurchaseListener listener;
    private LocalGameServices gameServices = new LocalGameServices();

    @Override public int getVersionCode() {
        return 3;
    }

    @Override public void share(String message) {
        Logger.log("share: " + message);
    }

    @Override public void setPurchaseListener(IPurchaseListener listener) {
        this.listener = listener;
    }

    @Override public void purchase(PurchaseInfo info) {
        if (listener != null)
            listener.onPurchase(info);
    }

    @Override public void rateApp() {
    }
    @Override public void keepScreenOn(boolean value) {}

    @Override public IGameServices services() {
        return gameServices;
    }
}
