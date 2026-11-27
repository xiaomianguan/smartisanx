package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.control.SmartisanCleanerDial
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap

@Composable
internal fun CleanerDemo(onBack: () -> Unit) {
    var scanning by rememberSaveable { mutableStateOf(false) }
    SamplePageScaffold(title = "垃圾清理", onBack = onBack) {
        SmartisanListVerticalGap(height = 9.dp)
        Box(
            Modifier.fillMaxWidth().height(183.7.dp),
            contentAlignment = Alignment.Center,
        ) {
            SmartisanCleanerDial(scanning)
        }
        SmartisanGroup {
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Single,
                title = if (scanning) "停止扫描动画" else "播放扫描动画",
                onClick = { scanning = !scanning },
            )
        }
    }
}
