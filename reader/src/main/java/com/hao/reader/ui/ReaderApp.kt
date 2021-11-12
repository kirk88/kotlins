package com.hao.reader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sample.R
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.hao.reader.components.OverflowMenu
import com.hao.reader.components.OverflowMenuItem
import com.hao.reader.ui.common.Background
import com.hao.reader.ui.theme.ReaderThemeWithInsets
import kotlinx.coroutines.launch


@Composable
fun ReaderApp() {
    ReaderThemeWithInsets {
        Background {
            Column {
                MainAppBar()
                MainContent()
            }
        }
    }
}

@Composable
private fun MainAppBar() {
    var menuExpanded by remember { mutableStateOf(false) }
    SmallTopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        actions = {
            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.action_more)
                )
            }
            OverflowMenu(menuExpanded, onDismissRequest = { menuExpanded = false }) {
                OverflowMenuItem(
                    icon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "search") },
                    text = { Text(text = "Search", style = MaterialTheme.typography.bodyLarge) },
                    onClick = { menuExpanded = false }
                )
                OverflowMenuItem(
                    icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = "settings") },
                    text = { Text(text = "Settings", style = MaterialTheme.typography.bodyLarge) },
                    onClick = { menuExpanded = false }
                )
            }
        }
    )
}

@Composable
private fun MainContent() {
    val scaffoldState = androidx.compose.material.rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        Text(
            text = stringResource(R.string.lorem_ipsum),
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(state = rememberScrollState())
        )

        FloatingActionButton(modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset((-16).dp, (-16).dp), onClick = {
                coroutineScope.launch {
                  scaffoldState.snackbarHostState.showSnackbar(message = "What the fuck?")
                }
            }
        ) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SampleAppPreview() {
    ReaderApp()
}