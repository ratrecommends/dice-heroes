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

package com.vlaaad.dice.purchases.tasks.imp;

import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFuture;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.purchases.PurchaseHelper;
import com.vlaaad.dice.purchases.tasks.IabTask;
import com.vlaaad.dice.util.IabHelper;
import com.vlaaad.dice.util.IabResult;
import com.vlaaad.dice.util.Purchase;

/**
 * Created 30.01.14 by vlaaad
 */
public class PurchaseTask implements IabTask {
    private final PurchaseHelper helper;
    private final PurchaseInfo info;

    public PurchaseTask(PurchaseHelper helper, PurchaseInfo info) {
        this.helper = helper;
        this.info = info;
    }

    @Override public IFuture<IabResult> start() {
        final Future<IabResult> future = new Future<IabResult>();
        helper.iabHelper.launchPurchaseFlow(helper.activity, info.skuName(), PurchaseHelper.RC_REQUEST, new IabHelper.OnIabPurchaseFinishedListener() {
            @Override public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (helper.iabHelper == null) {
                    future.happen(result);
                    return;
                }
                if (result.isFailure()) {
                    helper.mobileApi.fails.add(result.getMessage());
                    helper.mobileApi.notifyListener();
                    future.happen(result);
                    return;
                }
                helper.consumeAsync(purchase, info);
                future.happen(result);
            }
        });
        return future;
    }
}
