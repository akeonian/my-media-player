package com.example.mymediaplayer.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.mymediaplayer.viewmodels.MainPagerViewModel
import java.lang.reflect.Constructor

/**
 * This fragment should be used in a pager to receive callbacks for whether this fragment is selected or not
 * */

private const val PAGER_POSITION = "pagerPosition"

class PagerDataFragment : MediaDataFragment() {

    private val pagerPosition: Int
        get() =
            arguments?.getInt(PAGER_POSITION) ?: throw IllegalStateException(
                "No argument for pager position, make" +
                        " sure you are instantiating the fragment using createInstance method"
            )

    private var selectedFragment = false
    private val pagerViewModel: MainPagerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerViewModel.selectedPosition.observe(viewLifecycleOwner) {
            if (it == pagerPosition) {
                onFragmentSelected()
                selectedFragment = true
            } else if (selectedFragment) {
                selectedFragment = false
                onFragmentDeselected()
            }
        }
    }

    protected fun onFragmentSelected() {

    }
    protected fun onFragmentDeselected() {

    }

    companion object {

        private fun <T> Class<T>.getNoArgConstructor(): Constructor<T> {
            if (constructors.size > 1 || constructors[0].isVarArgs) {
                throw IllegalArgumentException("The child of PagerDataFragment should only have one public constructor and without any arguments")
            }
            @Suppress("UNCHECKED_CAST")
            return constructors[0] as Constructor<T>
        }

        private fun <T> Class<T>.getNewInstance(): T {
            return getNoArgConstructor().newInstance()
        }

        // TODO: make subclasses of this fragment to given menu options depending on the fragment
        fun <T : PagerDataFragment> createInstance(
            fragmentClass: Class<T>,
            pagerPosition: Int,
            browseId: String? = null
        ): T {
            val f = fragmentClass.getNewInstance()
            f.arguments = MediaDataFragmentArgs(browseId).toBundle().apply {
                putInt(PAGER_POSITION, pagerPosition)
            }
            return f
        }
    }
}