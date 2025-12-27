package re.limus.timas.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import re.limus.timas.annotations.UiCategory
import re.limus.timas.databinding.FragmentHookListBinding
import re.limus.timas.hook.manager.HookManager
import re.limus.timas.ui.adapter.HookAdapter

class HookListFragment : Fragment() {

    private var _binding: FragmentHookListBinding? = null
    private val binding get() = _binding!!

    private val category: UiCategory? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_CATEGORY, UiCategory::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_CATEGORY) as? UiCategory
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val filteredHooks = category?.let {
            HookManager.getAllHooks().filter { it.category == category }
        } ?: emptyList()

        binding.recyclerViewHooks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = HookAdapter(filteredHooks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        @JvmStatic
        fun newInstance(category: UiCategory) = HookListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_CATEGORY, category)
            }
        }
    }
}