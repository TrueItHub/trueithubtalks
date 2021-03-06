package xyz.savvamirzoyan.trueithubtalks.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import timber.log.Timber
import xyz.savvamirzoyan.trueithubtalks.R
import xyz.savvamirzoyan.trueithubtalks.interfaces.IAuthenticateActivity

class AuthenticateActivity : AppCompatActivity(), IAuthenticateActivity {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticate)

        Timber.i("onCreate() called")

        val navController = this.findNavController(R.id.authenticate_nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.authenticate_nav_host_fragment)
        return navController.navigateUp()
    }

    override fun moveToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

fun Boolean.toInt(): Int = if (this) 1 else 0