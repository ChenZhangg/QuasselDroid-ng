/*
 * Quasseldroid - Quassel client for Android
 *
 * Copyright (c) 2019 Janne Koschinski
 * Copyright (c) 2019 The Quassel Project
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.kuschku.quasseldroid.ui.chat.buffers

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import de.kuschku.libquassel.protocol.BufferId
import de.kuschku.libquassel.protocol.Buffer_Activity
import de.kuschku.libquassel.protocol.Buffer_Type
import de.kuschku.libquassel.protocol.Message_Type
import de.kuschku.libquassel.quassel.BufferInfo
import de.kuschku.libquassel.quassel.ExtendedFeature
import de.kuschku.libquassel.quassel.syncables.BufferViewConfig
import de.kuschku.libquassel.quassel.syncables.interfaces.INetwork
import de.kuschku.libquassel.util.flag.hasFlag
import de.kuschku.libquassel.util.flag.minus
import de.kuschku.libquassel.util.helpers.mapMap
import de.kuschku.libquassel.util.helpers.mapOrElse
import de.kuschku.libquassel.util.helpers.nullIf
import de.kuschku.libquassel.util.helpers.value
import de.kuschku.quasseldroid.BuildConfig
import de.kuschku.quasseldroid.R
import de.kuschku.quasseldroid.persistence.db.AccountDatabase
import de.kuschku.quasseldroid.persistence.db.QuasselDatabase
import de.kuschku.quasseldroid.settings.AppearanceSettings
import de.kuschku.quasseldroid.settings.MessageSettings
import de.kuschku.quasseldroid.ui.chat.ChatActivity
import de.kuschku.quasseldroid.ui.chat.add.create.ChannelCreateActivity
import de.kuschku.quasseldroid.ui.chat.add.join.ChannelJoinActivity
import de.kuschku.quasseldroid.ui.chat.add.query.QueryCreateActivity
import de.kuschku.quasseldroid.ui.coresettings.network.NetworkEditActivity
import de.kuschku.quasseldroid.ui.info.channellist.ChannelListActivity
import de.kuschku.quasseldroid.util.ColorContext
import de.kuschku.quasseldroid.util.avatars.AvatarHelper
import de.kuschku.quasseldroid.util.helper.*
import de.kuschku.quasseldroid.util.irc.format.IrcFormatDeserializer
import de.kuschku.quasseldroid.util.service.ServiceBoundFragment
import de.kuschku.quasseldroid.util.ui.view.WarningBarView
import de.kuschku.quasseldroid.viewmodel.data.BufferHiddenState
import de.kuschku.quasseldroid.viewmodel.data.BufferListItem
import de.kuschku.quasseldroid.viewmodel.data.BufferState
import de.kuschku.quasseldroid.viewmodel.data.BufferStatus
import de.kuschku.quasseldroid.viewmodel.helper.ChatViewModelHelper
import javax.inject.Inject

class BufferViewConfigFragment : ServiceBoundFragment() {
  @BindView(R.id.chatListToolbar)
  lateinit var chatListToolbar: Toolbar

  @BindView(R.id.chatListSpinner)
  lateinit var chatListSpinner: AppCompatSpinner

  @BindView(R.id.chatList)
  lateinit var chatList: RecyclerView

  @BindView(R.id.feature_context_bufferactivitysync)
  lateinit var featureContextBufferActivitySync: WarningBarView

  @BindView(R.id.buffer_search)
  lateinit var bufferSearch: EditText

  @BindView(R.id.buffer_search_clear)
  lateinit var bufferSearchClear: AppCompatImageButton

  @BindView(R.id.buffer_search_container)
  lateinit var bufferSearchContainer: ViewGroup

  @BindView(R.id.fab_chatlist)
  lateinit var fab: SpeedDialView

  @Inject
  lateinit var appearanceSettings: AppearanceSettings

  @Inject
  lateinit var messageSettings: MessageSettings

  @Inject
  lateinit var database: QuasselDatabase

  @Inject
  lateinit var accountDatabase: AccountDatabase

  @Inject
  lateinit var ircFormatDeserializer: IrcFormatDeserializer

  @Inject
  lateinit var modelHelper: ChatViewModelHelper

  private var actionMode: ActionMode? = null

  private val actionModeCallback = object : ActionMode.Callback {
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
      val selected = modelHelper.selectedBuffer.value
      val info = selected?.info
      val session = modelHelper.session.value?.orNull()
      val bufferSyncer = session?.bufferSyncer
      val network = session?.networks?.get(selected?.info?.networkId)
      val bufferViewConfig = modelHelper.bufferViewConfig.value

      return if (info != null && session != null) {
        when (item?.itemId) {
          R.id.action_channellist -> {
            network?.let {
              ChannelListActivity.launch(requireContext(), network = it.networkId())
            }
            actionMode?.finish()
            true
          }
          R.id.action_configure   -> {
            network?.let {
              NetworkEditActivity.launch(requireContext(), network = it.networkId())
            }
            actionMode?.finish()
            true
          }
          R.id.action_connect     -> {
            network?.requestConnect()
            actionMode?.finish()
            true
          }
          R.id.action_disconnect  -> {
            network?.requestDisconnect()
            actionMode?.finish()
            true
          }
          R.id.action_join        -> {
            session.rpcHandler.sendInput(info, "/join ${info.bufferName}")
            actionMode?.finish()
            true
          }
          R.id.action_part        -> {
            session.rpcHandler.sendInput(info, "/part ${info.bufferName}")
            actionMode?.finish()
            true
          }
          R.id.action_delete      -> {
            MaterialDialog.Builder(activity!!)
              .content(R.string.buffer_delete_confirmation)
              .positiveText(R.string.label_yes)
              .negativeText(R.string.label_no)
              .negativeColorAttr(R.attr.colorTextPrimary)
              .backgroundColorAttr(R.attr.colorBackgroundCard)
              .contentColorAttr(R.attr.colorTextPrimary)
              .onPositive { _, _ ->
                selected.info?.let {
                  session.bufferSyncer.requestRemoveBuffer(info.bufferId)
                }
              }
              .onAny { _, _ ->
                actionMode?.finish()
              }
              .build()
              .show()
            true
          }
          R.id.action_rename      -> {
            MaterialDialog.Builder(activity!!)
              .input(
                getString(R.string.label_buffer_name),
                info.bufferName,
                false
              ) { _, input ->
                selected.info?.let {
                  session.bufferSyncer.requestRenameBuffer(info.bufferId, input.toString())
                }
              }
              .positiveText(R.string.label_save)
              .negativeText(R.string.label_cancel)
              .negativeColorAttr(R.attr.colorTextPrimary)
              .backgroundColorAttr(R.attr.colorBackgroundCard)
              .contentColorAttr(R.attr.colorTextPrimary)
              .onAny { _, _ ->
                actionMode?.finish()
              }
              .build()
              .show()
            true
          }
          R.id.action_unhide      -> {
            bufferSyncer?.let {
              bufferViewConfig?.orNull()?.insertBufferSorted(info, bufferSyncer)
            }
            actionMode?.finish()
            true
          }
          R.id.action_hide_temp   -> {
            bufferViewConfig?.orNull()?.requestRemoveBuffer(info.bufferId)
            actionMode?.finish()
            true
          }
          R.id.action_hide_perm   -> {
            bufferViewConfig?.orNull()?.requestRemoveBufferPermanently(info.bufferId)
            actionMode?.finish()
            true
          }
          else                    -> false
        }
      } else {
        false
      }
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
      actionMode = mode
      mode?.menuInflater?.inflate(R.menu.context_buffer, menu)
      return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
      mode?.tag = "BUFFERS"
      return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
      actionMode = null
      listAdapter.unselectAll()
    }
  }

  private lateinit var listAdapter: BufferListAdapter

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.chat_chatlist, container, false)
    ButterKnife.bind(this, view)

    val adapter = BufferViewConfigAdapter()
    modelHelper.bufferViewConfigs.switchMap {
      combineLatest(it.map(BufferViewConfig::liveUpdates))
    }.toLiveData().observe(this, Observer {
      if (it != null) {
        adapter.submitList(it)
      }
    })

    var hasSetBufferViewConfigId = false
    adapter.setOnUpdateFinishedListener {
      if (!hasSetBufferViewConfigId) {
        chatListSpinner.setSelection(adapter.indexOf(modelHelper.chat.bufferViewConfigId.value).nullIf { it == -1 }
                                     ?: 0)
        modelHelper.chat.bufferViewConfigId.onNext(chatListSpinner.selectedItemId.toInt())
        hasSetBufferViewConfigId = true
      }
    }
    chatListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(adapter: AdapterView<*>?) {
        if (hasSetBufferViewConfigId)
          modelHelper.chat.bufferViewConfigId.onNext(-1)
      }

      override fun onItemSelected(adapter: AdapterView<*>?, element: View?, position: Int,
                                  id: Long) {
        if (hasSetBufferViewConfigId)
          modelHelper.chat.bufferViewConfigId.onNext(id.toInt())
      }
    }
    chatListSpinner.adapter = adapter

    listAdapter = BufferListAdapter(
      messageSettings,
      modelHelper.chat.selectedBufferId,
      modelHelper.chat.expandedNetworks
    )

    val avatarSize = resources.getDimensionPixelSize(R.dimen.avatar_size_buffer)

    val colorContext = ColorContext(requireContext(), messageSettings)

    val colorAccent = requireContext().theme.styledAttributes(R.attr.colorAccent) {
      getColor(0, 0)
    }

    val colorAway = requireContext().theme.styledAttributes(R.attr.colorAway) {
      getColor(0, 0)
    }

    var chatListState: Parcelable? = savedInstanceState?.getParcelable(KEY_STATE_LIST)
    var hasRestoredChatListState = false
    listAdapter.setOnUpdateFinishedListener {
      if (it.isNotEmpty()) {
        chatList.layoutManager?.let {
          if (chatListState != null) {
            it.onRestoreInstanceState(chatListState)
            hasRestoredChatListState = true
          }
        }
      }
    }

    modelHelper.negotiatedFeatures.toLiveData().observe(this, Observer { (connected, features) ->
      featureContextBufferActivitySync.setMode(
        if (!connected || features.hasFeature(ExtendedFeature.BufferActivitySync)) WarningBarView.MODE_NONE
        else WarningBarView.MODE_ICON
      )
    })

    combineLatest(
      modelHelper.bufferList,
      modelHelper.chat.expandedNetworks,
      modelHelper.selectedBuffer,
      database.filtered().listenRx(accountId).toObservable(),
      accountDatabase.accounts().listenDefaultFiltered(accountId, 0).toObservable()
    ).map { (info, expandedNetworks, selected, filteredList, defaultFiltered) ->
      val (config, list) = info ?: Pair(null, emptyList())
      val minimumActivity = config?.minimumActivity() ?: Buffer_Activity.NONE
      val activities = filteredList.associate { it.bufferId to it.filtered.toUInt() }
      list.asSequence().sortedBy { props ->
        !props.info.type.hasFlag(Buffer_Type.StatusBuffer)
      }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { props ->
        props.network.networkName
      }).map { props ->
        val activity = props.activity - (activities[props.info.bufferId]
                                         ?: defaultFiltered?.toUInt()
                                         ?: 0u)
        BufferListItem(
          props.copy(
            activity = activity,
            description = ircFormatDeserializer.formatString(
              props.description.toString(),
              colorize = messageSettings.colorizeMirc
            ),
            bufferActivity = Buffer_Activity.of(
              when {
                props.highlights > 0                  -> Buffer_Activity.Highlight
                activity.hasFlag(Message_Type.Plain) ||
                activity.hasFlag(Message_Type.Notice) ||
                activity.hasFlag(Message_Type.Action) -> Buffer_Activity.NewMessage
                activity.isNotEmpty()                 -> Buffer_Activity.OtherActivity
                else                                  -> Buffer_Activity.NoActivity
              }
            ),
            fallbackDrawable = if (props.info.type.hasFlag(Buffer_Type.QueryBuffer)) {
              props.ircUser?.let {
                val nickName = it.nick()
                val useSelfColor = when (messageSettings.colorizeNicknames) {
                  MessageSettings.ColorizeNicknamesMode.ALL          -> false
                  MessageSettings.ColorizeNicknamesMode.ALL_BUT_MINE ->
                    props.ircUser?.network()?.isMyNick(nickName) == true
                  MessageSettings.ColorizeNicknamesMode.NONE         -> true
                }

                colorContext.buildTextDrawable(it.nick(), useSelfColor)
              } ?: colorContext.buildTextDrawable("", colorAway)
            } else {
              val color = if (props.bufferStatus == BufferStatus.ONLINE) colorAccent
              else colorAway

              colorContext.buildTextDrawable("#", color)
            },
            avatarUrls = props.ircUser?.let {
              AvatarHelper.avatar(messageSettings, it, avatarSize)
            } ?: emptyList()
          ),
          BufferState(
            networkExpanded = expandedNetworks[props.network.networkId]
                              ?: (props.networkConnectionState == INetwork.ConnectionState.Initialized),
            selected = selected.info?.bufferId == props.info.bufferId
          )
        )
      }.filter { (props, state) ->
        (props.info.type.hasFlag(BufferInfo.Type.StatusBuffer) || state.networkExpanded) &&
        (minimumActivity.toInt() <= props.bufferActivity.toInt() ||
         props.info.type.hasFlag(Buffer_Type.StatusBuffer))
      }.toList()
    }.toLiveData().observe(this, Observer { processedList ->
      if (hasRestoredChatListState) {
        chatListState = chatList.layoutManager?.onSaveInstanceState()
        hasRestoredChatListState = false
      }
      listAdapter.submitList(processedList)
    })
    listAdapter.setOnClickListener(this@BufferViewConfigFragment::clickListener)
    listAdapter.setOnLongClickListener(this@BufferViewConfigFragment::longClickListener)
    chatList.adapter = listAdapter

    modelHelper.selectedBuffer.toLiveData().observe(this, Observer { buffer ->
      if (buffer != null) {
        val menu = actionMode?.menu
        if (menu != null) {
          val allActions = setOf(
            R.id.action_channellist,
            R.id.action_configure,
            R.id.action_connect,
            R.id.action_disconnect,
            R.id.action_join,
            R.id.action_part,
            R.id.action_delete,
            R.id.action_rename,
            R.id.action_unhide,
            R.id.action_hide_temp,
            R.id.action_hide_perm
          )

          val visibilityActions = when (buffer.hiddenState) {
            BufferHiddenState.VISIBLE          -> setOf(
              R.id.action_hide_temp,
              R.id.action_hide_perm
            )
            BufferHiddenState.HIDDEN_TEMPORARY -> setOf(
              R.id.action_unhide,
              R.id.action_hide_perm
            )
            BufferHiddenState.HIDDEN_PERMANENT -> setOf(
              R.id.action_unhide,
              R.id.action_hide_temp
            )
          }

          val availableActions = when (buffer.info?.type?.enabledValues()?.firstOrNull()) {
            Buffer_Type.StatusBuffer  -> {
              when (buffer.connectionState) {
                INetwork.ConnectionState.Disconnected -> setOf(
                  R.id.action_configure, R.id.action_connect
                )
                INetwork.ConnectionState.Initialized  -> setOf(
                  R.id.action_channellist, R.id.action_configure, R.id.action_disconnect
                )
                else                                  -> setOf(
                  R.id.action_configure, R.id.action_connect, R.id.action_disconnect
                )
              }
            }
            Buffer_Type.ChannelBuffer -> {
              if (buffer.joined) {
                setOf(R.id.action_part)
              } else {
                setOf(R.id.action_join, R.id.action_delete)
              } + visibilityActions
            }
            Buffer_Type.QueryBuffer   -> {
              setOf(R.id.action_delete, R.id.action_rename) + visibilityActions
            }
            else                      -> visibilityActions
          }

          val unavailableActions = allActions - availableActions

          for (action in availableActions) {
            menu.findItem(action)?.isVisible = true
          }
          for (action in unavailableActions) {
            menu.findItem(action)?.isVisible = false
          }
        }
      } else {
        actionMode?.finish()
      }
    })

    chatListToolbar.inflateMenu(R.menu.context_bufferlist)
    chatListToolbar.menu.findItem(R.id.action_search).isChecked = modelHelper.chat.bufferSearchTemporarilyVisible.value
    chatListToolbar.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.action_search      -> {
          item.isChecked = !item.isChecked
          modelHelper.chat.bufferSearchTemporarilyVisible.onNext(item.isChecked)
          true
        }
        R.id.action_show_hidden -> {
          item.isChecked = !item.isChecked
          modelHelper.chat.showHidden.onNext(item.isChecked)
          true
        }
        else                    -> false
      }
    }
    chatList.layoutManager = LinearLayoutManager(context)
    chatList.itemAnimator = DefaultItemAnimator()
    chatList.setItemViewCacheSize(10)

    modelHelper.chat.stateReset.toLiveData().observe(this, Observer {
      listAdapter.submitList(emptyList())
      hasSetBufferViewConfigId = false
    })

    val bufferSearchPermanentlyVisible = modelHelper.bufferViewConfig
      .mapMap(BufferViewConfig::showSearch)
      .mapOrElse(false)

    combineLatest(modelHelper.chat.bufferSearchTemporarilyVisible.distinctUntilChanged(),
                  bufferSearchPermanentlyVisible)
      .toLiveData().observe(this, Observer { (temporarily, permanently) ->
        val visible = temporarily || permanently

        val menuItem = chatListToolbar.menu.findItem(R.id.action_search)
        menuItem.isVisible = !permanently
        if (permanently) menuItem.isChecked = false
        else menuItem.isChecked = temporarily

        bufferSearchContainer.visibleIf(visible)
        if (!visible) bufferSearch.setText("")
      })

    bufferSearch.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable) {
        modelHelper.chat.bufferSearch.onNext(s.toString())
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
    })

    bufferSearchClear.setTooltip()

    bufferSearchClear.setOnClickListener {
      bufferSearch.setText("")
    }

    @ColorInt var colorLabel = 0
    @ColorInt var colorLabelBackground = 0

    @ColorInt var fabBackground0 = 0
    @ColorInt var fabBackground1 = 0
    @ColorInt var fabBackground2 = 0
    view.context.theme.styledAttributes(
      R.attr.colorTextPrimary, R.attr.colorBackgroundCard,
      R.attr.senderColorE, R.attr.senderColorD, R.attr.senderColorC
    ) {
      colorLabel = getColor(0, 0)
      colorLabelBackground = getColor(1, 0)

      fabBackground0 = getColor(2, 0)
      fabBackground1 = getColor(3, 0)
      fabBackground2 = getColor(4, 0)
    }

    fab.addActionItem(
      SpeedDialActionItem.Builder(R.id.fab_create, R.drawable.ic_add)
        .setFabBackgroundColor(fabBackground0)
        .setFabImageTintColor(0xffffffffu.toInt())
        .setLabel(R.string.label_create_channel)
        .setLabelBackgroundColor(colorLabelBackground)
        .setLabelColor(colorLabel)
        .create()
    )

    fab.addActionItem(
      SpeedDialActionItem.Builder(R.id.fab_join, R.drawable.ic_channel)
        .setFabBackgroundColor(fabBackground1)
        .setFabImageTintColor(0xffffffffu.toInt())
        .setLabel(R.string.label_join_long)
        .setLabelBackgroundColor(colorLabelBackground)
        .setLabelColor(colorLabel)
        .create()
    )

    fab.addActionItem(
      SpeedDialActionItem.Builder(R.id.fab_query, R.drawable.ic_account)
        .setFabBackgroundColor(fabBackground2)
        .setFabImageTintColor(0xffffffffu.toInt())
        .setLabel(R.string.label_query_medium)
        .setLabelBackgroundColor(colorLabelBackground)
        .setLabelColor(colorLabel)
        .create()
    )

    fab.setOnActionSelectedListener {
      val networkId = modelHelper.bufferData?.value?.network?.networkId()
      when (it.id) {
        R.id.fab_query  -> {
          context?.let {
            QueryCreateActivity.launch(it, networkId = networkId)
          }
          fab.close(false)
          true
        }
        R.id.fab_join   -> {
          context?.let {
            ChannelJoinActivity.launch(it, networkId = networkId)
          }
          fab.close(false)
          true
        }
        R.id.fab_create -> {
          context?.let {
            ChannelCreateActivity.launch(it, networkId = networkId)
          }
          fab.close(false)
          true
        }
        else            -> false
      }
    }

    fab.visibleIf(BuildConfig.DEBUG)

    return view
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(KEY_STATE_LIST, chatList.layoutManager?.onSaveInstanceState())
  }

  private fun clickListener(bufferId: BufferId) {
    if (actionMode != null) {
      longClickListener(bufferId)
    } else {
      context?.let {
        modelHelper.chat.bufferSearchTemporarilyVisible.onNext(false)
        ChatActivity.launch(it, bufferId = bufferId)
      }
    }
  }

  private fun longClickListener(it: BufferId) {
    if (actionMode == null) {
      chatListToolbar.startActionMode(actionModeCallback)
    }
    if (!listAdapter.toggleSelection(it)) {
      actionMode?.finish()
    }
  }

  companion object {
    private const val KEY_STATE_LIST = "STATE_LIST"
  }
}
