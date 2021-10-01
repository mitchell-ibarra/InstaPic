package com.miibarra.instapic.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import com.miibarra.instapic.R

private const val TAG = "PostsActivity"

class PostsActivity : AppCompatActivity() {
    // Create as lateinit var since it will be initialized in onCreate
    // Should never be null after initialization
    private lateinit var firestoreDB: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        firestoreDB = FirebaseFirestore.getInstance()
        val collectionPosts = firestoreDB.collection("posts")
        // One of two ways to query all documents in collection
        // .get() gets entire collection
        // snapshotlistener will have firebase inform us when there is any change in the collection
        collectionPosts.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                // Something went wrong
                Log.e(TAG, "Exception when trying to query posts collection", exception)
                return@addSnapshotListener
            }

            // Otherwise check the data that was returned
            for (document in snapshot.documents) {
                Log.i(TAG, "Document ${document.id}: ${document.data}")
            }
        }
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