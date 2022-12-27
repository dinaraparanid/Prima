package com.dinaraparanid.prima.mvvmp.view.fragments

import android.os.Parcelable
import androidx.appcompat.widget.SearchView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dinaraparanid.prima.dialogs.createAndShowAwaitDialog
import com.dinaraparanid.prima.mvvmp.presenters.BasePresenter
import com.dinaraparanid.prima.mvvmp.ui_handlers.UIHandler
import com.dinaraparanid.prima.mvvmp.view.Loader
import com.dinaraparanid.prima.mvvmp.view.UIUpdatable
import com.dinaraparanid.prima.mvvmp.view_models.ObservableViewModel
import com.dinaraparanid.prima.utils.extensions.replace
import com.dinaraparanid.prima.utils.polymorphism.AbstractActivity
import com.dinaraparanid.prima.utils.polymorphism.AsyncListDifferAdapter
import com.dinaraparanid.prima.utils.polymorphism.runOnUIThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.Collections

/**
 * [ListFragment] with swipe fresh layout
 * (to update its [itemList])
 */

abstract class UpdatingListFragment<P, VM, H, B, Act, T, Adp, VH> :
    ListFragment<P, VM, H, B, Act, T, Adp, VH>(),
    SearchView.OnQueryTextListener,
    UIUpdatable<T>,
    FilterFragment<T>,
    Loader<T>
        where P : BasePresenter,
              VM : ObservableViewModel<P>,
              H : UIHandler,
              B : ViewDataBinding,
              Act : AbstractActivity,
              T : Parcelable,
              VH : RecyclerView.ViewHolder,
              Adp : AsyncListDifferAdapter<T, VH> {

    /** Item list to show data */
    protected val itemList: MutableList<T> = Collections.synchronizedList(mutableListOf())

    /** Item list for search operations */
    protected val itemListSearch: MutableList<T> = Collections.synchronizedList(mutableListOf())

    private fun freeMemory() {
        Glide.get(requireContext()).clearMemory()
        itemListSearch.clear()
    }

    override fun onStop() {
        super.onStop()
        freeMemory()
    }

    override fun onDestroyView() {
        freeMemory()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        itemList.clear()
    }

    private fun CoroutineScope.filterAsync(query: String) =
        async(Dispatchers.Default) { synchronized(itemList) { filter(itemList, query) } }

    override fun onQueryTextChange(query: String?): Boolean {
        runOnUIThread {
            query
                ?.takeIf(String::isNotEmpty)
                ?.let { filterAsync(it) }
                ?.await()
                ?.let(itemListSearch::replace)
        }

        return true
    }

    final override fun onLowMemory() {
        super.onLowMemory()
        freeMemory()
    }

    final override fun onQueryTextSubmit(query: String?) = false

    final override val loadedContent: List<T> get() = itemList

    /**
     * Loads content with [loadAsync]
     * and updates UI with [updateUIAsync]
     */

    fun updateUIOnChangeContentAsync() = runOnUIThread {
        val task = loadAsync()
        val progress = createAndShowAwaitDialog(requireContext(), false)

        val newItems = task.await()
        progress.dismiss()
        updateUIAsync(newItems)
    }
}