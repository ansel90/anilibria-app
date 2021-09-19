package ru.radiationx.data.datasource.holders

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.donation.DonationDetailResponse
import ru.radiationx.data.entity.domain.donation.DonationInfo

interface DonationHolder {
    fun observe(): Observable<DonationInfo>
    fun get(): Single<DonationInfo>
    fun save(data: DonationInfo): Completable
    fun delete(): Completable
}