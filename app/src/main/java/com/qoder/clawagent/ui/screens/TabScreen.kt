package com.qoder.clawagent.ui.screens

import androidx.compose.runtime.Composable
import com.qoder.clawagent.AppViewModel

/**
 * The bottom navigation pages share one route type; each title maps to the
 * real screen that now backs it.
 */
@Composable
fun TabScreen(
    vm: AppViewModel,
    title: String,
    onOpenExtensions: (Int) -> Unit,
) {
    when (title) {
        "日程" -> ScheduleScreen(vm = vm, onOpenExtensions = onOpenExtensions)
        "云盘" -> DriveScreen(vm = vm, onOpenExtensions = onOpenExtensions)
        else -> ProfileScreen(vm = vm, embedded = true, onOpenExtensions = onOpenExtensions)
    }
}
