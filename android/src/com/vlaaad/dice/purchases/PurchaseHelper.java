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

package com.vlaaad.dice.purchases;

import android.content.Intent;
import android.widget.Toast;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.common.util.Logger;
import com.vlaaad.common.util.futures.Future;
import com.vlaaad.common.util.futures.IFutureListener;
import com.vlaaad.dice.MainActivity;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.purchases.tasks.IabTask;
import com.vlaaad.dice.purchases.tasks.imp.ConsumeTask;
import com.vlaaad.dice.purchases.tasks.imp.PurchaseTask;
import com.vlaaad.dice.purchases.tasks.imp.QueryInventoryTask;
import com.vlaaad.dice.services.AndroidMobileApi;
import com.vlaaad.dice.util.IabHelper;
import com.vlaaad.dice.util.IabResult;
import com.vlaaad.dice.util.Purchase;

/**
 * Created 30.01.14 by vlaaad
 */
public class PurchaseHelper {

    private final Future<IabResult> onSetUp = new Future<IabResult>();

    public static final int RC_REQUEST = 10001;
    private static final String str = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAluUJ6sKxFJ6inxEZMmKaSzjn/HkE6gt0i7aVw/yrz5//0n2zvttfi+uZLKaHQF8SyPgK15HahVDAlD1kpQPwkyfnhRiO56ftx53JZnMmuG1XyBKVNWdOoTnfpY1rmzW/iJ0gQ5Q9rHaWDi8m5g7Pb2OvuVitneJ9pxeFx9aas+7wCbPPDFDEy858jPB37qdsqmf68MAw3ziQEUT/ebq+AmUkKTvcNsfViFFxC02ubuHCXJ9Wo2kP8e0zlVIi/B63/RqjXp+H1rgxui7PMpji8MY/lL9iWt4SNNy7IE7eNZpGVKmRDpJs5aOWtZdyUOeNrR6iJ5fHaTUoqBouplJ+kQIDAQAB";
    public IabHelper iabHelper;
    public final MainActivity activity;
    public final AndroidMobileApi mobileApi;
    private Array<IabTask> tasks = new Array<IabTask>(0);
    private boolean taskInProgress;

    public PurchaseHelper(final MainActivity activity, AndroidMobileApi mobileApi) {
        this.activity = activity;
        this.mobileApi = mobileApi;
        iabHelper = new IabHelper(activity, str);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override public void onIabSetupFinished(IabResult result) {
                Logger.log("finished setup with result " + result);
                if (result.isFailure()) {
                    Toast.makeText(activity, "Problem setting up in-app billing: " + result, Toast.LENGTH_LONG).show();
                    return;
                }
                addTask(new QueryInventoryTask(PurchaseHelper.this));
                onSetUp.happen(result);
            }
        });
    }

    public void addTask(IabTask task) {
        tasks.add(task);
        checkTasks();
    }

    private void checkTasks() {
        if (iabHelper == null || tasks.size == 0 || taskInProgress)
            return;
        taskInProgress = true;
        IabTask task = tasks.removeIndex(0);
//        Logger.log("start task " + task);
        task.start().addListener(new IFutureListener<IabResult>() {
            @Override public void onHappened(IabResult result) {
                taskInProgress = false;
                checkTasks();
            }
        });
    }

    public void consumeAsync(final Purchase purchase, final PurchaseInfo info) {
        onSetUp.addListener(new IFutureListener<IabResult>() {
            @Override public void onHappened(IabResult result) {
                if (result.isFailure()) {
                    Toast.makeText(activity, "Trying to consume, but setup failed: " + result + ". May be restart game?", Toast.LENGTH_LONG).show();
                    return;
                }
                addTask(new ConsumeTask(PurchaseHelper.this, purchase, info));
            }
        });
    }

    public void launchPurchaseFlow(final PurchaseInfo info) {
        onSetUp.addListener(new IFutureListener<IabResult>() {
            @Override public void onHappened(IabResult result) {
                if (result.isFailure()) {
                    Toast.makeText(activity, "Trying to purchase, but setup failed: " + result + ". May be restart game?", Toast.LENGTH_LONG).show();
                    return;
                }
                addTask(new PurchaseTask(PurchaseHelper.this, info));
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iabHelper == null)
            return;
        iabHelper.handleActivityResult(requestCode, resultCode, data);
    }

    public void dispose() {
        try {
            iabHelper.dispose();
        } catch (Exception ignored) {
            // Caused by: java.lang.IllegalArgumentException: Service not registered :(
        }
        iabHelper = null;
    }
}
