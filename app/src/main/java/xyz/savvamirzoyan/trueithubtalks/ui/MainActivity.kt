package xyz.savvamirzoyan.trueithubtalks.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import timber.log.Timber
import xyz.savvamirzoyan.trueithubtalks.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.i("onCreate() called")

        val navController = this.findNavController(R.id.main_nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.main_nav_host_fragment)
        return navController.navigateUp()
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume() Called")
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause() Called")
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop() Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy() Called")
    }

    override fun onRestart() {
        super.onRestart()
        Timber.i("onRestart() Called")
    }
}