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

import com.badlogic.gdx.Gdx;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotContents;
import com.google.android.gms.games.snapshot.Snapshots.OpenSnapshotResult;
import com.vlaaad.dice.MainActivity;
import com.vlaaad.dice.api.services.cloud.ICloudSave;
import com.vlaaad.dice.api.services.cloud.IConflictResolver;
import com.vlaaad.dice.api.services.cloud.IConflictResolverCallback;
import com.vlaaad.dice.game.user.UserData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.google.android.gms.games.snapshot.SnapshotMetadataChange.EMPTY_CHANGE;

/**
 * Created 20.05.14 by vlaaad
 */
public class GoogleCloudSave implements ICloudSave {
    public static final String SNAPSHOT_ID = "default-0";
    private final GoogleApiClient client;
    private final MainActivity activity;

    public GoogleCloudSave(MainActivity activity, GoogleApiClient client) {
        this.activity = activity;
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    @Override public void sync(final UserData userData, final IConflictResolver resolver) {
        activity.getMainHandler().post(new Runnable() {
            @Override public void run() {
                if (client.isConnected()) {
                    Games.Snapshots.open(client, SNAPSHOT_ID, true).setResultCallback(new ResultCallback<OpenSnapshotResult>() {
                        @Override public void onResult(final OpenSnapshotResult result) {
                            processOpenSnapshotResult(result, userData, resolver);
                        }
                    });
                }
            }
        });
    }

    private void processOpenSnapshotResult(OpenSnapshotResult result, final UserData userData, IConflictResolver resolver) {
        final Snapshot snapshot = result.getSnapshot();
        if (result.getStatus().isSuccess()) {
            final SnapshotContents contents = snapshot.getSnapshotContents();
            final Map server = fromBytes(contents);
            if (server != null && server.containsKey("uuid") && !server.get("uuid").equals(userData.uuid())) {
                performUserResolve(server, resolver, new IConflictResolverCallback() {
                    @Override public void onResolved(boolean useLocal) {
                        contents.writeBytes(useLocal ? toBytes(userData) : toBytes(server));
                        Games.Snapshots.commitAndClose(client, snapshot, EMPTY_CHANGE);
                    }
                });
            } else {
                contents.writeBytes(toBytes(userData));
                Games.Snapshots.commitAndClose(client, snapshot, EMPTY_CHANGE);
            }
        } else if (result.getStatus().getStatusCode() == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {
            Snapshot conflictingSnapshot = result.getConflictingSnapshot();
            startResolving(userData, conflictingSnapshot, result.getConflictId(), resolver);
        }
    }

    private void startResolving(final UserData userData, final Snapshot conflictingSnapshot, final String conflictId, final IConflictResolver resolver) {
        final SnapshotContents contents = conflictingSnapshot.getSnapshotContents();
        final Map server = fromBytes(contents);
        if (server == null) {
            contents.writeBytes(toBytes(userData));
            Games.Snapshots.resolveConflict(client, conflictId, SNAPSHOT_ID, EMPTY_CHANGE, contents);
            return;
        }
        performUserResolve(server, resolver, new IConflictResolverCallback() {
            @SuppressWarnings("unchecked")
            @Override public void onResolved(boolean useLocal) {
                contents.writeBytes(useLocal ? toBytes(userData) : toBytes(server));
                Games.Snapshots
                    .resolveConflict(client, conflictId, SNAPSHOT_ID, EMPTY_CHANGE, contents)
                    .setResultCallback(new ResultCallback<OpenSnapshotResult>() {
                        @Override public void onResult(OpenSnapshotResult result) {
                            if (result.getStatus().getStatusCode() == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {
                                startResolving(userData, result.getConflictingSnapshot(), result.getConflictId(), resolver);
                            }
                        }
                    });
            }
        });
    }

    private void performUserResolve(final Map server, final IConflictResolver resolver, final IConflictResolverCallback callback) {
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                resolver.resolveConflict(server, new IConflictResolverCallback() {
                    @Override public void onResolved(final boolean useLocal) {
                        activity.getMainHandler().post(new Runnable() {
                            @Override public void run() {
                                callback.onResolved(useLocal);
                            }
                        });
                    }
                });
            }
        });
    }

    private byte[] toBytes(Map data) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        String toSaveString = new Yaml(options).dump(data);
        byte[] result;
        try {
            result = toSaveString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = toSaveString.getBytes();
        }
        return result;
    }

    private byte[] toBytes(UserData userData) {
        return toBytes(userData.toMap());
    }

    private Map fromBytes(SnapshotContents contents) {
        byte[] bytes;
        try {
            bytes = contents.readFully();
        } catch (IOException e) {
            return null;
        }
        if (bytes == null)
            return null;
        String str;
        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            str = new String(bytes);
        }
        return (Map) new Yaml().load(str);
    }
}
