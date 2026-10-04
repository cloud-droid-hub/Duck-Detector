/*
 * Copyright 2026 Duck Apps Contributor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eltavine.duckdetector.ui

// Copyright (c) 2025-2026 fei_cong(https://github.com/feicong/feicong-course)

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eltavine.duckdetector.BuildConfig
import com.eltavine.duckdetector.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import com.eltavine.duckdetector.core.notifications.ScanNotificationPermissions
import com.eltavine.duckdetector.core.notifications.ScanProgressNotificationSnapshot
import com.eltavine.duckdetector.core.notifications.ScanProgressNotifier
import com.eltavine.duckdetector.core.ui.components.AlphaBuildBanner
import com.eltavine.duckdetector.core.ui.components.DetectorAutoExpansionDirective
import com.eltavine.duckdetector.core.ui.components.LocalDetectorAutoExpansionDirective
import com.eltavine.duckdetector.core.ui.components.ScreenshotWatermarkOverlay
import com.eltavine.duckdetector.core.ui.openExternalUri
import com.eltavine.duckdetector.features.bootloader.presentation.BootloaderUiStage
import com.eltavine.duckdetector.features.bootloader.presentation.BootloaderUiState
import com.eltavine.duckdetector.features.bootloader.presentation.BootloaderViewModel
import com.eltavine.duckdetector.features.customrom.presentation.CustomRomUiStage
import com.eltavine.duckdetector.features.customrom.presentation.CustomRomUiState
import com.eltavine.duckdetector.features.customrom.presentation.CustomRomViewModel
import com.eltavine.duckdetector.features.dashboard.ui.DashboardScreen
import com.eltavine.duckdetector.features.dashboard.ui.model.DashboardDetectorCardEntry
import com.eltavine.duckdetector.features.dashboard.ui.model.DashboardDetectorContribution
import com.eltavine.duckdetector.features.dashboard.ui.model.DashboardUiState
import com.eltavine.duckdetector.features.dashboard.ui.model.buildDashboardFindings
import com.eltavine.duckdetector.features.dashboard.ui.model.buildDashboardOverview
import com.eltavine.duckdetector.features.dashboard.ui.model.sortDashboardDetectorCards
import com.eltavine.duckdetector.features.deviceinfo.presentation.DeviceInfoViewModel
import com.eltavine.duckdetector.features.dangerousapps.presentation.DangerousAppsUiStage
import com.eltavine.duckdetector.features.dangerousapps.presentation.DangerousAppsUiState
import com.eltavine.duckdetector.features.dangerousapps.presentation.DangerousAppsViewModel
import com.eltavine.duckdetector.features.kernelcheck.presentation.KernelCheckUiStage
import com.eltavine.duckdetector.features.kernelcheck.presentation.KernelCheckUiState
import com.eltavine.duckdetector.features.kernelcheck.presentation.KernelCheckViewModel
import com.eltavine.duckdetector.features.lsposed.presentation.LSPosedUiStage
import com.eltavine.duckdetector.features.lsposed.presentation.LSPosedUiState
import com.eltavine.duckdetector.features.lsposed.presentation.LSPosedViewModel
import com.eltavine.duckdetector.features.memory.presentation.MemoryUiStage
import com.eltavine.duckdetector.features.memory.presentation.MemoryUiState
import com.eltavine.duckdetector.features.memory.presentation.MemoryViewModel
import com.eltavine.duckdetector.features.mount.presentation.MountUiStage
import com.eltavine.duckdetector.features.mount.presentation.MountUiState
import com.eltavine.duckdetector.features.mount.presentation.MountViewModel
import com.eltavine.duckdetector.features.nativeroot.presentation.NativeRootUiStage
import com.eltavine.duckdetector.features.nativeroot.presentation.NativeRootUiState
import com.eltavine.duckdetector.features.nativeroot.presentation.NativeRootViewModel
import com.eltavine.duckdetector.features.playintegrityfix.presentation.PlayIntegrityFixUiStage
import com.eltavine.duckdetector.features.playintegrityfix.presentation.PlayIntegrityFixUiState
import com.eltavine.duckdetector.features.playintegrityfix.presentation.PlayIntegrityFixViewModel
import com.eltavine.duckdetector.features.selinux.presentation.SelinuxUiStage
import com.eltavine.duckdetector.features.selinux.presentation.SelinuxUiState
import com.eltavine.duckdetector.features.selinux.presentation.SelinuxViewModel
import com.eltavine.duckdetector.features.settings.ui.SettingsScreen
import com.eltavine.duckdetector.features.settings.ui.model.SettingsUiState
import com.eltavine.duckdetector.features.su.presentation.SuUiStage
import com.eltavine.duckdetector.features.su.presentation.SuUiState
import com.eltavine.duckdetector.features.su.presentation.SuViewModel
import com.eltavine.duckdetector.features.systemproperties.presentation.SystemPropertiesUiStage
import com.eltavine.duckdetector.features.systemproperties.presentation.SystemPropertiesUiState
import com.eltavine.duckdetector.features.systemproperties.presentation.SystemPropertiesViewModel
import com.eltavine.duckdetector.features.tee.data.preferences.TeeNetworkConsentStore
import com.eltavine.duckdetector.features.tee.data.preferences.TeeNetworkPrefs
import com.eltavine.duckdetector.features.tee.presentation.TeeUiStage
import com.eltavine.duckdetector.features.tee.presentation.TeeUiState
import com.eltavine.duckdetector.features.tee.presentation.TeeViewModel
import com.eltavine.duckdetector.features.update.presentation.UpdateDownloadResolution
import com.eltavine.duckdetector.features.update.presentation.UpdateViewModel
import com.eltavine.duckdetector.features.update.ui.NightlyUpdateDialog
import com.eltavine.duckdetector.features.virtualization.presentation.VirtualizationUiStage
import com.eltavine.duckdetector.features.virtualization.presentation.VirtualizationUiState
import com.eltavine.duckdetector.features.virtualization.presentation.VirtualizationViewModel
import com.eltavine.duckdetector.features.zygisk.presentation.ZygiskUiStage
import com.eltavine.duckdetector.features.zygisk.presentation.ZygiskUiState
import com.eltavine.duckdetector.features.zygisk.presentation.ZygiskViewModel
import com.eltavine.duckdetector.ui.shell.AppDestination
import com.eltavine.duckdetector.ui.shell.attentionDetectorTitles
import com.eltavine.duckdetector.ui.shell.FloatingAppTabSwitcher
import kotlinx.coroutines.launch

@Composable
fun DuckDetectorApp() {
    val blacklistMatch = remember { DeviceBlacklist.matchCurrentDevice() }
    if (blacklistMatch != null) {
        Surface {
            BlockedDeviceScreen(
                match = blacklistMatch,
                modifier = Modifier.fillMaxSize(),
            )
        }
        return
    }

    val context = LocalContext.current
    val appContext = context.applicationContext
    val consentStore = remember(appContext) { TeeNetworkConsentStore.getInstance(appContext) }
    val teePrefs by produceState<TeeNetworkPrefs?>(
        initialValue = null,
        key1 = consentStore,
    ) {
        consentStore.prefs.collect { currentPrefs ->
            value = currentPrefs
        }
    }
    val notifyState = remember { ScanNotificationPermissions.read(appContext) }
    var destination by rememberSaveable { mutableStateOf(AppDestination.MAIN) }

    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            if (teePrefs == null) {
                StartupLoading(modifier = Modifier.fillMaxSize())
            } else {
                AppReadyShell(
                    destination = destination,
                    selectDest = { selected -> destination = selected },
                    networkPrefs = requireNotNull(teePrefs),
                    consentStore = consentStore,
                    notifyState = notifyState,
                )
            }

            ScreenshotWatermarkOverlay()

            if (teePrefs != null) {
                AlphaBuildBanner()
            }
        }
    }
}

@Composable
private fun AppReadyShell(
    destination: AppDestination,
    selectDest: (AppDestination) -> Unit,
    networkPrefs: TeeNetworkPrefs,
    consentStore: TeeNetworkConsentStore,
    notifyState: com.eltavine.duckdetector.core.notifications.ScanNotificationPermissionState,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val updateError = stringResource(R.string.update_open_failed)
    val scope = rememberCoroutineScope()
    var resolvingUpdate by remember { mutableStateOf(false) }
    val notifier = remember(appContext) { ScanProgressNotifier(appContext) }
    val updateFactory = remember(context) { UpdateViewModel.factory(context) }
    val bootFactory = remember(context) { BootloaderViewModel.factory(context) }
    val teeFactory = remember(context) { TeeViewModel.factory(context) }
    val customRomFactory = remember(context) { CustomRomViewModel.factory(context) }
    val appsFactory = remember(context) { DangerousAppsViewModel.factory(context) }
    val infoFactory = remember(context) { DeviceInfoViewModel.factory(context) }
    val kernelFactory = remember { KernelCheckViewModel.factory() }
    val lsposedFactory = remember(context) { LSPosedViewModel.factory(context) }
    val memoryFactory = remember { MemoryViewModel.factory() }
    val mountFactory = remember(context) { MountViewModel.factory(context) }
    val rootFactory = remember(context) { NativeRootViewModel.factory(context) }
    val playFactory = remember { PlayIntegrityFixViewModel.factory() }
    val selinuxFactory = remember(context) { SelinuxViewModel.factory(context) }
    val suFactory = remember { SuViewModel.factory() }
    val propsFactory = remember { SystemPropertiesViewModel.factory() }
    val virtFactory = remember(context) { VirtualizationViewModel.factory(context) }
    val zygiskFactory = remember(context) { ZygiskViewModel.factory(context) }
    val updateViewModel: UpdateViewModel = viewModel(factory = updateFactory)
    val bootVm: BootloaderViewModel = viewModel(factory = bootFactory)
    val teeViewModel: TeeViewModel = viewModel(factory = teeFactory)
    val customRomVm: CustomRomViewModel = viewModel(factory = customRomFactory)
    val appsVm: DangerousAppsViewModel = viewModel(factory = appsFactory)
    val infoVm: DeviceInfoViewModel = viewModel(factory = infoFactory)
    val kernelVm: KernelCheckViewModel = viewModel(factory = kernelFactory)
    val lsposedViewModel: LSPosedViewModel = viewModel(factory = lsposedFactory)
    val memoryViewModel: MemoryViewModel = viewModel(factory = memoryFactory)
    val mountViewModel: MountViewModel = viewModel(factory = mountFactory)
    val rootVm: NativeRootViewModel = viewModel(factory = rootFactory)
    val playVm: PlayIntegrityFixViewModel = viewModel(factory = playFactory)
    val selinuxViewModel: SelinuxViewModel = viewModel(factory = selinuxFactory)
    val suViewModel: SuViewModel = viewModel(factory = suFactory)
    val propsVm: SystemPropertiesViewModel = viewModel(factory = propsFactory)
    val virtVm: VirtualizationViewModel = viewModel(factory = virtFactory)
    val zygiskViewModel: ZygiskViewModel = viewModel(factory = zygiskFactory)
    val teeUiState by teeViewModel.uiState.collectAsState()
    val customRomUiState by customRomVm.uiState.collectAsState()
    val appsUiState by appsVm.uiState.collectAsState()
    val infoUiState by infoVm.uiState.collectAsState()
    val kernelUiState by kernelVm.uiState.collectAsState()
    val lsposedUiState by lsposedViewModel.uiState.collectAsState()
    val memoryUiState by memoryViewModel.uiState.collectAsState()
    val mountUiState by mountViewModel.uiState.collectAsState()
    val rootUiState by rootVm.uiState.collectAsState()
    val playUiState by playVm.uiState.collectAsState()
    val selinuxUiState by selinuxViewModel.uiState.collectAsState()
    val suUiState by suViewModel.uiState.collectAsState()
    val propsUiState by propsVm.uiState.collectAsState()
    val virtUiState by virtVm.uiState.collectAsState()
    val zygiskUiState by zygiskViewModel.uiState.collectAsState()
    val bootUiState by bootVm.uiState.collectAsState()
    val updateUiState by updateViewModel.uiState.collectAsState()

    val contributions = remember(
        bootUiState,
        teeUiState,
        customRomUiState,
        appsUiState,
        infoUiState,
        kernelUiState,
        lsposedUiState,
        memoryUiState,
        mountUiState,
        rootUiState,
        playUiState,
        selinuxUiState,
        suUiState,
        propsUiState,
        virtUiState,
        zygiskUiState,
    ) {
        listOf(
            buildBootloaderContribution(bootUiState),
            buildCustomRomContribution(customRomUiState),
            buildDangerousAppsContribution(appsUiState),
            buildKernelCheckContribution(kernelUiState),
            buildLsposedContribution(lsposedUiState),
            buildMemoryContribution(memoryUiState),
            buildMountContribution(mountUiState),
            buildNativeRootContribution(rootUiState),
            buildPlayIntegrityFixContribution(playUiState),
            buildSelinuxContribution(selinuxUiState),
            buildSuContribution(suUiState),
            buildSystemPropertiesContribution(propsUiState),
            buildTeeContribution(teeUiState),
            buildVirtualizationContribution(virtUiState),
            buildZygiskContribution(zygiskUiState),
        )
    }
    val dashboardLoading = contributions.any { !it.ready }
    var scanStartedAt by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var scanFinishedAt by remember { mutableStateOf<Long?>(null) }
    var scanEpoch by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(dashboardLoading) {
        if (dashboardLoading) {
            if (scanFinishedAt != null) {
                scanStartedAt = SystemClock.elapsedRealtime()
                scanFinishedAt = null
                scanEpoch = null
            }
        } else if (scanFinishedAt == null) {
            scanFinishedAt = SystemClock.elapsedRealtime()
            scanEpoch = System.currentTimeMillis()
        }
    }

    val scanDuration = scanFinishedAt
        ?.minus(scanStartedAt)
        ?.coerceAtLeast(0L)

    val dashboardState = remember(
        contributions,
        scanDuration,
        scanEpoch,
        dashboardLoading,
        infoUiState,
        bootUiState,
        teeUiState,
        customRomUiState,
        appsUiState,
        kernelUiState,
        lsposedUiState,
        memoryUiState,
        mountUiState,
        rootUiState,
        playUiState,
        selinuxUiState,
        suUiState,
        propsUiState,
        virtUiState,
        zygiskUiState,
    ) {
        DashboardUiState(
            overview = buildDashboardOverview(
                contributions = contributions,
                scanDurationMillis = scanDuration,
                scanCompletedAtEpochMillis = scanEpoch,
            ),
            topFindings = buildDashboardFindings(contributions),
            detectorCards = sortDashboardDetectorCards(
                listOf(
                    DashboardDetectorCardEntry.Bootloader(bootUiState.cardModel),
                    DashboardDetectorCardEntry.CustomRom(customRomUiState.cardModel),
                    DashboardDetectorCardEntry.DangerousApps(appsUiState.cardModel),
                    DashboardDetectorCardEntry.KernelCheck(kernelUiState.cardModel),
                    DashboardDetectorCardEntry.LSPosed(lsposedUiState.cardModel),
                    DashboardDetectorCardEntry.Memory(memoryUiState.cardModel),
                    DashboardDetectorCardEntry.Mount(mountUiState.cardModel),
                    DashboardDetectorCardEntry.NativeRoot(rootUiState.cardModel),
                    DashboardDetectorCardEntry.PlayIntegrityFix(playUiState.cardModel),
                    DashboardDetectorCardEntry.Selinux(selinuxUiState.cardModel),
                    DashboardDetectorCardEntry.Su(suUiState.cardModel),
                    DashboardDetectorCardEntry.SystemProperties(propsUiState.cardModel),
                    DashboardDetectorCardEntry.Tee(teeUiState.cardModel),
                    DashboardDetectorCardEntry.Virtualization(virtUiState.cardModel),
                    DashboardDetectorCardEntry.Zygisk(zygiskUiState.cardModel),
                ),
            ),
            deviceInfoCard = infoUiState.cardModel,
            isLoading = dashboardLoading,
        )
    }
    val settingsState = remember(networkPrefs.consentGranted, updateUiState.status) {
        SettingsUiState(
            isCrlNetworkingEnabled = networkPrefs.consentGranted,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            buildTimeUtc = BuildConfig.BUILD_TIME_UTC,
            buildHash = BuildConfig.BUILD_HASH,
            updateStatus = updateUiState.status,
        )
    }
    val attentionTitles = remember(contributions) {
        attentionDetectorTitles(contributions)
    }
    var expandTitles by rememberSaveable { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(dashboardLoading, attentionTitles) {
        expandTitles = if (dashboardLoading) emptyList() else attentionTitles.toList()
    }

    val notifySnapshot = remember(
        contributions.size,
        contributions.count { it.ready },
        dashboardState.overview,
        dashboardLoading,
    ) {
        ScanProgressNotificationSnapshot(
            totalDetectorCount = contributions.size,
            readyDetectorCount = contributions.count { it.ready },
            dashboardOverview = dashboardState.overview,
            scanning = dashboardLoading,
        )
    }

    LaunchedEffect(notifyState, notifySnapshot) {
        notifier.update(
            permissionState = notifyState,
            snapshot = notifySnapshot,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (destination) {
            AppDestination.MAIN -> {
                CompositionLocalProvider(
                    LocalDetectorAutoExpansionDirective provides DetectorAutoExpansionDirective(
                        titles = expandTitles.toSet(),
                        onConsumed = { title ->
                            expandTitles = expandTitles.filterNot { it == title }
                        },
                    ),
                ) {
                    DashboardScreen(
                        uiState = dashboardState,
                        showTeeDetailsDialog = teeUiState.showDetailsDialog,
                        showTeeCertificatesDialog = teeUiState.showCertificatesDialog,
                        onTeeExpandedChange = teeViewModel::onExpandedChange,
                        onTeeFooterAction = teeViewModel::onFooterAction,
                        onDismissTeeDetails = teeViewModel::dismissDetails,
                        onDismissTeeCertificates = teeViewModel::dismissCertificates,
                    )
                }
            }

            AppDestination.SETTINGS -> {
                SettingsScreen(
                    uiState = settingsState,
                    onCrlNetworkingChange = { enabled ->
                        scope.launch {
                            consentStore.setConsent(enabled)
                            teeViewModel.rescan()
                        }
                    },
                    onCheckForUpdates = updateViewModel::onSettingsUpdateAction,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        FloatingAppTabSwitcher(
            selectedDestination = destination,
            onSelectDestination = selectDest,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp),
        )

        if (
            updateUiState.isDialogVisible &&
            updateUiState.availableUpdate != null
        ) {
            val availableUpdate = requireNotNull(updateUiState.availableUpdate)
            NightlyUpdateDialog(
                currentVersionName = BuildConfig.VERSION_NAME,
                update = availableUpdate,
                downloadEnabled = !resolvingUpdate,
                onDismiss = updateViewModel::dismissUpdate,
                onViewChanges = {
                    if (!openExternalUri(context, availableUpdate.compareUrl)) {
                        Toast.makeText(
                            context,
                            updateError,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                onDownload = {
                    if (!resolvingUpdate) {
                        resolvingUpdate = true
                        scope.launch {
                            try {
                                when (val resolution = updateViewModel.resolveDownload()) {
                                    is UpdateDownloadResolution.Ready -> {
                                        if (openExternalUri(context, resolution.url)) {
                                            updateViewModel.dismissUpdate()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                updateError,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }

                                    UpdateDownloadResolution.Failed -> {
                                        Toast.makeText(
                                            context,
                                            updateError,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }

                                    UpdateDownloadResolution.Current,
                                    UpdateDownloadResolution.Refreshed -> Unit
                                }
                            } finally {
                                resolvingUpdate = false
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun StartupLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "Preparing startup",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Loading scan preferences.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun buildBootloaderContribution(
    bootloaderUiState: BootloaderUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "bootloader",
        title = bootloaderUiState.cardModel.title,
        status = bootloaderUiState.cardModel.status,
        headline = bootloaderUiState.cardModel.verdict,
        summary = bootloaderUiState.cardModel.summary,
        ready = bootloaderUiState.stage != BootloaderUiStage.LOADING,
    )
}

private fun buildCustomRomContribution(
    customRomUiState: CustomRomUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "custom_rom",
        title = customRomUiState.cardModel.title,
        status = customRomUiState.cardModel.status,
        headline = customRomUiState.cardModel.verdict,
        summary = customRomUiState.cardModel.summary,
        ready = customRomUiState.stage != CustomRomUiStage.LOADING,
    )
}

private fun buildSelinuxContribution(
    selinuxUiState: SelinuxUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "selinux",
        title = selinuxUiState.cardModel.title,
        status = selinuxUiState.cardModel.status,
        headline = selinuxUiState.cardModel.verdict,
        summary = selinuxUiState.cardModel.summary,
        ready = selinuxUiState.stage != SelinuxUiStage.LOADING,
    )
}

private fun buildKernelCheckContribution(
    kernelCheckUiState: KernelCheckUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "kernel_check",
        title = kernelCheckUiState.cardModel.title,
        status = kernelCheckUiState.cardModel.status,
        headline = kernelCheckUiState.cardModel.verdict,
        summary = kernelCheckUiState.cardModel.summary,
        ready = kernelCheckUiState.stage != KernelCheckUiStage.LOADING,
    )
}

private fun buildMountContribution(
    mountUiState: MountUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "mount",
        title = mountUiState.cardModel.title,
        status = mountUiState.cardModel.status,
        headline = mountUiState.cardModel.verdict,
        summary = mountUiState.cardModel.summary,
        ready = mountUiState.stage != MountUiStage.LOADING,
    )
}

private fun buildMemoryContribution(
    memoryUiState: MemoryUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "memory",
        title = memoryUiState.cardModel.title,
        status = memoryUiState.cardModel.status,
        headline = memoryUiState.cardModel.verdict,
        summary = memoryUiState.cardModel.summary,
        ready = memoryUiState.stage != MemoryUiStage.LOADING,
    )
}

private fun buildLsposedContribution(
    lsposedUiState: LSPosedUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "lsposed",
        title = lsposedUiState.cardModel.title,
        status = lsposedUiState.cardModel.status,
        headline = lsposedUiState.cardModel.verdict,
        summary = lsposedUiState.cardModel.summary,
        ready = lsposedUiState.stage != LSPosedUiStage.LOADING,
    )
}

private fun buildPlayIntegrityFixContribution(
    playIntegrityFixUiState: PlayIntegrityFixUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "play_integrity_fix",
        title = playIntegrityFixUiState.cardModel.title,
        status = playIntegrityFixUiState.cardModel.status,
        headline = playIntegrityFixUiState.cardModel.verdict,
        summary = playIntegrityFixUiState.cardModel.summary,
        ready = playIntegrityFixUiState.stage != PlayIntegrityFixUiStage.LOADING,
    )
}

private fun buildNativeRootContribution(
    nativeRootUiState: NativeRootUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "native_root",
        title = nativeRootUiState.cardModel.title,
        status = nativeRootUiState.cardModel.status,
        headline = nativeRootUiState.cardModel.verdict,
        summary = nativeRootUiState.cardModel.summary,
        ready = nativeRootUiState.stage != NativeRootUiStage.LOADING,
    )
}

private fun buildDangerousAppsContribution(
    dangerousAppsUiState: DangerousAppsUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "dangerous_apps",
        title = dangerousAppsUiState.cardModel.title,
        status = dangerousAppsUiState.cardModel.status,
        headline = dangerousAppsUiState.cardModel.verdict,
        summary = dangerousAppsUiState.cardModel.summary,
        ready = dangerousAppsUiState.stage != DangerousAppsUiStage.LOADING,
    )
}

private fun buildTeeContribution(
    teeUiState: TeeUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "tee",
        title = teeUiState.cardModel.title,
        status = teeUiState.cardModel.status,
        headline = teeUiState.cardModel.verdict,
        summary = teeUiState.cardModel.summary,
        findingDetail = teeUiState.cardModel.findingDetail,
        ready = teeUiState.stage != TeeUiStage.LOADING,
    )
}

private fun buildSuContribution(
    suUiState: SuUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "su",
        title = suUiState.cardModel.title,
        status = suUiState.cardModel.status,
        headline = suUiState.cardModel.verdict,
        summary = suUiState.cardModel.summary,
        ready = suUiState.stage != SuUiStage.LOADING,
    )
}

private fun buildSystemPropertiesContribution(
    systemPropertiesUiState: SystemPropertiesUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "system_properties",
        title = systemPropertiesUiState.cardModel.title,
        status = systemPropertiesUiState.cardModel.status,
        headline = systemPropertiesUiState.cardModel.verdict,
        summary = systemPropertiesUiState.cardModel.summary,
        ready = systemPropertiesUiState.stage != SystemPropertiesUiStage.LOADING,
    )
}

private fun buildZygiskContribution(
    zygiskUiState: ZygiskUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "zygisk",
        title = zygiskUiState.cardModel.title,
        status = zygiskUiState.cardModel.status,
        headline = zygiskUiState.cardModel.verdict,
        summary = zygiskUiState.cardModel.summary,
        ready = zygiskUiState.stage != ZygiskUiStage.LOADING,
    )
}

private fun buildVirtualizationContribution(
    virtualizationUiState: VirtualizationUiState,
): DashboardDetectorContribution {
    return DashboardDetectorContribution(
        id = "virtualization",
        title = virtualizationUiState.cardModel.title,
        status = virtualizationUiState.cardModel.status,
        headline = virtualizationUiState.cardModel.verdict,
        summary = virtualizationUiState.cardModel.summary,
        ready = virtualizationUiState.stage != VirtualizationUiStage.LOADING,
    )
}
