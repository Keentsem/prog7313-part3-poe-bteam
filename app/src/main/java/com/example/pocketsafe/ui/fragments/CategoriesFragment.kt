package com.example.pocketsafe.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.databinding.FragmentCategoriesBinding
import com.example.pocketsafe.ui.adapters.CategoryAdapter
import com.example.pocketsafe.ui.dialogs.CategorySelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesFragment : Fragment(), CategorySelectionDialog.OnCategorySelectedListener {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var categoryDao: CategoryDao
    
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CategoryAdapter()

        binding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@CategoriesFragment.adapter
        }

        binding.fabAddCategory.setOnClickListener {
            CategorySelectionDialog.newInstance().show(
                childFragmentManager,
                CategorySelectionDialog::class.java.simpleName
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            categoryDao.getAllCategories().collectLatest { categories ->
                adapter.submitList(categories)
            }
        }
    }

    override fun onCategorySelected(category: Category) {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryDao.insertCategory(category)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 