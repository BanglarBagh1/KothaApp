package com.example.bridge

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FocusedFieldSnapshot(
    val currentText: String,
    val selectionStart: Int,
    val selectionEnd: Int,
    val isEditable: Boolean,
    val isPassword: Boolean,
    val isShowingHintText: Boolean,
    val packageName: String,
    val className: String,
    val timestamp: Long = System.currentTimeMillis()
)

object TextBridgeStateHolder {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _focusedSnapshot = MutableStateFlow<FocusedFieldSnapshot?>(null)
    val focusedSnapshot: StateFlow<FocusedFieldSnapshot?> = _focusedSnapshot.asStateFlow()

    private val _isKeyboardVisible = MutableStateFlow(false)
    val isKeyboardVisible: StateFlow<Boolean> = _isKeyboardVisible.asStateFlow()

    private val _keyboardHeightPx = MutableStateFlow(0)
    val keyboardHeightPx: StateFlow<Int> = _keyboardHeightPx.asStateFlow()

    private val _foregroundPackage = MutableStateFlow("")
    val foregroundPackage: StateFlow<String> = _foregroundPackage.asStateFlow()

    fun updateConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun updateFocusedSnapshot(snapshot: FocusedFieldSnapshot?) {
        _focusedSnapshot.value = snapshot
    }

    fun updateKeyboard(visible: Boolean, heightPx: Int) {
        _isKeyboardVisible.value = visible
        _keyboardHeightPx.value = heightPx
    }

    fun updateForegroundPackage(pkg: String) {
        _foregroundPackage.value = pkg
    }
}
