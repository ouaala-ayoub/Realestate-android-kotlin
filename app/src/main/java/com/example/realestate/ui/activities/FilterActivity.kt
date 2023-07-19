package com.example.realestate.ui.activities

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.realestate.R
import com.example.realestate.data.models.SearchParams
import com.example.realestate.data.remote.network.Retrofit
import com.example.realestate.data.repositories.StaticDataRepository
import com.example.realestate.databinding.ActivityFilterBinding
import com.example.realestate.ui.viewmodels.FilterModel
import com.example.realestate.utils.setWithList

class FilterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SearchActivity"
    }

    private lateinit var binding: ActivityFilterBinding
    private lateinit var filterModel: FilterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFilterBinding.inflate(layoutInflater)
        filterModel = FilterModel(StaticDataRepository(Retrofit.getInstance()))


        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("search_params", SearchParams::class.java)
        } else {
            intent.getParcelableExtra("search_params")
        }
        Log.d(TAG, "params: $params")

        params?.apply {
            initialiseViews(this)
        }

        filterModel.categoriesList.observe(this) { categories ->
            if (!categories.isNullOrEmpty()) {
                binding.categoryEditText.setWithList(categories)
            } else {
                binding.categoryEditText.apply {
                    setText(getString(R.string.error_getting_type))
                    isEnabled = false
                }
            }
        }


        binding.search.setOnClickListener {
            //get the search params
            val searchParams = SearchParams(
                title = (0..10).random().toString()
            )
            intent.putExtra("search_params", searchParams)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        setContentView(binding.root)
    }

    private fun initialiseViews(searchParams: SearchParams) {
        binding.apply {

        }
    }
}