package com.example.realestate.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realestate.R
import com.example.realestate.data.models.CurrentUser
import com.example.realestate.data.models.SearchParams
import com.example.realestate.data.models.Type
import com.example.realestate.data.models.User
import com.example.realestate.data.remote.network.Retrofit
import com.example.realestate.data.repositories.PostsRepository
import com.example.realestate.data.repositories.StaticDataRepository
import com.example.realestate.data.repositories.UsersRepository
import com.example.realestate.databinding.FragmentHomeModifiedBinding
import com.example.realestate.ui.activities.MainActivity
import com.example.realestate.ui.adapters.PostsAdapter
import com.example.realestate.ui.viewmodels.HomeViewModel
import com.example.realestate.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class HomeFragment : Fragment(), ActivityResultListener {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private var favourites: List<String> = listOf()
    private lateinit var binding: FragmentHomeModifiedBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var searchParams: SearchParams
    private var searchTimes = 0
    private var selectedChipId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = (requireActivity() as MainActivity)
        viewModel = activity.viewModel

        postsAdapter = PostsAdapter(
            object : OnPostClickListener {
                override fun onClick(postId: String) {
                    goToPostFragment(postId)
                }
            },
            object : OnAddToFavClicked {
                override fun onChecked(postId: String, userId: String) {
                    viewModel.deleteFromFavourites(userId, postId)
                }

                override fun onUnChecked(postId: String, userId: String) {
                    viewModel.addToFavourites(userId, postId)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val activity = (requireActivity() as MainActivity)
        binding = FragmentHomeModifiedBinding.inflate(inflater, container, false)
        binding.postRv.apply {
            setAdapter(postsAdapter)
            setLayoutManager(LinearLayoutManager(requireContext()))
            addVeiledItems(10)
        }

        //initialise filter params
        searchParams = activity.params
        searchParams.location?.country = binding.countryPicker.selectedCountryName

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //to add a confirmation dialog
                    val dialog = makeDialog(
                        requireContext(),
                        object : OnDialogClicked {
                            override fun onPositiveButtonClicked() {
                                requireActivity().finish()
                            }

                            override fun onNegativeButtonClicked() {
                                // nothing
                            }
                        },
                        title = getString(R.string.app_name),
                        message = getString(R.string.Leave),
                        negativeText = getString(R.string.No),
                        positiveText = getString(R.string.Yes)
                    )
                    dialog.apply {
                        show()
                        separateButtonsBy(10)
                    }

                }

            })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = (requireActivity() as MainActivity)
        activity.setActivityResultListener(this)

        binding.apply {

            viewModel.addedToFav.observe(viewLifecycleOwner) { holder ->
                holder?.apply {
                    if (!data) {
                        requireContext().toast(getString(R.string.error), Toast.LENGTH_SHORT)
                    }
                }
            }

            viewModel.deletedFromFav.observe(viewLifecycleOwner) { holder ->
                holder?.apply {
                    if (!data) {
                        requireContext().toast(getString(R.string.error), Toast.LENGTH_SHORT)
                    }
                }
            }

            viewModel.user.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    user.favourites = listOf()
                    postsAdapter.setFavourites(user.favourites)
                } else {
                    requireContext().toast(getString(R.string.error), Toast.LENGTH_SHORT)
                }
            }

            //handle error message
            viewModel.postsMessage.observe(viewLifecycleOwner) { postsMessage ->
                Log.d(TAG, "message: $postsMessage")
                if (postsMessage.isEmpty()) {
                    this.postsMessage.visibility = View.GONE
                } else {
                    this.postsMessage.visibility = View.VISIBLE
                    this.postsMessage.text = postsMessage
                }

            }
            viewModel.categoriesMessage.observe(viewLifecycleOwner) { categoriesMessage ->
                Log.d(TAG, "message: $categoriesMessage")
                if (categoriesMessage.isEmpty()) {
                    this.categoriesMessage.visibility = View.GONE
                } else {
                    this.categoriesMessage.visibility = View.VISIBLE
                    this.categoriesMessage.text = categoriesMessage
                }
            }

            //handle loading
            viewModel.isProgressBarTurning.observe(viewLifecycleOwner) { loading ->
                binding.progressBar.isVisible = loading
                if (loading && !binding.postRv.isVeiled) {
                    binding.postRv.veil()
                }
            }

            //get the current country and send the request to get the posts of this country
            countryPicker.setOnCountryChangeListener {
                // your code to handle selected country
                countryPicker.selectedCountryName?.apply {
                    val name = this
                    searchParams.location?.country = name
                    viewModel.getPosts(searchParams, source = "countryPicker")
                }
            }

            viewModel.categoriesList.observe(viewLifecycleOwner) { categories ->
                if (categories == null) return@observe
                categoriesChipGroup.apply {
                    fillWith(categories)
                    setOnCheckedStateChangeListener { group, checkedId ->
                        if (checkedId.isEmpty()) {
                            searchParams.category = null
                            viewModel.getPosts(
                                searchParams,
                                source = "setOnCheckedStateChangeListener isEmpty = true"
                            )
                        } else {
                            val selectedChip: Chip? = group.findViewById(checkedId[0])
                            val selectedCategory: String? = selectedChip?.text?.toString()

                            searchParams.category = selectedCategory
                            viewModel.getPosts(
                                searchParams,
                                source = "setOnCheckedStateChangeListener isEmpty = false"
                            )

                        }
                    }
                }
            }

            //default selected chip
            selectedChipId = binding.all.id

            handleChips()
            handleSearch()

            //handle swipe gesture
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.getPosts(searchParams, source = "swipeRefreshLayout.setOnRefreshListener")
                if (viewModel.categoriesList.value.isNullOrEmpty()) {
                    viewModel.getCategories()
                }
            }


            //disable swipe refresh if not on top
            scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val isAtTop = scrollY == 0
                swipeRefreshLayout.isEnabled =
                    isAtTop && !postRv.canScrollVertically(-1)
            }

            viewModel.postsList.observe(viewLifecycleOwner) { posts ->

                handleHomeButton()

                //prevent scrolling bugs
//                if (posts.isNullOrEmpty()) {
//                    collapsingBar.disableScroll()
//                } else {
//                    collapsingBar.enableScroll()
//                }


                posts?.apply {

                    val recyclerViewState =
                        binding.postRv.getRecyclerView().layoutManager?.onSaveInstanceState()
                    postsAdapter.setPostsList(posts)
                    binding.postRv.getRecyclerView().layoutManager?.onRestoreInstanceState(
                        recyclerViewState
                    )
                    binding.postRv.unVeil()

                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

    }

    private fun handleSearch() {
        binding.searchView.apply {

            //search by user input
            setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.apply {
                        searchByQuery(query, "onQueryTextSubmit")
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    //TODO fix this bug where this function gets called without query change
//                    viewModel.apply {
//                        searchByQuery(newText, "onQueryTextChange")
//                    }
                    return false
                }

                fun searchByQuery(query: String?, source: String) {
                    if (query.isNullOrBlank())
                        searchParams.title = null
                    else
                        searchParams.title = query

                    viewModel.getPosts(searchParams, source = source)
                }
            })
        }

    }


    private fun AutoCompleteTextView.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun handleHomeButton() {
        (requireActivity() as MainActivity).bottomNavView.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    binding.scrollView.scrollTo(0, 0)
                }
            }
        }
    }


    private fun ChipGroup.fillWith(categories: List<String>) {
        for (category in categories) {
            val chip = Chip(context)
            chip.apply {
                text = category
                isCheckable = true
                isCheckedIconVisible = false
//                setChipDrawable(chipDrawable)
            }
            addView(chip)
        }
    }

    private fun onChipClicked(view: View) {
        val chipId = view.id

        // Unselect previously selected chip if any
        if (selectedChipId != -1) {
            val previousChip = requireActivity().findViewById<Chip>(selectedChipId)
            previousChip.isChecked = false
            previousChip.isEnabled = true
        }

        // Update selected chip
        val chip = view as Chip
        chip.isChecked = true
        chip.isEnabled = false
        selectedChipId = chipId

        // Perform actions based on the selected chip
        when (chipId) {
            binding.all.id -> {
                searchParams.type = null
            }
            binding.rent.id -> {
                searchParams.type = Type.RENT.value
            }
            binding.buy.id -> {
                searchParams.type = Type.BUY.value
            }
        }
        viewModel.getPosts(searchParams, source = "onChipClicked")
    }


    private fun handleChips() {
        for (chip in binding.chips.children) {
            chip.setOnClickListener {
                if (selectedChipId == chip.id) {
                    // Chip is already selected, do nothing
                    Log.d(TAG, "onChipClicked already selected")
                    return@setOnClickListener
                }
                onChipClicked(it)
            }
        }
    }

    override fun onResultOk(searchParams: SearchParams) {
        requestData(searchParams)
    }

    private fun requestData(params: SearchParams) {
        Log.d(TAG, "requesting Data with params: $params")
        viewModel.getPosts(searchParams, source = "onResultOk requestData")
    }

    override fun onResultCancelled() {
        Log.d(TAG, "onResultCancelled")
    }

    private fun goToPostFragment(postId: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToPostNav(postId)
        findNavController().navigate(action)
    }


}