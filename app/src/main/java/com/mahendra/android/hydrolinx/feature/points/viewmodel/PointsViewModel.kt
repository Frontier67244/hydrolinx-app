package com.mahendra.android.hydrolinx.feature.points.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahendra.android.hydrolinx.core.notification.NotificationBus
import com.mahendra.android.hydrolinx.core.notification.NotificationEvent
import com.mahendra.android.hydrolinx.data.repository.LocalRepository
import com.mahendra.android.hydrolinx.feature.points.logic.RedeemRules
import com.mahendra.android.hydrolinx.feature.points.state.PointsEvent
import com.mahendra.android.hydrolinx.feature.points.state.PointsNotificationItem
import com.mahendra.android.hydrolinx.feature.points.state.PointsUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PointsViewModel(
    private val repository: LocalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PointsUiState(
            todayIso = LocalDate.now().toString(),
        ),
    )
    val uiState: StateFlow<PointsUiState> = _uiState.asStateFlow()

    private var walletCollector: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDemoPointWalletSeeded()
            subscribeWallet()
        }
    }

    fun onEvent(event: PointsEvent) {
        when (event) {
            PointsEvent.TapRedeem -> handleTapRedeem()
            PointsEvent.OpenScan -> {
                if (_uiState.value.canScan) {
                    _uiState.update {
                        it.copy(
                            isScanOpen = true,
                            isNotifyPanelOpen = false,
                        )
                    }
                }
            }

            PointsEvent.CloseScan -> _uiState.update { it.copy(isScanOpen = false) }
            is PointsEvent.SelectScanVolume -> {
                _uiState.update { it.copy(selectedScanVolumeMl = event.volumeMl) }
            }

            PointsEvent.ConfirmScan -> handleConfirmScan()
            PointsEvent.ToggleNotifyPanel -> {
                _uiState.update { it.copy(isNotifyPanelOpen = !it.isNotifyPanelOpen) }
            }
        }
    }

    private fun handleTapRedeem() {
        val snapshot = _uiState.value
        if (!snapshot.canRedeemToday) return

        val redeem = RedeemRules.calculateRedeem(
            activePoints = snapshot.activePoints,
            bankPoints = snapshot.bankPoints,
        ) ?: return

        viewModelScope.launch {
            repository.applyRedeem(
                newActive = redeem.newActive,
                newBank = redeem.newBank,
                tokenDelta = redeem.tokenGained,
                redeemDateIso = snapshot.todayIso,
            )

            val notification = notificationItem(
                title = "Redeem berhasil",
                subtitle = "Token telah siap digunakan.",
            )
            _uiState.update {
                it.copy(
                    isNotifyPanelOpen = true,
                    isScanOpen = false,
                    lastNotifyMessage = "Redeem berhasil • +${redeem.tokenGained} token",
                    notifications = listOf(notification) + it.notifications,
                )
            }

            NotificationBus.emit(
                NotificationEvent.RedeemSuccess(tokenGained = redeem.tokenGained),
            )
        }
    }

    private fun handleConfirmScan() {
        val snapshot = _uiState.value
        if (!snapshot.canScan) return

        viewModelScope.launch {
            repository.consumeToken()
            val notification = notificationItem(
                title = "Scan berhasil",
                subtitle = "Anda mendapatkan ${snapshot.selectedScanVolumeMl} Ml air.",
            )
            _uiState.update {
                it.copy(
                    isScanOpen = false,
                    isNotifyPanelOpen = true,
                    lastNotifyMessage = "Scan berhasil • ${snapshot.selectedScanVolumeMl} Ml siap diambil",
                    notifications = listOf(notification) + it.notifications,
                )
            }

            NotificationBus.emit(
                NotificationEvent.ScanSuccess(volumeMl = snapshot.selectedScanVolumeMl),
            )
        }
    }

    private fun subscribeWallet() {
        walletCollector?.cancel()
        walletCollector = viewModelScope.launch {
            repository.observePointWallet().collect { wallet ->
                _uiState.update {
                    it.copy(
                        totalRedeem = wallet.totalRedeem,
                        activePoints = wallet.activePoints,
                        bankPoints = wallet.bankPoints,
                        tokenCount = wallet.tokenCount,
                        lastRedeemDateIso = wallet.lastRedeemDateIso,
                    )
                }
            }
        }
    }

    private fun notificationItem(
        title: String,
        subtitle: String,
        epochMillis: Long = System.currentTimeMillis(),
    ): PointsNotificationItem {
        val time = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        return PointsNotificationItem(
            id = epochMillis,
            title = title,
            subtitle = subtitle,
            timeLabel = CLOCK_FORMAT.format(time),
        )
    }

    override fun onCleared() {
        walletCollector?.cancel()
        super.onCleared()
    }

    companion object {
        private val CLOCK_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")

        fun factory(repository: LocalRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PointsViewModel(repository) as T
                }
            }
    }
}
