package ru.radiationx.anilibria.presentation.configuring

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.repository.ConfigurationRepository
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class ConfiguringPresenter @Inject constructor(
        private val router: Router,
        private val apiConfig: ApiConfig,
        private val configurationRepository: ConfigurationRepository,
        private val schedulers: SchedulersProvider,
        private val errorHandler: IErrorHandler
) : BasePresenter<ConfiguringView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        loadConfig()
        //checkAvail()
        //checkProxies()
    }

    fun loadConfig() {
        configurationRepository
                .getConfiguration()
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    viewState.showStatus("Загрузка данных")
                }
                .subscribe({
                    val addresses = apiConfig.getAddresses()
                    val proxies = addresses.sumBy { it.proxies.size }
                    viewState.showStatus("Загружено адресов: ${addresses.size}; прокси: $proxies")
                    checkAvail()
                }, {
                    viewState.showStatus("Ошибка загрузки данных")
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    fun checkAvail() {
        val addresses = apiConfig.getAddresses()
        Observable
                .fromIterable(addresses)
                .concatMapSingle { address ->
                    configurationRepository.checkAvailable(address.api).map { Pair(address, it) }
                }
                .filter { it.second }
                .map { it.first }
                .toList()
                .observeOn(schedulers.ui())
                .subscribe({
                    viewState.showStatus("Доступнные адреса: ${it.size}")
                    Log.e("boboob", "checkAvail ${it.joinToString()}")
                    if (it.isNotEmpty()) {
                        apiConfig.updateActiveAddress(it.first())
                        apiConfig.updateNeedConfig(false)
                    } else {
                        checkProxies()
                    }
                }, {
                    viewState.showStatus("Ошибка проверки доступности адресов")
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    fun checkProxies() {
        val proxies = apiConfig.getAddresses().map { it.proxies }.reduce { acc, list -> acc.plus(list) }
        Observable
                .fromIterable(proxies)
                .concatMapSingle { proxy ->
                    configurationRepository.getPingHost(proxy.ip).map { Pair(proxy, it) }
                }
                .filter { !it.second.hasError() }
                .toList()
                .observeOn(schedulers.ui())
                .subscribe({
                    it.forEach {
                        apiConfig.setProxyPing(it.first, it.second.timeTaken)
                    }
                    val bestProxy = it.minBy { it.second.timeTaken }
                    if (bestProxy != null) {
                        apiConfig.updateNeedConfig(false)
                    }
                    viewState.showStatus("Доступнные прокси: ${it.size}; будет использован ${bestProxy?.first}")
                }, {
                    viewState.showStatus("Ошибка проверки доступности прокси-серверов")
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

}