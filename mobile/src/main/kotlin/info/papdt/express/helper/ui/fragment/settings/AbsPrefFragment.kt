package info.papdt.express.helper.ui.fragment.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat

import info.papdt.express.helper.R
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.SettingsActivity
import kotlinx.coroutines.*
import moe.shizuku.preference.Preference
import moe.shizuku.preference.PreferenceFragment
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class AbsPrefFragment : PreferenceFragment(), CoroutineScope {

	private lateinit var job: Job
	override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

	lateinit var settings: Settings

	val parentActivity: SettingsActivity? get() = activity as? SettingsActivity

	override fun onCreate(savedInstanceState: Bundle?) {
		settings = Settings.getInstance(activity!!.applicationContext)
		super.onCreate(savedInstanceState)
        job = Job()
	}

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

	fun makeSnackbar(message: String, duration: Int): Snackbar? {
		return parentActivity?.makeSnackbar(message, duration)
	}

	fun makeRestartTips() {
		makeSnackbar(getString(R.string.toast_need_restart), Snackbar.LENGTH_LONG)
				?.setAction(R.string.toast_need_restart_action) {
					val context = parentActivity
					val packageManager = context?.packageManager
					val intent = packageManager?.getLaunchIntentForPackage(context.packageName)
					val componentName = intent?.component
					val i = Intent.makeRestartActivityTask(componentName)
					parentActivity?.startActivity(i)
					System.exit(0)
				}?.show()
	}

	fun openWebsite(url: String) {
		val builder = CustomTabsIntent.Builder()
		builder.setToolbarColor(ContextCompat.getColor(activity!!, R.color.pink_500))
		builder.build().launchUrl(activity, Uri.parse(url))
	}

    fun ui(block: suspend CoroutineScope.() -> Unit) {
        launch(coroutineContext, block = {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        })
    }

    fun <T> asyncIO(block: suspend CoroutineScope.() -> T): Deferred<T> {
        return async(Dispatchers.IO, block = {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        })
    }

	class PreferenceProperty<out T: Preference>(private val key: String): ReadOnlyProperty<PreferenceFragment, T> {

        private var value: T? = null

        override fun getValue(thisRef: PreferenceFragment, property: KProperty<*>): T {
            if (value == null) {
                value = thisRef.findPreference(key) as T
            }
            return value!!
        }

    }

    class NullablePreferenceProperty<out T: Preference>(private val key: String): ReadOnlyProperty<PreferenceFragment, T?> {

        private var value: T? = null

        override fun getValue(thisRef: PreferenceFragment, property: KProperty<*>): T? {
            if (value == null) {
                value = thisRef.findPreference(key) as? T
            }
            return value
        }

    }

}
