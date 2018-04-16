package de.kuschku.quasseldroid.ui.coresettings.networkserver

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import butterknife.BindView
import butterknife.ButterKnife
import de.kuschku.libquassel.quassel.syncables.interfaces.INetwork
import de.kuschku.quasseldroid.R
import de.kuschku.quasseldroid.ui.coresettings.SettingsFragment
import de.kuschku.quasseldroid.util.helper.setDependent

class NetworkServerFragment : SettingsFragment(), SettingsFragment.Savable,
                              SettingsFragment.Changeable {
  @BindView(R.id.host)
  lateinit var host: EditText

  @BindView(R.id.port)
  lateinit var port: EditText

  @BindView(R.id.ssl_enabled)
  lateinit var sslEnabled: SwitchCompat

  @BindView(R.id.ssl_verify)
  lateinit var sslVerify: SwitchCompat

  @BindView(R.id.password)
  lateinit var password: EditText

  @BindView(R.id.proxy_enabled)
  lateinit var proxyEnabled: SwitchCompat

  @BindView(R.id.proxy_group)
  lateinit var proxyGroup: ViewGroup

  @BindView(R.id.proxy_type)
  lateinit var proxyType: Spinner

  @BindView(R.id.proxy_host)
  lateinit var proxyHost: EditText

  @BindView(R.id.proxy_port)
  lateinit var proxyPort: EditText

  @BindView(R.id.proxy_user)
  lateinit var proxyUser: EditText

  @BindView(R.id.proxy_pass)
  lateinit var proxyPass: EditText

  private var item: INetwork.Server? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.settings_networkserver, container, false)
    ButterKnife.bind(this, view)

    (arguments?.getSerializable("server") as? INetwork.Server)?.let {
      item = it
    }

    val typeAdapter = ProxyTypeAdapter(listOf(
      ProxyTypeItem(
        value = INetwork.ProxyType.Socks5Proxy,
        name = R.string.settings_networkserver_proxy_type_socks5
      ),
      ProxyTypeItem(
        value = INetwork.ProxyType.HttpProxy,
        name = R.string.settings_networkserver_proxy_type_http
      )
    ))
    proxyType.adapter = typeAdapter

    item?.let { data ->
      host.setText(data.host)
      port.setText(data.port.toString())
      sslEnabled.isChecked = data.useSsl
      sslVerify.isChecked = data.sslVerify
      password.setText(data.password)
      proxyEnabled.isChecked = data.useProxy
      proxyType.setSelection(typeAdapter.indexOf(data.proxyType) ?: 0)
      proxyHost.setText(data.proxyHost)
      proxyPort.setText(data.proxyPort.toString())
      proxyUser.setText(data.proxyUser)
      proxyPass.setText(data.proxyPass)
    }

    proxyEnabled.setDependent(proxyGroup)

    return view
  }

  override fun onSave() = item.let { data ->
    val intent = Intent()
    intent.putExtra("old", data)
    val new = INetwork.Server(
      host = host.text.toString(),
      port = port.text.toString().toIntOrNull() ?: data?.port ?: 0,
      useSsl = sslEnabled.isChecked,
      sslVerify = sslVerify.isChecked,
      password = password.text.toString(),
      useProxy = proxyEnabled.isChecked,
      proxyType = proxyType.selectedItemId.toInt(),
      proxyHost = proxyHost.text.toString(),
      proxyPort = proxyPort.text.toString().toIntOrNull() ?: data?.proxyPort ?: 0,
      proxyUser = proxyUser.text.toString(),
      proxyPass = proxyPass.text.toString()
    )
    intent.putExtra("new", new)
    requireActivity().setResult(Activity.RESULT_OK, intent)
    true
  }

  override fun hasChanged() = item != INetwork.Server(
    host = host.text.toString(),
    port = port.text.toString().toIntOrNull() ?: item?.port ?: 0,
    useSsl = sslEnabled.isChecked,
    sslVerify = sslVerify.isChecked,
    password = password.text.toString(),
    useProxy = proxyEnabled.isChecked,
    proxyType = proxyType.selectedItemId.toInt(),
    proxyHost = proxyHost.text.toString(),
    proxyPort = proxyPort.text.toString().toIntOrNull() ?: item?.proxyPort ?: 0,
    proxyUser = proxyUser.text.toString(),
    proxyPass = proxyPass.text.toString()
  )
}
