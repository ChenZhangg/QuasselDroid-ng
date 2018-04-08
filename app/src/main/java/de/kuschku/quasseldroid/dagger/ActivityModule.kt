package de.kuschku.quasseldroid.dagger

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.kuschku.quasseldroid.service.QuasselService
import de.kuschku.quasseldroid.ui.chat.ChatActivity
import de.kuschku.quasseldroid.ui.chat.ChatActivityModule
import de.kuschku.quasseldroid.ui.chat.ChatFragmentProvider
import de.kuschku.quasseldroid.ui.chat.info.InfoActivity
import de.kuschku.quasseldroid.ui.chat.info.InfoFragmentProvider
import de.kuschku.quasseldroid.ui.chat.topic.TopicActivity
import de.kuschku.quasseldroid.ui.chat.topic.TopicFragmentProvider
import de.kuschku.quasseldroid.ui.clientsettings.about.AboutSettingsActivity
import de.kuschku.quasseldroid.ui.clientsettings.about.AboutSettingsFragmentProvider
import de.kuschku.quasseldroid.ui.clientsettings.app.AppSettingsActivity
import de.kuschku.quasseldroid.ui.clientsettings.app.AppSettingsFragmentProvider
import de.kuschku.quasseldroid.ui.clientsettings.crash.CrashSettingsActivity
import de.kuschku.quasseldroid.ui.clientsettings.crash.CrashSettingsFragmentProvider
import de.kuschku.quasseldroid.ui.clientsettings.license.LicenseSettingsActivity
import de.kuschku.quasseldroid.ui.clientsettings.license.LicenseSettingsFragmentProvider
import de.kuschku.quasseldroid.ui.coresettings.CoreSettingsActivity
import de.kuschku.quasseldroid.ui.coresettings.CoreSettingsFragmentProvider
import de.kuschku.quasseldroid.ui.coresettings.chatlist.ChatListActivity
import de.kuschku.quasseldroid.ui.coresettings.chatlist.ChatListFragmentProvider
import de.kuschku.quasseldroid.ui.coresettings.identity.IdentityActivity
import de.kuschku.quasseldroid.ui.coresettings.identity.IdentityFragmentProvider
import de.kuschku.quasseldroid.ui.coresettings.networkconfig.NetworkConfigActivity
import de.kuschku.quasseldroid.ui.coresettings.networkconfig.NetworkConfigFragmentProvider
import de.kuschku.quasseldroid.ui.setup.accounts.edit.AccountEditActivity
import de.kuschku.quasseldroid.ui.setup.accounts.selection.AccountSelectionActivity
import de.kuschku.quasseldroid.ui.setup.accounts.selection.AccountSelectionFragmentProvider
import de.kuschku.quasseldroid.ui.setup.accounts.setup.AccountSetupActivity
import de.kuschku.quasseldroid.ui.setup.accounts.setup.AccountSetupFragmentProvider

@Module
abstract class ActivityModule {
  @ContributesAndroidInjector(modules = [ChatActivityModule::class, ChatFragmentProvider::class])
  abstract fun bindChatActivity(): ChatActivity

  @ContributesAndroidInjector(modules = [InfoFragmentProvider::class])
  abstract fun bindInfoActivity(): InfoActivity

  @ContributesAndroidInjector(modules = [TopicFragmentProvider::class])
  abstract fun bindTopicActivity(): TopicActivity

  @ContributesAndroidInjector(modules = [AppSettingsFragmentProvider::class])
  abstract fun bindAppSettingsActivity(): AppSettingsActivity

  @ContributesAndroidInjector(modules = [CrashSettingsFragmentProvider::class])
  abstract fun bindCrashSettingsActivity(): CrashSettingsActivity

  @ContributesAndroidInjector(modules = [AboutSettingsFragmentProvider::class])
  abstract fun bindAboutSettingsActivity(): AboutSettingsActivity

  @ContributesAndroidInjector(modules = [LicenseSettingsFragmentProvider::class])
  abstract fun bindLicenseSettingsActivity(): LicenseSettingsActivity

  @ContributesAndroidInjector(modules = [CoreSettingsFragmentProvider::class])
  abstract fun bindCoreSettingsActivity(): CoreSettingsActivity

  @ContributesAndroidInjector(modules = [IdentityFragmentProvider::class])
  abstract fun bindIdentityActivity(): IdentityActivity

  @ContributesAndroidInjector(modules = [ChatListFragmentProvider::class])
  abstract fun bindChatListActivity(): ChatListActivity

  @ContributesAndroidInjector(modules = [NetworkConfigFragmentProvider::class])
  abstract fun bindNetworkConfigActivity(): NetworkConfigActivity

  @ContributesAndroidInjector(modules = [AccountSetupFragmentProvider::class])
  abstract fun bindAccountSetupActivity(): AccountSetupActivity

  @ContributesAndroidInjector(modules = [AccountSelectionFragmentProvider::class])
  abstract fun bindAccountSelectionActivity(): AccountSelectionActivity

  @ContributesAndroidInjector
  abstract fun bindAccountEditActivity(): AccountEditActivity

  @ContributesAndroidInjector
  abstract fun bindQuasselService(): QuasselService
}
