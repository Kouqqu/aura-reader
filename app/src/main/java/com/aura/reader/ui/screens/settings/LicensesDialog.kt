package com.aura.reader.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.reader.ui.theme.LocalAppStrings

private data class OpenSourceLicense(
    val title: String,
    val author: String,
    val licenseType: String,
    val description: String,
    val details: String
)

private val licensesList = listOf(
    OpenSourceLicense(
        title = "Aura Reader",
        author = "Koukku (https://github.com/Kouqqu/aura-reader)",
        licenseType = "GNU GPL v3.0",
        description = "Современная быстрая читалка книг для Android.",
        details = "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version."
    ),
    OpenSourceLicense(
        title = "Шрифты (Google Sans, Inter, Literata, Lora, Marcellus, PT Serif, JetBrains Mono)",
        author = "The Google Sans Project, Rasmus Andersson, Google Inc, Cyreal, Astigmatic, ParaType, JetBrains",
        licenseType = "SIL Open Font License 1.1",
        description = "Качественные встроенные гарнитуры для комфортного чтения и интерфейса.",
        details = "Licensed under the SIL Open Font License, Version 1.1 (https://openfontlicense.org).\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of the Font Software, to use, study, copy, merge, embed, modify, redistribute, and sell modified and unmodified copies of the Font Software."
    ),
    OpenSourceLicense(
        title = "Шрифт Zakon",
        author = "Олексій Чекаль, Геннадій Заречнюк, Дмитро Растворцев, Андрій Шевченко",
        licenseType = "Официальный шрифт (Минкультуры Украины)",
        description = "Специальный историко-правовой шрифт для украинского и английского интерфейса.",
        details = "Правообладатель: Национальная библиотека Украины имени Ярослава Мудрого (www.mincult.gov.ua). Шрифт включен как опциональная интерфейсная гарнитура."
    ),
    OpenSourceLicense(
        title = "Liquid Glass Shader & Dock Refraction",
        author = "Ahmed Gamil (https://github.com/ahmedgamil/liquid-glass)",
        licenseType = "MIT License",
        description = "Физически корректная рефракция и размытие для плавающего острова Liquid Glass.",
        details = "Copyright (c) 2025 Ahmed Gamil\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files, to deal in the Software without restriction."
    ),
    OpenSourceLicense(
        title = "Библиотека Haze",
        author = "Chris Banes (https://github.com/chrisbanes/haze)",
        licenseType = "Apache License 2.0",
        description = "Эффект матового стекла и глубинного размытия для Jetpack Compose.",
        details = "Copyright (C) 2023 Chris Banes\n\nLicensed under the Apache License, Version 2.0. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"
    ),
    OpenSourceLicense(
        title = "jsoup: Java HTML Parser",
        author = "Jonathan Hedley (https://jsoup.org)",
        licenseType = "MIT License",
        description = "Быстрый и надежный парсинг и форматирование глав книг EPUB и FB2.",
        details = "Copyright (c) 2009-2024 Jonathan Hedley (jonathan@hedley.net)\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files."
    ),
    OpenSourceLicense(
        title = "Звуковые эффекты («Spooky Ghosts»)",
        author = "Gfx Sounds (Uppbeat)",
        licenseType = "Uppbeat Royalty-Free License",
        description = "Аудиоэффекты праздничной атмосферы.",
        details = "Track: 'Spooky Ghosts' by Gfx Sounds, via Uppbeat (https://uppbeat.io). Licensed for use in personal and commercial mobile applications."
    ),
    OpenSourceLicense(
        title = "AndroidX & Jetpack Compose",
        author = "The Android Open Source Project (Google LLC)",
        licenseType = "Apache License 2.0",
        description = "Официальные библиотеки платформы Android, Material 3 и Jetpack.",
        details = "Copyright (C) The Android Open Source Project\n\nLicensed under the Apache License, Version 2.0."
    )
)

@Composable
internal fun LicensesDialog(
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Policy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.licensesTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.licensesSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.close)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(licensesList) { item ->
                    LicenseItemCard(item)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.done)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun LicenseItemCard(license: OpenSourceLicense) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "licenseChevron"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = license.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = license.licenseType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer { rotationZ = chevronRotation }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = license.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Автор: ${license.author}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = license.details,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
