package com.samsung.developer.spendemo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.developer.spendemo.ui.theme.SPenDemoTheme

class MainActivity : ComponentActivity() {

    private val sPenViewModel = SPenViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SPenDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SPen(LocalContext.current, sPenViewModel)
                }
            }
        }
    }
}

@Composable
fun SPen(context: Context, sPenViewModel: SPenViewModel) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Connect

        Text(
            text = context.getString(R.string.spen_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(onClick = {
            sPenViewModel.communication(context)
        }) {
            Text(context.getString(
                if (sPenViewModel.connected) R.string.spen_disconnect
                else R.string.spen_connect))
        }

        // Actions

        if (sPenViewModel.connected) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                context.getString(R.string.spen_subtitle),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SPenAction(SPenAction.LEFT.symbol, sPenViewModel.action == SPenAction.LEFT)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SPenAction(SPenAction.UP.symbol, sPenViewModel.action == SPenAction.UP)
                    SPenAction(SPenAction.ACTION.symbol, sPenViewModel.action != SPenAction.NONE)
                    SPenAction(SPenAction.DOWN.symbol, sPenViewModel.action == SPenAction.DOWN)
                }
                SPenAction(SPenAction.RIGHT.symbol, sPenViewModel.action == SPenAction.RIGHT)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(context.getString(R.string.spen_airmotion))
                Text(sPenViewModel.airMotion)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Try

            Text(
                text = context.getString(R.string.spen_try),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = sPenViewModel.tryAction.symbol,
                fontSize = 50.sp
            )
        }

        // Error
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                sPenViewModel.errorState,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }

}

@Composable
fun SPenAction(text: String, selected: Boolean) {
    Card(shape = RoundedCornerShape(8.dp)) {
        Text(
            text,
            modifier = Modifier
                .background(
                    if (selected) MaterialTheme.colors.primary
                    else Color.LightGray)
                .padding(16.dp),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    SPenDemoTheme {
        SPen(LocalContext.current, SPenViewModel())
    }
}