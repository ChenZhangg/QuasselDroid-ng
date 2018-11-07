/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package de.kuschku.quasseldroid.util.ui.settings;

import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;

public class ListPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

  private static final String SAVE_STATE_INDEX = "ListPreferenceDialogFragment.index";
  private static final String SAVE_STATE_ENTRIES = "ListPreferenceDialogFragment.entries";
  private static final String SAVE_STATE_ENTRY_VALUES =
    "ListPreferenceDialogFragment.entryValues";

  private int mClickedDialogEntryIndex;
  private CharSequence[] mEntries;
  private CharSequence[] mEntryValues;

  public static ListPreferenceDialogFragmentCompat newInstance(String key) {
    final ListPreferenceDialogFragmentCompat fragment =
      new ListPreferenceDialogFragmentCompat();
    final Bundle b = new Bundle(1);
    b.putString(ARG_KEY, key);
    fragment.setArguments(b);
    return fragment;
  }

  private static void putCharSequenceArray(Bundle out, String key, CharSequence[] entries) {
    final ArrayList<String> stored = new ArrayList<>(entries.length);

    for (final CharSequence cs : entries) {
      stored.add(cs.toString());
    }

    out.putStringArrayList(key, stored);
  }

  private static CharSequence[] getCharSequenceArray(Bundle in, String key) {
    final ArrayList<String> stored = in.getStringArrayList(key);

    return stored == null ? null : stored.toArray(new CharSequence[stored.size()]);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      final ListPreference preference = getListPreference();

      if (preference.getEntries() == null || preference.getEntryValues() == null) {
        throw new IllegalStateException(
          "ListPreference requires an entries array and an entryValues array.");
      }

      mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
      mEntries = preference.getEntries();
      mEntryValues = preference.getEntryValues();
    } else {
      mClickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, 0);
      mEntries = getCharSequenceArray(savedInstanceState, SAVE_STATE_ENTRIES);
      mEntryValues = getCharSequenceArray(savedInstanceState, SAVE_STATE_ENTRY_VALUES);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(SAVE_STATE_INDEX, mClickedDialogEntryIndex);
    putCharSequenceArray(outState, SAVE_STATE_ENTRIES, mEntries);
    putCharSequenceArray(outState, SAVE_STATE_ENTRY_VALUES, mEntryValues);
  }

  private ListPreference getListPreference() {
    return (ListPreference) getPreference();
  }

  @Override
  protected void onPrepareDialogBuilder(MaterialDialog.Builder builder) {
    super.onPrepareDialogBuilder(builder);

    builder.items(mEntries).itemsCallbackSingleChoice(mClickedDialogEntryIndex, new MaterialDialog.ListCallbackSingleChoice() {
      @Override
      public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
        mClickedDialogEntryIndex = which;

        /*
         * Clicking on an item simulates the positive button
         * click, and dismisses the dialog.
         */
        ListPreferenceDialogFragmentCompat.this.onClick(dialog, DialogAction.POSITIVE);
        dialog.dismiss();
        return true;
      }
    });

    /*
     * The typical interaction for list-based dialogs is to have
     * click-on-an-item dismiss the dialog instead of the user having to
     * press 'Ok'.
     */
    builder.positiveText(null);
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    final ListPreference preference = getListPreference();
    if (positiveResult && mClickedDialogEntryIndex >= 0) {
      String value = mEntryValues[mClickedDialogEntryIndex].toString();
      if (preference.callChangeListener(value)) {
        preference.setValue(value);
      }
    }
  }

}
