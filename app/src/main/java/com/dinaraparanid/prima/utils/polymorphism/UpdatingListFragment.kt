package com.dinaraparanid.prima.utils.polymorphism

import android.annotation.SuppressLint
import androidx.appcompat.widget.SearchView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.Serializable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * [ListFragment] with swipe fresh layout
 * (to update its [itemList])
 */

abstract class UpdatingListFragment<Act, T, A, VH, B> :
    ListFragment<Act, T, A, VH, B>(),
    SearchView.OnQueryTextListener,
    UIUpdatable<List<T>>,
    FilterFragment<T>,
    Loader<List<T>>
        where Act: AbstractActivity,
              T : Serializable,
              VH : RecyclerView.ViewHolder,
              A : RecyclerView.Adapter<VH>,
              B : ViewDataBinding {

    /** Item list for every fragment */
    protected val itemList: MutableList<T> = CopyOnWriteArrayList()

    /** Item list to use in search operations */
    protected val itemListSearch: MutableList<T> = CopyOnWriteArrayList()

    /** Swipe refresh layout to update [itemList] */
    protected abstract var updater: SwipeRefreshLayout?

    override fun onPause() {
        super.onPause()
        updater!!.clearAnimation()
        updater!!.clearDisappearingChildren()
        updater!!.clearFocus()
        updater!!.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updater = null
    }

    override fun onDestroy() {
        super.onDestroy()
        itemList.clear()
        itemListSearch.clear()
    }

    override fun onResume() {
        super.onResume()
        updater!!.isEnabled = true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null && query.isNotEmpty()) {
            val filteredModelList = filter(
                itemList,
                query
            )

            itemListSearch.clear()
            itemListSearch.addAll(filteredModelList)
            adapter?.notifyDataSetChanged()
            runOnUIThread { updateUIAsync(itemListSearch) }
            recyclerView!!.scrollToPosition(0)
        }
        return true
    }

    override fun onLowMemory() {
        super.onLowMemory()
        itemListSearch.clear()
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override val loaderContent: List<T> get() = itemList

    /** Like [UIUpdatable.updateUIAsync] but src is [itemList] */
    internal suspend fun updateUIAsync() = updateUIAsync(itemList)
}