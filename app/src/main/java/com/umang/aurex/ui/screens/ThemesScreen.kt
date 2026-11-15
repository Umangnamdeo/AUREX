package com.umang.aurex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.umang.aurex.data.AurexSettings
import com.umang.aurex.data.ThemeSpec
import com.umang.aurex.ui.AurexViewModel
import com.umang.aurex.ui.components.IslandPill
import com.umang.aurex.ui.theme.resolveTheme

private fun ThemeSpec.asSettings(base: AurexSettings) =
    base.copy(style = style, bg = bg, fill = fill, icon = icon, accent = accent, alpha = alpha,
        widthDp = 26f, heightDp = 84f, cornerDp = 13f)

@Composable
fun ThemesScreen(vm: AurexViewModel) {
    val ctx = LocalContext.current
    val s by vm.settings.collectAsState()
    val custom by vm.customThemes.collectAsState()
    val clipboard = LocalClipboardManager.current
    val all = (ThemeSpec.builtIns + custom).map { resolveTheme(ctx, it) }
    var selectedId by remember { mutableStateOf(s.themeId) }
    val selected = all.firstOrNull { it.id == selectedId } ?: all.first()
    var previewLevel by remember { mutableFloatStateOf(0.65f) }
    var showCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(Modifier.padding(top = 12.dp)) {
                Text("Themes", style = MaterialTheme.typography.headlineMedium)
                Text("Choose a theme, or make your own", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(190.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF0A0C11), selected.accent.let { Color(it).copy(alpha = 0.25f) })), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(selected.name, style = MaterialTheme.typography.titleLarge)
                        Text(selected.tagline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text("Drag the pill to preview", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IslandPill(
                        s.copy(style = selected.style, bg = selected.bg, fill = selected.fill, icon = selected.icon, alpha = selected.alpha, widthDp = 46f, heightDp = 150f, cornerDp = 23f, reducedMotion = s.reducedMotion),
                        previewLevel, previewLevel <= 0.001f, onLevel = { previewLevel = it },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.applyTheme(selected) }, Modifier.weight(1f), enabled = s.themeId != selected.id) {
                        Text(if (s.themeId == selected.id) "Applied" else "Apply theme")
                    }
                    OutlinedButton(onClick = { showCreate = true }, Modifier.weight(1f)) { Text("Save current look") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val text = clipboard.getText()?.text.orEmpty()
                        val ok = vm.importTheme(text)
                        Toast.makeText(ctx, if (ok) "Theme imported" else "Clipboard does not contain an AUREX theme", Toast.LENGTH_SHORT).show()
                    }, Modifier.weight(1f)) { Text("Import") }
                    OutlinedButton(onClick = {
                        clipboard.setText(AnnotatedString(vm.exportTheme(selected)))
                        Toast.makeText(ctx, "Theme copied to clipboard", Toast.LENGTH_SHORT).show()
                    }, Modifier.weight(1f)) { Text("Export") }
                }
                if (!selected.builtIn) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.updateCustomTheme(selected); Toast.makeText(ctx, "Theme updated from current look", Toast.LENGTH_SHORT).show() }, Modifier.weight(1f)) { Text("Update from current") }
                        OutlinedButton(onClick = { vm.deleteTheme(selected.id); selectedId = s.themeId }, Modifier.weight(1f)) { Text("Delete") }
                    }
                }
            }
        }
        items(all, key = { it.id }) { t ->
            val isSel = t.id == selected.id
            Column(
                Modifier
                    .background(Brush.verticalGradient(listOf(Color(t.accent).copy(alpha = 0.22f), Color(0xFF0A0C11))), RoundedCornerShape(20.dp))
                    .border(if (isSel) 2.dp else 1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                    .clickable { selectedId = t.id }
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                    IslandPill(t.asSettings(s).copy(reducedMotion = true), 0.7f, false)
                }
                Text(t.name, style = MaterialTheme.typography.titleSmall)
                Text(if (s.themeId == t.id) "Active" else t.tagline, style = MaterialTheme.typography.bodySmall, color = if (s.themeId == t.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Save current look") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saves the island's current style and colors as a custom theme.")
                    OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("Theme name") })
                }
            },
            confirmButton = { TextButton(onClick = { vm.createCustomTheme(name); name = ""; showCreate = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } },
        )
    }
}
