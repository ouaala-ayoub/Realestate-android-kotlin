package com.example.realestate.ui.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.realestate.R
import com.example.realestate.data.models.ImagesMedia
import com.example.realestate.databinding.AddMoreLayoutBinding
import com.example.realestate.databinding.SingleImagePickedBinding
import com.example.realestate.ui.viewmodels.postaddmodels.ImagesSelectModel
import com.example.realestate.utils.loadImageUri
import com.example.realestate.utils.swap

class ImagesSelectAdapter(
    val imagesNumber: Int,
    val viewModel: ImagesSelectModel,
    private val onAddMoreClicked: AddMoreClicked
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG = "ImagesSelectAdapter"
    }

    private var imagesList: ImagesMedia = ImagesMedia()

    fun setImagesList(list: List<Uri?>) {
        imagesList.uriList = list.toMutableList()
        if (imagesList.uriList.isNotEmpty())
            imagesList.uriList.add(null)
        else
            imagesList.uriList.remove(null)
        Log.d(TAG, "setImagesList: ${imagesList.uriList}")
        notifyDataSetChanged()
    }

    fun getSelectedUris(): List<Uri> {
        val result = imagesList.getNonNullElements()
        if (result.size > 1)
            result.swap(imagesList.selectedPosition, 0)

        return result
    }

    inner class AddMoreHolder(private val binding: AddMoreLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.addMoreButton.setOnClickListener {
                onAddMoreClicked.onClicked()
            }
        }
    }

    inner class ImagesSelectHolder(private val binding: SingleImagePickedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun deleteElementAt(position: Int) {
            imagesList.uriList.apply {
                if (imagesList.selectedPosition == position)
                    imagesList.selectedPosition = 0

                viewModel.deleteElementAt(position)
            }
        }

        private fun checkImageAt(
            position: Int,
        ) {
            val oldSelected = imagesList.selectedPosition
            imagesList.selectedPosition = position

            notifyItemChanged(position)
            notifyItemChanged(oldSelected)

        }

        fun bind(position: Int) {
            val currentImage = imagesList.uriList[position]
            val isNull = currentImage == null
            val isSelected = position == imagesList.selectedPosition

            binding.apply {
                //checkbox handling
                checkBox.apply {
                    isChecked = isSelected && !isNull
                    isEnabled = !isNull
                    isClickable = !isSelected
                }

                //handle delete
                delete.setOnClickListener {
                    if (currentImage != null) {
                        deleteElementAt(position)
                    }
                }

                //load image
                if (currentImage == null) {
                    selectedImage.setImageResource(R.drawable.image_cadre)
                } else {
                    selectedImage.loadImageUri(currentImage)
                }

                //handle checking
                if (!isSelected && !isNull) {
                    wholeImage.setOnClickListener {
                        checkImageAt(position)
                    }
                    checkBox.setOnClickListener {
                        checkImageAt(position)
                    }
                }

            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return when (viewType) {
            ViewType.NORMAL.ordinal -> ImagesSelectHolder(
                SingleImagePickedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> {
                AddMoreHolder(
                    AddMoreLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }


    }

    override fun getItemCount() = imagesList.uriList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ViewType.NORMAL.ordinal -> {
                val myHolder = holder as ImagesSelectHolder
                myHolder.bind(position)
            }
            else -> {
                val myHolder = holder as AddMoreHolder
                myHolder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (imagesList.uriList[position] != null)
            ViewType.NORMAL.ordinal
        else
            ViewType.ADD_MORE.ordinal
    }
}

enum class ViewType {
    NORMAL, ADD_MORE
}

interface AddMoreClicked {
    fun onClicked()
}
