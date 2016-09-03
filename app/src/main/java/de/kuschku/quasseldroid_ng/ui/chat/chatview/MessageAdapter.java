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

package de.kuschku.quasseldroid_ng.ui.chat.chatview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import de.kuschku.libquassel.message.Message;
import de.kuschku.quasseldroid_ng.ui.theme.AppContext;
import de.kuschku.util.observables.AutoScroller;
import de.kuschku.util.observables.callbacks.UICallback;
import de.kuschku.util.observables.callbacks.wrappers.AdapterUICallbackWrapper;
import de.kuschku.util.observables.lists.IObservableList;
import de.kuschku.util.observables.lists.ObservableComparableSortedList;
import de.kuschku.util.observables.lists.ObservableSortedList;

import static de.kuschku.util.AndroidAssert.assertNotNull;

@UiThread
public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    @NonNull
    private static final ObservableSortedList<Message> emptyList = new ObservableComparableSortedList<>(Message.class);
    @NonNull
    private final ChatMessageRenderer renderer;
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final UICallback callback;
    @NonNull
    private IObservableList<UICallback, Message> messageList = emptyList();
    @NonNull
    private AppContext context;

    public MessageAdapter(@NonNull Context ctx, @NonNull AppContext context, @Nullable AutoScroller scroller) {
        this.context = context;
        this.inflater = LayoutInflater.from(ctx);
        this.renderer = new ChatMessageRenderer(context);
        this.callback = new AdapterUICallbackWrapper(this, scroller);
    }

    @NonNull
    public static ObservableSortedList<Message> emptyList() {
        return emptyList;
    }

    public void setMessageList(@NonNull ObservableSortedList<Message> messageList) {
        this.messageList.removeCallback(callback);
        this.messageList = messageList;
        this.messageList.addCallback(callback);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean highlightFlag = viewType % 2 == 1;
        Message.Type actualType = Message.Type.fromId(viewType >> 1);
        return new MessageViewHolder(context, inflater.inflate(renderer.getLayoutRes(actualType), parent, false), highlightFlag);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = getItem(position);
        assertNotNull(msg);

        renderer.onBind(holder, msg);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        Message.Type type = message.type;
        int highlightFlag = message.flags.Highlight ? 1 : 0;
        return type.value << 1 | highlightFlag;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public Message getItem(int position) {
        return messageList.get(position);
    }
}
