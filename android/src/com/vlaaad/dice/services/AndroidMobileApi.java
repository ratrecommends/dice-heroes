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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.WindowManager;
import android.widget.Toast;
import com.badlogic.gdx.utils.Array;
import com.vlaaad.dice.MainActivity;
import com.vlaaad.dice.api.IMobileApi;
import com.vlaaad.dice.api.purchases.IPurchaseListener;
import com.vlaaad.dice.api.services.IGameServices;
import com.vlaaad.dice.game.config.purchases.PurchaseInfo;
import com.vlaaad.dice.purchases.PurchaseHelper;
import com.vlaaad.dice.util.IabResult;

public class AndroidMobileApi implements IMobileApi {
    private IPurchaseListener listener;

    private final MainActivity activity;
    private final Array<PurchaseInfo> toSend = new Array<PurchaseInfo>();
    public final Array<String> fails = new Array<String>();

    private boolean versionCodeInitialized = false;
    private int versionCode;

    public AndroidMobileApi(MainActivity activity) {
        this.activity = activity;
    }

    @Override public int getVersionCode() {
        if (!versionCodeInitialized) {
            try {
                PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                versionCode = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                versionCode = -1;
            }
            versionCodeInitialized = true;
        }
        return versionCode;
    }

    @Override public void purchase(final PurchaseInfo info) {
        final PurchaseHelper helper = activity.getPurchaseHelper();
        if (helper != null) {
            try {
                helper.launchPurchaseFlow(info);
            } catch (Exception ignored) {
                fails.add("ui-iab-failed");
                notifyListener();
            }
        }
    }

    @Override public void share(String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        activity.startActivity(Intent.createChooser(intent, "Share"));
    }

    @Override public void setPurchaseListener(IPurchaseListener listener) {
        this.listener = listener;
        notifyListener();
    }


    public void onConsumed(IabResult result, PurchaseInfo info) {
        if (result.isSuccess()) {
            toSend.add(info);
        } else {
            fails.add(result.getMessage());
        }
        notifyListener();
    }

    public void notifyListener() {
        if (listener == null)
            return;
        for (PurchaseInfo info : toSend) {
            listener.onPurchase(info);
        }
        toSend.clear();
        for (String message : fails) {
            listener.onPurchaseFailed(message);
        }
        fails.clear();
    }

    @Override public void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e1) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName()));
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(activity, "Can't launch market :(", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override public void keepScreenOn(final boolean value) {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                if (value) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    @Override public IGameServices services() {
        return activity.getGameServicesHelper();
    }

}
