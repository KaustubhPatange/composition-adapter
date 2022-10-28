package com.example.compose_adapter.data.repo

import androidx.paging.*
import com.example.compose_adapter.data.model.PaginatedResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

typealias PaginatedCall<T> = suspend (nextPage: Int) -> T

enum class PageState {
    NOT_STARTED,
    IDLE,
    LOADING_INIT,
    LOADING_MORE,
    ERROR
}

data class PagingList<ITEM : Any>(
    val pagedData: Flow<PagingData<ITEM>>,
    val pagingState: StateFlow<PageState>,
)

class PagingRepo<ITEM : Any>(
    private val call: PaginatedCall<PaginatedResponse<ITEM>>,
    private val pageState: MutableStateFlow<PageState>,
) : PagingSource<Int, ITEM>() {

    override fun getRefreshKey(state: PagingState<Int, ITEM>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ITEM> {
        val currentPage = params.key ?: 1

        val state = if (currentPage <= 1) PageState.LOADING_INIT else PageState.LOADING_MORE
        pageState.emit(state)

        return try {
            val result = call(currentPage)

            pageState.emit(PageState.IDLE)

            val nextKey = if (currentPage * PAGE_SIZE >= result.totalResults) null else currentPage + 1
            val prevKey = if (currentPage <= 1) null else currentPage - 1

            LoadResult.Page(
                data = result.articles,
                nextKey = nextKey,
                prevKey = prevKey,
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            pageState.emit(PageState.ERROR)
            return LoadResult.Error(e)
        }
    }

    companion object {
        private const val PAGE_SIZE = 20

        class Factory<ITEM : Any>(
            private val config: PagingConfig,
            private val call: PaginatedCall<PaginatedResponse<ITEM>>
        ) {
            private val _pagingState = MutableStateFlow(PageState.NOT_STARTED)
            val pagingState = _pagingState.asStateFlow()

            val flow get() = pager!!.flow

            private var pager: Pager<Int, ITEM>? = null

            init {
                create()
            }

            fun create(): Pager<Int, ITEM> {
                val pager = Pager(
                    config = config,
                    pagingSourceFactory = { PagingRepo(call, _pagingState) }
                )
                this.pager = pager
                return pager
            }
        }

        fun <ITEM : Any> getPaged(call: PaginatedCall<PaginatedResponse<ITEM>>): PagingList<ITEM> {
            val config = PagingConfig(
                prefetchDistance = PAGE_SIZE / 2,
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
            )
            val factory = Factory(config, call)

            return PagingList(
                pagedData = factory.flow,
                pagingState = factory.pagingState,
            )
        }
    }
}