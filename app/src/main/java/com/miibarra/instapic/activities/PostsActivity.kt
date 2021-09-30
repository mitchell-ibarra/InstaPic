package com.miibarra.instapic.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.miibarra.instapic.R

class PostsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Menu inflater instantiates menu XML files into Menu objects
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Grab the menu item and go to profile activity if profile is chosen
        if (item.itemId == R.id.menu_profile) {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            startActivity(profileIntent)
        }
        return super.onOptionsItemSelected(item)
    }
}