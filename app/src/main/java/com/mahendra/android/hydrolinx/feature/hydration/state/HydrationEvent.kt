package com.mahendra.android.hydrolinx.feature.hydration.state

sealed interface HydrationEvent {
    data object TapLetsGo : HydrationEvent
    data object OpenConfigure : HydrationEvent
    data class UpdateDraftTarget(val ml: Int) : HydrationEvent
    data object SaveTarget : HydrationEvent

    data object OpenDrinkSheet : HydrationEvent
    data object CloseDrinkSheet : HydrationEvent
    data class SelectDrinkSize(val ml: Int) : HydrationEvent
    data object ConfirmDrink : HydrationEvent

    data object ToggleHistoryPanel : HydrationEvent
    data object ToggleNotifyPanel : HydrationEvent
    data object DismissNotifyPanel : HydrationEvent
}
