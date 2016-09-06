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

package de.kuschku.util.irc.format.spans;

import android.support.annotation.ColorInt;
import android.text.style.ForegroundColorSpan;

public class IrcForegroundColorSpan extends ForegroundColorSpan implements Copyable<IrcForegroundColorSpan> {
    public final int mircColor;

    public IrcForegroundColorSpan(int mircColor, @ColorInt int color) {
        super(color);
        this.mircColor = mircColor;
    }

    @Override
    public IrcForegroundColorSpan copy() {
        return new IrcForegroundColorSpan(mircColor, getForegroundColor());
    }
}
