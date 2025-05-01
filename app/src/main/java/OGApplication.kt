import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MY_Preference")
class OGApplication: Application() {
    companion object {
        private lateinit var context: Context
        fun getAppContext(): Context = OGApplication.context
    }
    override fun onCreate() {
        super.onCreate()

        context = applicationContext
}
}