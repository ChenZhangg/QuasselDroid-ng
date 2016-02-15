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

package de.kuschku.libquassel.events;

import android.support.annotation.NonNull;

import java.security.cert.X509Certificate;

import de.kuschku.libquassel.ssl.UnknownCertificateException;
import de.kuschku.util.accounts.ServerAddress;

public class UnknownCertificateEvent {
    public final X509Certificate certificate;
    public final ServerAddress address;

    public UnknownCertificateEvent(X509Certificate certificate, ServerAddress address) {
        this.certificate = certificate;
        this.address = address;
    }

    public UnknownCertificateEvent(@NonNull UnknownCertificateException cause) {
        this(cause.certificate, cause.address);
    }
}
