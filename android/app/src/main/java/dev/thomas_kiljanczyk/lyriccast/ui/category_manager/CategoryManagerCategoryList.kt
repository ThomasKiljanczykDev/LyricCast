/*
 * Created by Tomasz Kiljanczyk on 6/8/25, 10:15 PM
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 6/8/25, 10:15 PM
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import dev.thomas_kiljanczyk.lyriccast.ui.shared.theme.LyricCastTheme


@Composable
fun CategoryListItem(
    item: CategoryItem,
    onCategorySelected: (CategoryItem, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    canClick: Boolean = false
) {
    val categoryColor = item.category.color
    val color = if (categoryColor != null) Color(categoryColor) else null
    val animatedBorderColor by animateColorAsState(
        targetValue = if (!item.isSelected) Color.Transparent else color
            ?: MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .combinedClickable(
                onClick = {
                    if (canClick) {
                        onCategorySelected(item, !item.isSelected)
                    }
                },
                // TODO: verify if the device vibrates on long click
                onLongClick = {
                    onCategorySelected(item, !item.isSelected)
                },
                indication = ripple(true),
                interactionSource = remember { MutableInteractionSource() })
            .border(
                width = 2.dp,
                color = animatedBorderColor,
                shape = CardDefaults.shape
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .height(48.dp)
        ) {
            Text(
                text = item.category.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            )

            if (color != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = color
                    ), modifier = Modifier
                        .width(60.dp)
                        .height(30.dp)
                        .padding(end = 8.dp)

                ) {}
            }
        }
    }
}

@PreviewLightDark
@Composable
fun CategoryListItemCheckboxSelectedPreview() {
    LyricCastTheme {
        Surface {
            CategoryListItem(
                item = CategoryItem(
                    category = Category(
                        id = "1", name = "Sample Category", color = R.color.red
                    ), isSelected = true
                ), onCategorySelected = { _, _ -> })
        }
    }
}

@PreviewLightDark
@Composable
fun CategoryListItemCheckboxNotSelectedPreview() {
    LyricCastTheme {
        Surface {
            CategoryListItem(
                item = CategoryItem(
                    category = Category(
                        id = "1", name = "Sample Category", color = R.color.red
                    ), isSelected = false
                ), onCategorySelected = { _, _ -> })
        }
    }
}

@PreviewLightDark
@Composable
fun CategoryListItemNoCheckboxPreview() {
    LyricCastTheme {
        Surface {
            CategoryListItem(
                item = CategoryItem(
                    category = Category(
                        id = "1", name = "Sample Category", color = R.color.red
                    ), isSelected = false
                ), onCategorySelected = { _, _ -> })
        }
    }
}

@Composable
fun CategoryList(
    categories: List<CategoryItem>,
    onCategorySelected: (CategoryItem, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val anyCategorySelected = categories.any { it.isSelected }
    LazyColumn(
        modifier = modifier
    ) {
        items(categories, key = { it.category.id }) { category ->
            CategoryListItem(
                item = category,
                onCategorySelected = onCategorySelected,
                canClick = anyCategorySelected,
                modifier = Modifier.animateItem()
            )
        }
    }
}


@PreviewLightDark
@Composable
fun CategoryListPreview() {
    LyricCastTheme {
        Surface {
            CategoryList(categories = List(3) { index ->
                CategoryItem(
                    category = Category(
                        id = index.toString(), name = "Category $index", color = R.color.red
                    )
                )
            }, onCategorySelected = { _, _ -> })
        }
    }
}
