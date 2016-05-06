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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.vlaaad.dice.purchases.PurchaseHelper;
import com.vlaaad.dice.services.AndroidMobileApi;
import com.vlaaad.dice.services.GameServicesHelper;

public class MainActivity extends AndroidApplication {

    private PurchaseHelper purchaseHelper;
    private GameServicesHelper gameServicesHelper;
    private Handler mainHandler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(getMainLooper());

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.hideStatusBar = true;
        config.useAccelerometer = false;
        config.useCompass = false;

        final AndroidMobileApi api = new AndroidMobileApi(this);
        initialize(new DiceHeroes(api), config);
        gameServicesHelper = new GameServicesHelper(this);
        purchaseHelper = new PurchaseHelper(this, api);
    }

    @Override protected void onStart() {
        super.onStart();
        gameServicesHelper.onStart(this);
    }

    @Override protected void onStop() {
        super.onStop();
        gameServicesHelper.onStop();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (purchaseHelper != null)
            purchaseHelper.onActivityResult(requestCode, resultCode, data);
        if (gameServicesHelper != null)
            gameServicesHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (purchaseHelper != null)
            purchaseHelper.dispose();
    }

    public PurchaseHelper getPurchaseHelper() {
        return purchaseHelper;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public GameServicesHelper getGameServicesHelper() {
        return gameServicesHelper;
    }

}
