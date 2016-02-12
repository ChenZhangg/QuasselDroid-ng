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

package de.kuschku.quasseldroid_ng.ui.chat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.kuschku.libquassel.events.BacklogInitEvent;
import de.kuschku.libquassel.events.ConnectionChangeEvent;
import de.kuschku.libquassel.events.InitEvent;
import de.kuschku.libquassel.primitives.types.BufferInfo;
import de.kuschku.quasseldroid_ng.R;
import de.kuschku.quasseldroid_ng.util.BoundFragment;

import static de.kuschku.util.AndroidAssert.assertNotNull;

public class LoadingFragment extends BoundFragment {
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    @Bind(R.id.label)
    TextView label;

    @Bind(R.id.count)
    TextView count;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        ButterKnife.bind(this, view);


        label.setText("Connecting");
        count.setText("");

        return view;
    }

    public void onEventMainThread(ConnectionChangeEvent event) {
        progressBar.setIndeterminate(true);

        label.setText(event.status.name());
        count.setText("");
    }

    public void onEventMainThread(InitEvent event) {
        if (context.client().connectionStatus() == ConnectionChangeEvent.Status.INITIALIZING_DATA) {
            progressBar.setIndeterminate(false);
            progressBar.setMax(event.max);
            progressBar.setProgress(event.loaded);

            label.setText(event.getClass().getSimpleName());
            count.setText(String.format("%d/%d", event.loaded, event.max));
        }
    }

    public void onEventMainThread(BacklogInitEvent event) {
        if (context.client().connectionStatus() == ConnectionChangeEvent.Status.LOADING_BACKLOG) {
            progressBar.setIndeterminate(false);
            progressBar.setMax(event.max);
            progressBar.setProgress(event.loaded);

            label.setText(event.getClass().getSimpleName());
            count.setText(String.format("%d/%d", event.loaded, event.max));
        }
    }
}
