package com.tisosoft.minimalpersonalos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val showScrollbar = scrollState.maxValue > 0
    if (!showScrollbar) return

    val alpha by animateFloatAsState(
        targetValue = if (scrollState.isScrollInProgress) 1f else 0.4f,
        animationSpec = tween(durationMillis = 500),
        label = "scrollbarAlpha"
    )

    Column(
        modifier = modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .padding(end = 2.dp, top = 4.dp, bottom = 4.dp)
            .width(4.dp)
            .alpha(alpha)
            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
    ) {
        val viewPortHeight = scrollState.viewportSize.toFloat()
        val totalHeight = (scrollState.maxValue + viewPortHeight)
        
        if (totalHeight > 0) {
            val thumbHeightRatio = (viewPortHeight / totalHeight).coerceIn(0.1f, 1f)
            val startWeight = scrollState.value.toFloat() / totalHeight
            val endWeight = 1f - startWeight - thumbHeightRatio

            if (startWeight > 0) {
                Spacer(modifier = Modifier.weight(startWeight.coerceAtLeast(0.0001f)))
            }
            Box(
                modifier = Modifier
                    .weight(thumbHeightRatio)
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
            )
            if (endWeight > 0) {
                Spacer(modifier = Modifier.weight(endWeight.coerceAtLeast(0.0001f)))
            }
        }
    }
}

@Composable
fun BoxScope.LazyVerticalScrollbar(
    listState: LazyListState,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    if (totalItems == 0) return

    val alpha by animateFloatAsState(
        targetValue = if (listState.isScrollInProgress) 1f else 0.4f,
        animationSpec = tween(durationMillis = 500),
        label = "scrollbarAlpha"
    )

    Column(
        modifier = modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .padding(end = 2.dp, top = 4.dp, bottom = 4.dp)
            .width(4.dp)
            .alpha(alpha)
            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
    ) {
        val firstVisibleIndex = listState.firstVisibleItemIndex
        val visibleItemsCount = listState.layoutInfo.visibleItemsInfo.size
        
        if (visibleItemsCount < totalItems) {
            val thumbHeightRatio = (visibleItemsCount.toFloat() / totalItems).coerceIn(0.1f, 1f)
            val startWeight = firstVisibleIndex.toFloat() / totalItems
            val endWeight = 1f - startWeight - thumbHeightRatio

            if (startWeight > 0) {
                Spacer(modifier = Modifier.weight(startWeight.coerceAtLeast(0.0001f)))
            }
            Box(
                modifier = Modifier
                    .weight(thumbHeightRatio)
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
            )
            if (endWeight > 0) {
                Spacer(modifier = Modifier.weight(endWeight.coerceAtLeast(0.0001f)))
            }
        }
    }
}
