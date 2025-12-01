package com.example.akioratinder.ui.adapters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ServerFilterAdapter(
    servers: List<String>,
    selectedServer: String? = null,
    modifier: Modifier = Modifier,
    onServerSelected: (String?) -> Unit = {}
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Опция "Все"
        item {
            FilterChip(
                selected = selectedServer == null,
                onClick = { onServerSelected(null) },
                label = { Text("All") },
                modifier = Modifier.height(36.dp)
            )
        }
        
        items(servers, key = { it }) { server ->
            FilterChip(
                selected = selectedServer == server,
                onClick = { onServerSelected(server) },
                label = { Text(server) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            label()
        }
    }
}

