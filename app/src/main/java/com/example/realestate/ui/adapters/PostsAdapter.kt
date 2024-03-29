package com.example.realestate.ui.adapters

import android.content.pm.ApplicationInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realestate.R
import com.example.realestate.data.models.*
import com.example.realestate.databinding.SinglePostBinding
import com.example.realestate.utils.*

class PostsAdapter(


    private val postClickListener: OnPostClickListener,
    private val addToFavClicked: OnAddToFavClicked? = null,

    ) : RecyclerView.Adapter<PostsAdapter.PostHolder>(), Filterable {
    companion object {
        const val TAG = "PostAdapter"
    }

    private var postsList: MutableList<PostWithOwnerId> = mutableListOf()
    private var filteredList: List<PostWithOwnerId> = postsList

    //    private var postsList: MutableList<PostWithOwnerId> = mutableListOf()
//    private var postsListFull: MutableList<PostWithOwnerId> = postsList
    private var favourites: MutableList<String> = mutableListOf()
    private var countriesData: CountriesData? = null
    fun setPostsList(list: List<PostWithOwnerId>) {
        postsList = list.toMutableList()
        filteredList = postsList
        notifyDataSetChanged()
    }

    fun setLiked(list: List<String>) {
        favourites = list.toMutableList()
        notifyDataSetChanged()
    }

    fun setCountriesData(data: CountriesData?) {
        countriesData = data
        notifyDataSetChanged()
    }

    inner class PostHolder(private val binding: SinglePostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val detailsShortAdapter = DetailsShortAdapter()
        fun bind(position: Int) {
            val currentPost = filteredList[position]
            val context = binding.root.context
            val isChecked = currentPost.id in favourites
            val countryData =
                countriesData?.find { country -> country.name == currentPost.location.country }

            binding.apply {


                postWhole.setOnClickListener {
                    postClickListener.onClick(currentPost.id!!)
                }

                //load the first image if nothing found load first media
                val firstImage =
                    currentPost.media.find { image ->
                        getMediaType(
                            image,
                            TAG
                        ) == MediaType.IMAGE
                    }

                if (firstImage != null) {
                    postImage.loadImage(firstImage)
                } else {
                    if (currentPost.media.isNotEmpty()) {
                        postImage.loadImage(currentPost.media[0])
                    }
                }

                outOfOrder.isVisible = currentPost.status == PostStatus.OUT_OF_ORDER.value

                postInfo.apply {
                    defineField(
                        context.getString(
                            R.string.category_type,
                            currentPost.category.upperFirstLetter(),
                            currentPost.type.upperFirstLetter()
                        )
                    )
                }
                when (currentPost.type) {
                    Type.RENT.value -> {
                        val toShow = context.getString(
                            R.string.price_rent,
                            formatNumberWithCommas(currentPost.price.toDouble()),
                            currentPost.period
                        )
                        postPrice.defineField(
                            toShow
                        )
                    }
                    else -> {
                        val toShow = context.getString(
                            R.string.price,
                            formatNumberWithCommas(currentPost.price.toDouble())
                        )
                        postPrice.defineField(
                            toShow
                        )
                    }
                }
                currentPost.location.apply {
                    countryFlag.loadImage(countryData?.image)
                    postLocation.text =
                        context.getString(
                            R.string.location,
                            country,
                            city,
                            area,
                        )
                }

                //favourites button

                addToFav.isChecked = isChecked
                addToFav.setOnClickListener {

                    val userConnected = CurrentUser.isConnected()
//                    val userId = CurrentUser.prefs.get()
                    val postId = currentPost.id!!

                    if (userConnected) {
                        if (isChecked) {
                            addToFavClicked?.onChecked(postId)
                        } else {
                            addToFavClicked?.onUnChecked(postId)
                        }

                    } else {
                        addToFav.isEnabled = false
                    }

                }


                val features = currentPost.features
                if (features != null) {
                    detailsShortRv.apply {
                        adapter = detailsShortAdapter
                        layoutManager =
                            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                        detailsShortAdapter.setFeatures(features)
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        return PostHolder(
            SinglePostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.bind(position)
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private val exampleFilter = object : Filter() {
        override fun performFiltering(query: CharSequence?): FilterResults {

            filteredList = if (query.isNullOrEmpty()) {
                postsList
            } else {
                postsList.filter { post ->
                    //filtering by description
                    post.description.contains(query, ignoreCase = true)
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(query: CharSequence?, result: FilterResults?) {
            notifyDataSetChanged()
        }

    }
}