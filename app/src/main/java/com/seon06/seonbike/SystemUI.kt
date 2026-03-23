package com.seon06.seonbike

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

@Composable
fun FloatingBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onSearch: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf("") }
    
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeBottomPadding = imeInsets.asPaddingValues().calculateBottomPadding()
    
    val animatedOffset by animateDpAsState(
        targetValue = if (imeBottomPadding > 0.dp) -imeBottomPadding + 8.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "FloatingBarOffset"
    )

    Surface(
        modifier = modifier
            .offset(y = animatedOffset)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape) // Haze 적용 전 클립
            .then(
                if (hazeState != null) {
                    Modifier.hazeChild(
                        state = hazeState,
                        style = HazeDefaults.style(
                            backgroundColor = Color.Transparent,
                            tint = HazeTint(Color.White.copy(alpha = 0.15f)), // 투명도를 살짝 낮춤 (0.02 -> 0.15)
                            blurRadius = 8.dp,
                            noiseFactor = 0f
                        )
                    )
                } else Modifier
            ),
        shape = CircleShape,
        color = Color.Transparent,
        shadowElevation = 0.dp, // 3D 효과(그림자/그라데이션) 제거
        tonalElevation = 0.dp   // 3D 효과 제거
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "어디로 갈까요?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                BasicTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                        onSearch(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true
                )
            }
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
