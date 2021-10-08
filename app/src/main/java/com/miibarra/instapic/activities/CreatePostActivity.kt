package com.miibarra.instapic.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.miibarra.instapic.R
import com.miibarra.instapic.models.PostModel
import com.miibarra.instapic.models.UserModel
import kotlinx.android.synthetic.main.activity_create_post.*

private const val TAG = "CreatePostActivity"

class CreatePostActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private var authenticatedUser: UserModel? = null
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    val imagePickerResultLauncher = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri = result.data?.data
            Log.i(TAG, "Photo URI $photoUri")
            ivImagePreview.setImageURI(photoUri)
        } else {
            Toast.makeText(this, "Closing Image Chooser", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        storageReference = FirebaseStorage.getInstance().reference

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

        btnChooseImage.setOnClickListener {
            Log.i(TAG, "Opening image chooser on device")

            // Create intent to open any appliaction that can handle this intent and show images
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            // Checks to see if there is an app that can handle this intent
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                imagePickerResultLauncher.launch(imagePickerIntent)
            }
        }

        btnSubmitPost.setOnClickListener {
            onSubmitButtonClicked()
        }
    }

    private fun onSubmitButtonClicked() {
        if (photoUri == null) {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
            return
        }
        if (etEnterDescription.text.isBlank()) {
            Toast.makeText(this, "Please create a description", Toast.LENGTH_SHORT).show()
            return
        }
        if (authenticatedUser == null) {
            Toast.makeText(this, "User not signed in, please wait", Toast.LENGTH_SHORT).show()
        }

        // Upload photo to Firebase Storage
        btnSubmitPost.isEnabled = false
        val photoUploadUri = photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-${authenticatedUser?.username}-photo.jpg")
        // Use Tasks API to perform tasks that will continue operations if there are no errors
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                // Retrieve the image URL once it has been uploaded
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                // Create Post Model object with the image url and add that to the posts collection
                val newPost = PostModel(
                    etEnterDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    authenticatedUser)
                firestoreDB.collection("posts").add(newPost)
            }.addOnCompleteListener { newPostCreationTask ->
                btnSubmitPost.isEnabled = true
                if (!newPostCreationTask.isSuccessful) {
                    Log.e(TAG, "Error encountered when attempting to upload new post to Firebase", newPostCreationTask.exception)
                    Toast.makeText(this, "Error: Failed to save post", Toast.LENGTH_SHORT).show()
                }
                // Otherwise new post succeeded so clear fields and take user back to profile activity
                etEnterDescription.text.clear()
                ivImagePreview.setImageResource(0)
                Toast.makeText(this, "Successfully created new post!", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, authenticatedUser?.username)
                startActivity(profileIntent)
                finish()
            }


    }
}