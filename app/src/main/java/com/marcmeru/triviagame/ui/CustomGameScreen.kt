package com.marcmeru.triviagame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomGameScreen(
    onStartGame: (GameConfig) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚙️ Custom Game",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Categoría
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var expandedCategory by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory }
        ) {
            OutlinedTextField(
                value = getCategoryName(selectedCategory),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Any Category") },
                    onClick = {
                        selectedCategory = null
                        expandedCategory = false
                    }
                )
                categories.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedCategory = id
                            expandedCategory = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dificultad
        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { selectedDifficulty = null },
                    label = {
                        Text(
                            "Any",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedDifficulty == "easy",
                    onClick = { selectedDifficulty = "easy" },
                    label = {
                        Text(
                            "Easy",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDifficulty == "medium",
                    onClick = { selectedDifficulty = "medium" },
                    label = {
                        Text(
                            "Medium",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedDifficulty == "hard",
                    onClick = { selectedDifficulty = "hard" },
                    label = {
                        Text(
                            "Hard",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Botón Start
        Button(
            onClick = {
                onStartGame(
                    GameConfig(
                        category = selectedCategory,
                        difficulty = selectedDifficulty,
                        amount = 10
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Start Custom Game", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

val categories = mapOf(
    9 to "General Knowledge",
    10 to "Books",
    11 to "Film",
    12 to "Music",
    13 to "Musicals & Theatres",
    14 to "Television",
    15 to "Video Games",
    16 to "Board Games",
    17 to "Science & Nature",
    18 to "Computers",
    19 to "Mathematics",
    20 to "Mythology",
    21 to "Sports",
    22 to "Geography",
    23 to "History",
    24 to "Politics",
    25 to "Art",
    26 to "Celebrities",
    27 to "Animals",
    28 to "Vehicles",
    29 to "Comics",
    30 to "Gadgets",
    31 to "Anime & Manga",
    32 to "Cartoon & Animations"
)

fun getCategoryName(id: Int?): String {
    return if (id == null) "Any Category" else categories[id] ?: "Unknown"
}
