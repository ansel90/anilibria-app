package ru.radiationx.anilibria.model.loading

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposables

class DataLoadingController<T>(
    private val firstPage: Int = 1,
    private val dataSource: (PageLoadParams) -> Single<ScreenStateAction.Data<T>>
) {

    private val stateRelay = BehaviorRelay.create<DataLoadingState<T>>()

    private var currentPage = firstPage

    private var dataDisposable = Disposables.disposed()

    fun observeState(): Observable<DataLoadingState<T>> {
        return stateRelay.hide().distinctUntilChanged()
    }

    fun refresh() {
        loadPage(firstPage)
    }

    fun loadMore() {
        loadPage(currentPage + 1)
    }

    fun release() {
        dataDisposable.dispose()
    }

    private fun loadPage(page: Int) {
        if (!dataDisposable.isDisposed) {
            return
        }

        val params = createPageLoadParams(page)

        val startLoadingAction: ScreenStateAction<T>? = when {
            params.isEmptyLoading -> ScreenStateAction.EmptyLoading()
            params.isRefreshLoading -> ScreenStateAction.Refresh()
            params.isMoreLoading -> ScreenStateAction.MoreLoading()
            else -> null
        }
        if (startLoadingAction != null) {
            updateStateByAction(startLoadingAction)
        }

        dataDisposable = dataSource
            .invoke(params)
            .subscribe({ dataAction ->
                updateStateByAction(dataAction)
                currentPage = page
            }) { throwable ->
                updateStateByAction(ScreenStateAction.Error(throwable))
            }
    }

    private fun createPageLoadParams(page: Int): PageLoadParams {
        val isFirstPage = page == firstPage
        val isEmptyData = stateRelay.value?.data == null
        return PageLoadParams(
            page = page,
            isFirstPage = isFirstPage,
            isDataEmpty = isEmptyData,
            isEmptyLoading = isFirstPage && isEmptyData,
            isRefreshLoading = isFirstPage && !isEmptyData,
            isMoreLoading = !isFirstPage
        )
    }

    private fun updateState(block: (DataLoadingState<T>) -> DataLoadingState<T>) {
        val newState = block.invoke(stateRelay.value ?: DataLoadingState())
        stateRelay.accept(newState)
    }

    private fun updateStateByAction(action: ScreenStateAction<T>) {
        updateState {
            it.applyAction(action)
        }
    }

}