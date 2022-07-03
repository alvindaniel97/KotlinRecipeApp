package com.alvin.kotlinrecipeapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.alvin.kotlinrecipeapp.R
import com.alvin.kotlinrecipeapp.databinding.ActivityBottomNavigationBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

class BottomNavigationActivity : BaseActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_recipe_feed, R.id.navigation_my_recipe
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        fabAddRecipe.setOnClickListener {
            startActivity(Intent(this@BottomNavigationActivity, AddRecipeActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemProfile ->
                startActivity(
                    Intent(
                        this@BottomNavigationActivity,
                        ProfileActivity::class.java
                    )
                )
            R.id.itemLogout -> {
                showProgressDialog()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@BottomNavigationActivity, LoginActivity::class.java))
                finish()
                hideProgressDialog()
                Toast.makeText(this@BottomNavigationActivity, resources.getString(R.string.logout), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}