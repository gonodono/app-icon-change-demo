package com.gonodono.appiconchangedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var iconChangeManager: IconChangeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = IconChangeManager(this).also { iconChangeManager = it }

        setContent {
            Content(
                manager.aliases,
                manager.currentAlias,
                manager::currentAlias::set,
                manager.isIconChangeActivated,
                manager::isIconChangeActivated::set,
                manager.determineStartMode(intent, savedInstanceState),
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        iconChangeManager.onSaveInstanceState(outState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    aliases: List<ActivityAlias>,
    currentAlias: ActivityAlias,
    setCurrentAlias: (ActivityAlias) -> Unit,
    isIconChangeActivated: Boolean,
    setIsActivated: (Boolean) -> Unit,
    startMode: IconChangeManager.StartMode
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFFDDDDDD))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        MainContent(
            aliases = aliases,
            currentAlias = currentAlias,
            setCurrentAlias = setCurrentAlias,
            isIconChangeActivated = isIconChangeActivated,
            setIsActivated = setIsActivated,
            modifier = Modifier.padding(paddingValues)
        )
    }

    if (startMode != IconChangeManager.StartMode.Normal) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = startMode.toString(),
                duration = SnackbarDuration.Short
            )
        }
    }
}

@Composable
private fun MainContent(
    aliases: List<ActivityAlias>,
    currentAlias: ActivityAlias,
    setCurrentAlias: (ActivityAlias) -> Unit,
    isIconChangeActivated: Boolean,
    setIsActivated: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider()
        Text(
            text = "Current alias:\n${currentAlias.simpleName}",
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )

        if (isIconChangeActivated) {
            OutlinedButton({ setIsActivated(false) }) { Text("Deactivate") }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(aliases) { alias ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.run {
                            if (alias == currentAlias) alpha(0.3F)
                            else clickable { setCurrentAlias(alias) }
                        }
                    ) {
                        DrawableIcon(alias.icon)
                        Text(alias.title)
                    }
                }
            }
        } else {
            OutlinedButton({ setIsActivated(true) }) { Text("Activate") }
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