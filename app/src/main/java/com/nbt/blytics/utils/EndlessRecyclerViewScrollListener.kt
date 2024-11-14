package com.nbt.blytics.utils


import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


/**
 * @Details used for pagination in recylerview
 * @author Iftekhar Ahmed
 * @Date 04-October -2019
 */

abstract class EndlessRecyclerViewScrollListener : RecyclerView.OnScrollListener {
	// The minimum amount of items to have below your current scroll position
	// before loading more.
	private var visibleThreshold = 2

	// The current offset index of data you have loaded
	private var currentPage = 0

	// The total number of items in the dataset after the last load
	private var previousTotalItemCount = 0

	// True if we are still waiting for the last set of data to load.
	private var loading = true

	// Sets the starting page index
	private var startingPageIndex = 0
	private var totalItemCount = 0
	private var lastVisibleItemPosition = 0

	var mLayoutManager: RecyclerView.LayoutManager

	constructor(layoutManager: GridLayoutManager) {
		this.mLayoutManager = layoutManager
	}

	constructor(layoutManager: StaggeredGridLayoutManager) {
		this.mLayoutManager = layoutManager
		visibleThreshold = visibleThreshold * layoutManager.spanCount
	}

	constructor(layoutManager: LinearLayoutManager) {
		this.mLayoutManager = layoutManager
	}

	fun reset() {
		totalItemCount = 0
		lastVisibleItemPosition = 0
		currentPage = 0
		previousTotalItemCount = 0
		loading = true
		visibleThreshold = 5
		startingPageIndex = 0
	}

	fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
		var maxSize = 0
		for (i in lastVisibleItemPositions.indices) {
			if (i == 0) {
				maxSize = lastVisibleItemPositions[i]
			} else if (lastVisibleItemPositions[i] > maxSize) {
				maxSize = lastVisibleItemPositions[i]
			}
		}
		return maxSize
	}

	override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
		lastVisibleItemPosition = 0
		totalItemCount = mLayoutManager.itemCount

		if (mLayoutManager is LinearLayoutManager) {
			lastVisibleItemPosition =
				(mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
		} else if (mLayoutManager is GridLayoutManager) {
			lastVisibleItemPosition =
				(mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
		} else if (mLayoutManager is StaggeredGridLayoutManager) {
			val lastVisibleItemPositions =
				(mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
			// get maximum element within the list
			lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
		}
		if (totalItemCount < previousTotalItemCount) {
			this.currentPage = this.startingPageIndex
			this.previousTotalItemCount = totalItemCount
			if (totalItemCount == 0) {
				this.loading = true
			}
		}
		if (loading && totalItemCount > previousTotalItemCount) {
			loading = false
			previousTotalItemCount = totalItemCount
		}

		if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
			currentPage++
			onLoadMore(currentPage, totalItemCount)
			loading = true
		}
	}

	abstract fun onLoadMore(page: Int, totalItemsCount: Int)
}