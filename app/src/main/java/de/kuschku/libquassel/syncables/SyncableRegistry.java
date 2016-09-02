/*
 * QuasselDroid - Quassel client for Android
 * Copyright (C) 2016 Janne Koschinski
 * Copyright (C) 2016 Ken Børge Viktil
 * Copyright (C) 2016 Magnus Fjell
 * Copyright (C) 2016 Martin Sandsmark <martin.sandsmark@kde.org>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.kuschku.libquassel.syncables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import de.kuschku.libquassel.exceptions.UnknownTypeException;
import de.kuschku.libquassel.functions.types.InitDataFunction;
import de.kuschku.libquassel.objects.serializers.ObjectSerializer;
import de.kuschku.libquassel.primitives.types.QVariant;
import de.kuschku.libquassel.syncables.serializers.AliasManagerSerializer;
import de.kuschku.libquassel.syncables.serializers.BufferSyncerSerializer;
import de.kuschku.libquassel.syncables.serializers.BufferViewConfigSerializer;
import de.kuschku.libquassel.syncables.serializers.BufferViewManagerSerializer;
import de.kuschku.libquassel.syncables.serializers.IdentitySerializer;
import de.kuschku.libquassel.syncables.serializers.IgnoreListManagerSerializer;
import de.kuschku.libquassel.syncables.serializers.IrcChannelSerializer;
import de.kuschku.libquassel.syncables.serializers.IrcUserSerializer;
import de.kuschku.libquassel.syncables.serializers.NetworkConfigSerializer;
import de.kuschku.libquassel.syncables.serializers.NetworkSerializer;
import de.kuschku.libquassel.syncables.types.SyncableObject;
import de.kuschku.libquassel.syncables.types.interfaces.QSyncableObject;

public class SyncableRegistry {
    @NonNull
    private static final Map<String, ObjectSerializer> map = new HashMap<>();

    static {
        map.put("IgnoreListManager", IgnoreListManagerSerializer.get());
        map.put("BufferSyncer", BufferSyncerSerializer.get());
        map.put("BufferViewConfig", BufferViewConfigSerializer.get());
        map.put("BufferViewManager", BufferViewManagerSerializer.get());
        map.put("Identity", IdentitySerializer.get());
        map.put("IrcChannel", IrcChannelSerializer.get());
        map.put("IrcUser", IrcUserSerializer.get());
        map.put("Network", NetworkSerializer.get());
        map.put("NetworkConfig", NetworkConfigSerializer.get());
        map.put("AliasManager", AliasManagerSerializer.get());
    }

    private SyncableRegistry() {

    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static SyncableObject from(@NonNull InitDataFunction function) throws UnknownTypeException {
        String className = function.className;
        ObjectSerializer<? extends SyncableObject> serializer = map.get(className);
        if (serializer == null) throw new UnknownTypeException(className, function);
        return serializer.from(function);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Map<String, QVariant<Object>> toVariantMap(QSyncableObject data) {
        String className = data.getClass().getSimpleName();
        ObjectSerializer<QSyncableObject> serializer = map.get(className);
        if (serializer == null) throw new UnknownTypeException(className);
        return serializer.toVariantMap(data);
    }
}
