package com.miibarra.instapic.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.miibarra.instapic.R
import com.miibarra.instapic.adapters.PostsAdapter
import com.miibarra.instapic.models.PostModel
import kotlinx.android.synthetic.main.activity_posts.*

private const val TAG = "PostsActivity"

class PostsActivity : AppCompatActivity() {
    // Create as lateinit var since it will be initialized in onCreate
    // Should never be null after initialization
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var posts: MutableList<PostModel>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        // Start with empty list and once asynchronous call to firestore is done it will be filled
        posts = mutableListOf()

        // Create the adapter
        adapter = PostsAdapter(this, posts)

        // Bind the adapter and the layout manager to the RV
        rvPostsHome.adapter = adapter
        rvPostsHome.layoutManager = LinearLayoutManager(this)

        firestoreDB = FirebaseFirestore.getInstance()
        val collectionPosts = firestoreDB
            .collection("posts")
            .limit(10)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

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
            val postList = snapshot.toObjects(PostModel::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList) {
                Log.i(TAG, "Post $post")
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