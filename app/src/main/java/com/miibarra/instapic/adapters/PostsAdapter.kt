package com.miibarra.instapic.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.miibarra.instapic.R
import com.miibarra.instapic.models.PostModel
import kotlinx.android.synthetic.main.item_single_post.view.*
import java.math.BigInteger
import java.security.MessageDigest

class PostsAdapter (val context: Context, val posts: List<PostModel>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_single_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(post: PostModel) {
            val username = post.user?.username as String
            itemView.tvUsername.text = username
            itemView.tvDescription.text = post.description
            Glide.with(context).load(post.imageUrl).into(itemView.ivPost)
            Glide.with(context).load(getProfileImageUrl(username)).into(itemView.ivProfileImage)
            itemView.tvPostTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
        }

        private fun getProfileImageUrl(username: String): String {
            // User gravatar to generate a unique image url for each user
            val md5Digest = MessageDigest.getInstance("MD5")
            val hashcode = md5Digest.digest(username.toByteArray())
            val bigInt = BigInteger(hashcode)
            val hexadecimal = bigInt.abs().toString(16)
            return "https://www.gravatar.com/avatar/$hexadecimal?d=identicon"
        }
    }
}