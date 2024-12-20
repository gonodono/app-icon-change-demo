package com.gonodono.appiconchangedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var iconChangeManager: IconChangeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IconChangeManager's current internal implementation assumes that a
        // new one is created for each Activity instance, and that there's only
        // ever one of each running at a time. If you intend to cache it in a
        // ViewModel or an Application or the like, you'll need to refactor the
        // Activity parameter and start mode handling, at least.
        val manager = IconChangeManager(this).also { iconChangeManager = it }
        val startMode = manager.determineStartMode(intent, savedInstanceState)

        // Nothing here changes without the Activity recreating or restarting,
        // so the IconChangeManager is just passed in directly for the demo.
        setContent { Content(manager, startMode) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // This is necessary only if you're using determineStartMode().
        iconChangeManager.onSaveInstanceState(outState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    manager: IconChangeManager,
    startMode: IconChangeManager.StartMode
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFFEEEEEE))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        MainContent(manager, Modifier.padding(paddingValues))
    }

    if (startMode == IconChangeManager.StartMode.Normal) return

    LaunchedEffect(Unit) {
        snackbarHostState.showSnackbar(
            message = startMode.toString(),
            duration = SnackbarDuration.Short
        )
    }
}

@Composable
private fun MainContent(
    manager: IconChangeManager,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider()

        val isActivated = manager.isIconChangeActivated

        OutlinedButton({ manager.isIconChangeActivated = !isActivated }) {
            Text(if (isActivated) "Deactivate" else "Activate")
        }

        val header = buildAnnotatedString {
            appendLine("Current alias:")
            val span = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            withStyle(span) { append(manager.currentAlias.simpleName) }
        }
        Text(text = header, textAlign = TextAlign.Center)

        if (!isActivated) return

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(20.dp)
        ) {
            items(manager.selectableAliases) { alias ->
                val itemModifier = if (manager.currentAlias != alias) {
                    Modifier
                        .clickable { manager.currentAlias = alias }
                        .alpha(0.5F)
                        .scale(0.9F)
                } else {
                    Modifier.scale(1.1F)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = itemModifier
                ) {
                    DrawableIcon(alias.icon)
                    Text(alias.title)
                }
            }
        }
    }
}

@Composable
private fun DrawableIcon(id: Int) {
    val drawable = with(LocalContext.current) {
        remember(id) { resources.getDrawable(id, theme) }
    }
    Canvas(Modifier.size(80.dp)) {
        val side = 80.dp.roundToPx()
        drawable.setBounds(0, 0, side, side)
        drawable.draw(drawContext.canvas.nativeCanvas)
    }
}