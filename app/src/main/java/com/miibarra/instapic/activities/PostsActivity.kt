package com.miibarra.instapic.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.miibarra.instapic.R
import com.miibarra.instapic.adapters.PostsAdapter
import com.miibarra.instapic.models.PostModel
import com.miibarra.instapic.models.UserModel
import kotlinx.android.synthetic.main.activity_posts.*

private const val TAG = "PostsActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostsActivity : AppCompatActivity() {

    private var authenticatedUser: UserModel? = null
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

        firestoreDB.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                authenticatedUser = userSnapshot.toObject(UserModel::class.java)
                Log.i(TAG, "Authenticated user is $authenticatedUser")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get the authenticated user", exception)
            }


        var collectionPosts: Query = firestoreDB
            .collection("posts")
            .limit(10)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        // Check for username and if not null then that means we are in the profile activity
        // Query posts to only show the results that match the profile's username
        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null) {
            supportActionBar?.title = username
            collectionPosts = collectionPosts.whereEqualTo("user.username", username)
        }

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

        fabCreatePost.setOnClickListener {
            val createPostIntent = Intent(this, CreatePostActivity::class.java)
            startActivity(createPostIntent)
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
            profileIntent.putExtra(EXTRA_USERNAME, authenticatedUser?.username)
            startActivity(profileIntent)
        }
        return super.onOptionsItemSelected(item)
    }
}