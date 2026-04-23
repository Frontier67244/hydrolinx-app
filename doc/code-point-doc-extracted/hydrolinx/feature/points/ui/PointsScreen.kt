package com.mahendra.android.hydrolinx.feature.points.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraPreviewUseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxTheme

// ---------------------------------------------------------------------------
// Improved Notification System
// ---------------------------------------------------------------------------
private data class NotificationState(
    val isVisible: Boolean = false,
    val message: String = "",
    val subMessage: String = ""
)

@Composable
private fun CustomNotification(
    state: NotificationState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = HydrolinxCyan,
            tonalElevation = 8.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (state.subMessage.isNotEmpty()) {
                        Text(
                            text = state.subMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Temporary X icon replacement if not found
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HydrolinX brand logo — two cyan rings + sync arc (Canvas)
// ---------------------------------------------------------------------------
@Composable
private fun HydrolinxLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f
        
        // 1. Outer Cyan Ring
        val outerCyanStroke = radius * 0.12f
        drawCircle(
            color = HydrolinxCyan,
            radius = radius - outerCyanStroke / 2f,
            center = Offset(cx, cy),
            style = Stroke(width = outerCyanStroke)
        )
        
        // 2. White Ring Separator
        val whiteStroke = radius * 0.14f
        val whiteRadius = radius - outerCyanStroke - whiteStroke / 2f
        drawCircle(
            color = Color.White,
            radius = whiteRadius,
            center = Offset(cx, cy),
            style = Stroke(width = whiteStroke)
        )
        
        // 3. Inner Solid Cyan Disc
        val discRadius = radius - outerCyanStroke - whiteStroke
        drawCircle(
            color = HydrolinxCyan,
            radius = discRadius,
            center = Offset(cx, cy)
        )
        
        // 4. Two White Sync Arcs
        val arcRadius = discRadius * 0.55f
        val arcStroke = discRadius * 0.35f
        val arcSize = Size(arcRadius * 2, arcRadius * 2)
        val arcTopLeft = Offset(cx - arcRadius, cy - arcRadius)
        
        // Top-right arc
        drawArc(
            color = Color.White,
            startAngle = -60f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = arcStroke, cap = StrokeCap.Round)
        )
        
        // Bottom-left arc
        drawArc(
            color = Color.White,
            startAngle = 120f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = arcStroke, cap = StrokeCap.Round)
        )
    }
}

// ---------------------------------------------------------------------------
// Static preview data – will be replaced by real state from ViewModel later
// ---------------------------------------------------------------------------
private data class RedeemDisplayState(
    val userName: String = "Healthy",
    val totalRedeem: Int = 10,
    val activePoints: Int = 80,
    val activePointsCap: Int = 100,
    val tokenCount: Int = 1,
    val tokenCap: Int = 1,
    val bankPoints: Int = 80,
    val bankPointsCap: Int = 300,
)

