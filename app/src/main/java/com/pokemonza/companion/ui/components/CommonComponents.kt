package com.pokemonza.companion.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.pokemonza.companion.R
import com.pokemonza.companion.navigation.CompanionTab
import com.pokemonza.companion.ui.theme.ZAAccent
import com.pokemonza.companion.ui.theme.ZATypeColors

/** Glass UI — background art stays visible through all panels. */
val GlassPanel = Color(0xFF1A1A2E).copy(alpha = 0.38f)
val GlassPanelLight = Color(0xFF1A1A2E).copy(alpha = 0.28f)
/** Semi-transparent glass for bottom menu — content shows through. */
val GlassNavBar = Color(0xFF1A1A2E).copy(alpha = 0.38f)
val GlassNavBarHeight = 72.dp
val GlassPopup = Color(0xFF1A1A2E).copy(alpha = 0.72f)
val GlassField = Color(0xFF1A1A2E).copy(alpha = 0.35f)
val GlassChipSelected = Color(0xFFE94560).copy(alpha = 0.45f)
val GlassChip = Color(0xFFFFFFFF).copy(alpha = 0.12f)

@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.companion_background),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    // Light vignette only — keeps artwork visible
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.12f),
                        Color.Black.copy(alpha = 0.22f),
                        Color.Black.copy(alpha = 0.32f)
                    )
                )
            )
    )
}

@Composable
fun BackgroundScaffold(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        AppBackground()
        content()
    }
}

/** Custom bottom nav — true glass overlay (Material NavigationBar stays opaque). */
@Composable
fun GlassBottomNavigation(
    tabs: List<CompanionTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(GlassNavBarHeight)
            .background(GlassNavBar)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = selectedIndex == index
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) ZAAccent.copy(0.22f) else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = if (selected) ZAAccent else Color.White.copy(0.65f),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tab.title,
                    color = if (selected) ZAAccent else Color.White.copy(0.65f),
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassPanel)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        content = { content() }
    )
}

@Composable
fun GlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(shape)
            .background(GlassField)
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        cursorBrush = SolidColor(Color.White),
        singleLine = true,
        decorationBox = { inner ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.invoke()
                Box(Modifier.weight(1f).padding(start = if (leadingIcon != null) 8.dp else 0.dp)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color.White.copy(0.45f), fontSize = 14.sp)
                    }
                    inner()
                }
            }
        }
    )
}

@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GlassChipSelected,
            containerColor = GlassChip,
            labelColor = Color.White,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.White.copy(alpha = 0.2f),
            selectedBorderColor = Color.White.copy(alpha = 0.35f)
        )
    )
}

@Composable
fun glassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White.copy(0.4f),
    unfocusedBorderColor = Color.White.copy(0.2f),
    focusedContainerColor = GlassField,
    unfocusedContainerColor = GlassField,
    cursorColor = Color.White
)

@Composable
fun TypeBadge(type: String) {
    val color = ZATypeColors[type] ?: Color.Gray
    Text(
        text = type,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color.copy(alpha = 0.75f), MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun LastUpdatedText(timestamp: Long?) {
    if (timestamp == null) return
    val formatted = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
    Text(
        text = "Live data · $formatted",
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 10.sp,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
fun DetailPopup(
    title: String,
    onDismiss: () -> Unit,
    imageUrl: String? = null,
    @DrawableRes imageRes: Int? = null,
    imageMaxHeight: Dp = 96.dp,
    body: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 520.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassPopup)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { }
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "✕",
                        color = Color.White.copy(0.85f),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable(onClick = onDismiss),
                        fontSize = 18.sp
                    )
                }
                when {
                    imageUrl != null -> AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageMaxHeight)
                            .padding(vertical = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Fit
                    )
                    imageRes != null -> Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageMaxHeight)
                            .padding(vertical = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Fit
                    )
                }
                body()
            }
        }
    }
}
