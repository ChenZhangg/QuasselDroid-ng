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

package de.kuschku.quasseldroid_ng.ui.coresettings.aliases.helper;

import android.os.Bundle;

import de.kuschku.libquassel.syncables.types.impl.AliasManager;

public class AliasSerializerHelper {
    private AliasSerializerHelper() {
    }

    public static Bundle serialize(AliasManager.Alias alias) {
        Bundle bundle = new Bundle();
        bundle.putString("name", alias.name);
        bundle.putString("expansion", alias.expansion);
        return bundle;
    }

    public static AliasManager.Alias deserialize(Bundle bundle) {
        return new AliasManager.Alias(
                bundle.getString("name"),
                bundle.getString("expansion")
        );
    }
}