// ---------------------------------------------------------------------------
// Main screen
// ---------------------------------------------------------------------------
@Composable
fun PointsScreen() {
    var isScanOpen     by remember { mutableStateOf(false) }
    var totalRedeem    by remember { mutableIntStateOf(0) }
    var activePoints   by remember { mutableIntStateOf(100) }
    var tokenCount     by remember { mutableIntStateOf(0) }
    var bankPoints     by remember { mutableIntStateOf(0) }
    var lastRedeemDateIso by remember { mutableStateOf("") }
    
    // Developer state
    var isDevMode by remember { mutableStateOf(false) }
    
    // REDEEM: 100 poin -> 1 token. Maksimal bank poin 300.
    // Jika token tidak dipakai dlm 3 hari, balik ke bank.
    
    // Simulasi expiry: Jika ada token dan sudah lewat 3 hari
    LaunchedEffect(lastRedeemDateIso, tokenCount) {
        if (tokenCount > 0 && lastRedeemDateIso.isNotEmpty()) {
            val redeemDate = java.time.LocalDate.parse(lastRedeemDateIso)
            val today = java.time.LocalDate.now()
            if (today.isAfter(redeemDate.plusDays(3))) {
                // Balik ke bank, max 300
                bankPoints = minOf(300, bankPoints + 100)
                tokenCount = 0
            }
        }
    }
    
    var notification by remember { mutableStateOf(NotificationState()) }
    var notificationHistory by remember { mutableStateOf(listOf<NotifyItem>()) }
    var isNotifyOpen by remember { mutableStateOf(false) }

    // Auto-dismiss notification after 3 seconds
    LaunchedEffect(notification.isVisible) {
        if (notification.isVisible) {
            delay(3000)
            notification = notification.copy(isVisible = false)
        }
    }

    // Tanggal hari ini (LocalDate tersedia sejak API 26)
    val todayIso = remember { java.time.LocalDate.now().toString() }
    val currentTime = remember { 
        val now = java.time.LocalTime.now()
        String.format(Locale.getDefault(), "%02d.%02d", now.hour, now.minute)
    }

    // REDEEM: 100 active poin → 1 token, max 1x per hari
    val canRedeem = activePoints >= 100 && lastRedeemDateIso != todayIso
    val onRedeem: () -> Unit = {
        if (canRedeem) {
            activePoints -= 100
            tokenCount   += 1
            totalRedeem  += 1
            lastRedeemDateIso = todayIso   // kunci redeem hari ini

            // Pengisian otomatis dari bank (opsional sesuai spek awal)
            val transfer = minOf(bankPoints, 100 - activePoints)
            if (transfer > 0) {
                activePoints += transfer
                bankPoints   -= transfer
            }
            
            val msg = "Redeem Berhasil!"
            val subMsg = "1 Token ditambahkan ke akunmu."
            notification = NotificationState(isVisible = true, message = msg, subMessage = subMsg)
            notificationHistory = listOf(NotifyItem(msg, subMsg, currentTime)) + notificationHistory
        }
    }

    // Token x/1: berapa kali sudah redeem hari ini (0 atau 1)
    val redeemCountToday = if (lastRedeemDateIso == todayIso) 1 else 0
    val canScan = tokenCount > 0

    AnimatedContent(
        targetState = isScanOpen,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally(tween(300)) { it } togetherWith
                    slideOutHorizontally(tween(300)) { -it }
            } else {
                slideInHorizontally(tween(300)) { -it } togetherWith
                    slideOutHorizontally(tween(300)) { it }
            }
        },
        label = "scan_transition",
    ) { scanOpen ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (scanOpen) {
                ScanQrScreen(
                    onBack = { isScanOpen = false },
                    onConfirmScan = { volume ->
                        if (tokenCount > 0) {
                            tokenCount -= 1
                            isScanOpen = false
                            val msg = "Scan Berhasil!"
                            val subMsg = "$volume Ml air siap diambil."
                            notification = NotificationState(isVisible = true, message = msg, subMessage = subMsg)
                            notificationHistory = listOf(NotifyItem(msg, subMsg, currentTime)) + notificationHistory
                        }
                    }
                )
            } else {
                Column {
                    PointsScreenContent(
                        state = RedeemDisplayState(
                            totalRedeem      = totalRedeem,
                            activePoints     = activePoints,
                            activePointsCap  = 100,
                            tokenCount       = redeemCountToday,  // 0/1 atau 1/1 — limit harian
                            tokenCap         = 1,
                            bankPoints       = bankPoints,
                            bankPointsCap    = 300,
                        ),
                        canRedeem    = canRedeem,
                        canScan      = canScan,
                        onRedeem     = onRedeem,
                        onOpenScan   = { isScanOpen = true },
                        isNotifyOpen = isNotifyOpen,
                        notificationHistory = notificationHistory,
                        onToggleNotify = { isNotifyOpen = !isNotifyOpen },
                        onToggleDevMode = { isDevMode = !isDevMode }
                    )
                    
                    // Developer Controls
                    AnimatedVisibility(visible = isDevMode) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black.copy(alpha = 0.05f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { lastRedeemDateIso = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Speed, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Skip Day", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { activePoints = minOf(100, activePoints + 100) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.BugReport, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("+100 Pts", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            // Notification Overlay
            if (notification.isVisible) {
                CustomNotification(
                    state = notification,
                    onDismiss = { notification = notification.copy(isVisible = false) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 60.dp)
                )
            }
        }
    }
}

@Composable
private fun PointsScreenContent(
    state: RedeemDisplayState,
    canRedeem: Boolean = false,
    canScan: Boolean = false,
    onRedeem: () -> Unit = {},
    onOpenScan: () -> Unit = {},
    isNotifyOpen: Boolean = false,
    notificationHistory: List<NotifyItem> = emptyList(),
    onToggleNotify: () -> Unit = {},
    onToggleDevMode: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Header ─────────────────────────────────────────────────────
        RedeemTopBar(
            isNotifyOpen = isNotifyOpen,
            onLogoClick = onToggleDevMode
        )

        // ── Greeting card (hidden when notify panel is open) ────────────────
        AnimatedVisibility(
            visible = !isNotifyOpen,
            enter = fadeIn(tween(220)) + expandVertically(tween(220)),
            exit  = fadeOut(tween(180)) + shrinkVertically(tween(180)),
        ) {
            Column {
                Spacer(Modifier.height(6.dp))
                // Caret segitiga atas — speech bubble dari logo ke greeting card
                Box(
                    modifier = Modifier
                        .padding(start = 30.dp)  // center di atas logo (16dp margin + ~14dp offset)
                        .size(width = 14.dp, height = 8.dp)
                        .background(
                            color = HydrolinxCyan,
                            shape = GenericShape { size, _ ->
                                moveTo(size.width / 2f, 0f)   // puncak atas
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            },
                        )
                )
                GreetingCard(userName = state.userName)
            }
        }

        // ── Wallet card ────────────────────────────────────────────────────
        Spacer(Modifier.height(40.dp))
        WalletCard(
            totalRedeem = state.totalRedeem,
            activePoints = state.activePoints,
            activePointsCap = state.activePointsCap,
            tokenCount = state.tokenCount,
            tokenCap = state.tokenCap,
            bankPoints = state.bankPoints,
            bankPointsCap = state.bankPointsCap,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.weight(1f))

        // ── Notify panel (slides in above action row) ───────────────────────
        AnimatedVisibility(
            visible = isNotifyOpen,
            enter = fadeIn(tween(250)) + expandVertically(
                animationSpec = tween(250),
                expandFrom = Alignment.Bottom,
            ),
            exit  = fadeOut(tween(200)) + shrinkVertically(
                animationSpec = tween(200),
                shrinkTowards = Alignment.Bottom,
            ),
        ) {
            NotifyPanel(
                history = notificationHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // ── Action row ─────────────────────────────────────────────────────
        ActionRow(
            isNotifyActive  = isNotifyOpen,
            onToggleNotify  = onToggleNotify,
            canRedeem       = canRedeem,
            canScan         = canScan,
            onRedeem        = onRedeem,
            onOpenScan      = onOpenScan,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Top bar  –  logo circle + breadcrumb pill
// ---------------------------------------------------------------------------
@Composable
private fun RedeemTopBar(
    isNotifyOpen: Boolean = false,
    scanLabel: String? = null,
    onLogoClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Logo - Clickable for Dev Mode
        HydrolinxLogo(
            modifier = Modifier
                .size(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLogoClick
                )
        )

        Spacer(Modifier.width(12.dp))

        // Breadcrumb pill
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "POIN",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                    ),
                    color = Color.Black,
                )
                Spacer(Modifier.width(24.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
                Spacer(Modifier.width(24.dp))
                Text(
                    text = scanLabel ?: if (isNotifyOpen) "NOTIFY" else "REDEEM",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                    ),
                    color = Color.Black,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Greeting card
// ---------------------------------------------------------------------------
@Composable
private fun GreetingCard(userName: String) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, HydrolinxCyan),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // "Halo, Healthy"
                Text(
                    text = buildAnnotatedString {
                        append("Halo, ")
                        withStyle(SpanStyle(color = HydrolinxCyan, fontWeight = FontWeight.SemiBold)) {
                            append(userName)
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Poinmu sudah cukup?\nRedeem untuk token akses air sekarang.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = "Hint",
                tint = HydrolinxCyan,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Wallet card
// ---------------------------------------------------------------------------
@Composable
private fun WalletCard(
    totalRedeem: Int,
    activePoints: Int,
    activePointsCap: Int,
    tokenCount: Int,
    tokenCap: Int,
    bankPoints: Int,
    bankPointsCap: Int,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, HydrolinxCyan),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 96.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Sesuai gambar: angka total, bold
            Text(
                text = "$totalRedeem",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Redeem",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            
            Spacer(Modifier.height(120.dp))

            // ─ Stats row (Row bawah) ─
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatColumn(
                    value = "$activePoints/$activePointsCap",
                    label = "Total Poin",
                    modifier = Modifier.weight(1f),
                )
                StatColumn(
                    value = "$tokenCount/$tokenCap",
                    label = "Token",
                    modifier = Modifier.weight(1f),
                )
                StatColumn(
                    value = "$bankPoints/$bankPointsCap",
                    label = "Bank Poin",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// Action row  –  Bell | REDEEM button | QR
// ---------------------------------------------------------------------------
@Composable
private fun ActionRow(
    isNotifyActive: Boolean = false,
    onToggleNotify: () -> Unit = {},
    canRedeem: Boolean = false,
    canScan: Boolean = false,
    onRedeem: () -> Unit = {},
    onOpenScan: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Bell icon button — cyan border when active
        val bellBorderColor = if (isNotifyActive) HydrolinxCyan else Color(0xFFE0E0E0)
        val bellIconTint    = if (isNotifyActive) HydrolinxCyan else MaterialTheme.colorScheme.onSurface
        IconButton(
            onClick = onToggleNotify,
            modifier = Modifier
                .size(48.dp)
                .border(1.5.dp, bellBorderColor, RoundedCornerShape(12.dp)),
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White),
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifikasi",
                tint = bellIconTint,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        // REDEEM filled button
        Button(
            onClick = onRedeem,
            enabled = canRedeem,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HydrolinxCyan,
                contentColor = Color.White,
                disabledContainerColor = HydrolinxCyan.copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.6f),
            ),
        ) {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "REDEEM",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            )
        }

        Spacer(Modifier.width(12.dp))

        // QR scan icon button (outlined square)
        IconButton(
            onClick = onOpenScan,
            enabled = canScan,
            modifier = Modifier
                .size(48.dp)
                .border(
                    width = 1.5.dp,
                    color = if (canScan) Color(0xFFE0E0E0) else Color(0xFFE0E0E0).copy(alpha = 0.38f),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.38f)
            ),
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR",
                tint = if (canScan) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Notify panel  –  notification list + caret pointing to bell
// ---------------------------------------------------------------------------
private data class NotifyItem(val title: String, val body: String, val time: String)

@Composable
private fun NotifyPanel(
    history: List<NotifyItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedCard(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, HydrolinxCyan),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (history.isEmpty()) {
                    Text(
                        text = "Belum ada notifikasi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    history.forEachIndexed { index, item ->
                        if (index > 0) HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotifyRow(item)
                    }
                }
            }
        }

        // Caret mengarah ke bell button (center bell = 16dp margin + 24dp half = 40dp from screen)
        // Caret width=14dp, so start = 40 - 16 (panel margin) - 7 (half caret) = 17dp
        Box(
            modifier = Modifier
                .padding(start = 17.dp)
                .size(width = 14.dp, height = 8.dp)
                .background(
                    color = HydrolinxCyan,
                    shape = GenericShape { size, _ ->
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    },
                )
        )
    }
}

@Composable
private fun NotifyRow(item: NotifyItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = item.time,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Camera Preview (CameraX)
// ---------------------------------------------------------------------------
@Composable
private fun CameraPreview(modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = CameraPreviewUseCase.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
    )
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PointsScreenPreview() {
    HydrolinxTheme {
        PointsScreen()
    }
}

// ---------------------------------------------------------------------------
// Scan QR Screen
// ---------------------------------------------------------------------------
private val SCAN_VOLUMES = listOf(250, 500, 1000)

@Composable
fun ScanQrScreen(
    onBack: () -> Unit = {},
    onConfirmScan: (Int) -> Unit = {}
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    var selectedVolume by remember { mutableIntStateOf(500) }
    
    // ... camera permission logic remains same ...
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar with "SCAN" breadcrumb ─────────────────────────────
        RedeemTopBar(scanLabel = "SCAN")

        // ... camera preview Box remains same ...
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .border(1.5.dp, HydrolinxCyan, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Izinkan akses kamera",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ── Bottom panel ───────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                // Volume chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SCAN_VOLUMES.forEach { ml ->
                        FilterChip(
                            selected = selectedVolume == ml,
                            onClick = { selectedVolume = ml },
                            modifier = Modifier.weight(1f),
                            label = {
                                Text(
                                    text = "+$ml Ml",
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HydrolinxCyan.copy(alpha = 0.12f),
                                selectedLabelColor = HydrolinxCyan,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedVolume == ml,
                                selectedBorderColor = HydrolinxCyan,
                                borderColor = Color(0xFFDDDDDD),
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Back + SCAN button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Button(
                        onClick = { onConfirmScan(selectedVolume) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFE0E0E0)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "SCAN",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            )
                        )
                    }
                }
            }
        }
    }
}
