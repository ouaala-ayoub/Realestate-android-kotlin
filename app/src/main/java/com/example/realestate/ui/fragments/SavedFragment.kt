package com.example.realestate.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realestate.data.models.CurrentUser
import com.example.realestate.data.remote.network.Retrofit
import com.example.realestate.data.repositories.UsersRepository
import com.example.realestate.databinding.FragmentSavedBinding
import com.example.realestate.ui.adapters.FavouritesAdapter
import com.example.realestate.ui.viewmodels.SavedViewModel
import com.example.realestate.utils.OnFavouriteClickListener

class SavedFragment : Fragment() {

    companion object {
        private const val TAG = "SavedFragment"
    }

    private lateinit var binding: FragmentSavedBinding
    private val viewModel: SavedViewModel by lazy {
        SavedViewModel(UsersRepository(Retrofit.getInstance()))
    }
    private val postsAdapter: FavouritesAdapter by lazy {
        FavouritesAdapter(
            object : OnFavouriteClickListener {
                override fun onFavouriteClicked(postId: String) {
                    openPostFragment(postId)
                }

                override fun onDeleteClickListener(postId: String) {
                    val userId = CurrentUser.prefs.get()
                    userId?.apply {
                        viewModel.deleteFromFavourites(this, postId)
                    }
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get user saved posts
        val userId = CurrentUser.prefs.get()
        userId?.apply {
            viewModel.getSavedPosts(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedBinding.inflate(inflater, container, false)

        binding.savedRv.apply {
            adapter = postsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.apply {
            postsMessage.observe(viewLifecycleOwner) { postsMessage ->
                if (postsMessage.isEmpty()) {
                    binding.postsStateMessage.visibility = View.GONE
                } else {
                    binding.postsStateMessage.visibility = View.VISIBLE
                    binding.postsStateMessage.text = postsMessage
                }
            }
            savedList.observe(viewLifecycleOwner) { savedList ->
                savedList?.apply {
                    postsAdapter.setList(this)
                }
            }
            loading.observe(viewLifecycleOwner) { loading ->
                binding.savedLoading.isVisible = loading
            }
        }

        return binding.root
    }

    private fun openPostFragment(postId: String) {
        val action = SavedFragmentDirections.actionSavedFragmentToPostNav(postId)
        findNavController().navigate(action)
    }

}