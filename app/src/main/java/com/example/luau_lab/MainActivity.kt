package com.example.luau_lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LuauLabScreen() }
    }
}

private object LuauEngine {
    init { System.loadLibrary("luaulab") }
    external fun execute(source: String): String
}

@Composable
private fun LuauLabScreen() {
    var source by remember {
        mutableStateOf(
            """-- Welcome to Luau Lab
local name = "Luau"
local version = 1

print("Hello from " .. name .. "!")
print("Version:", version)

local numbers = {2, 4, 6, 8}
local total = 0

for _, value in ipairs(numbers) do
    total += value
end

print("Total:", total)"""
        )
    }
    var output by remember { mutableStateOf("Ready. Press RUN to execute your Luau code.") }
    var running by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF3434),
            background = Color(0xFF090909),
            surface = Color(0xFF151515)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF090909)).padding(16.dp)
        ) {
            Text("LUAU LAB", color = Color(0xFFFF3B3B), fontSize = 24.sp)
            Text(
                "Standalone Luau learning environment",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !running,
                    onClick = {
                        source = """print("Hello Luau!")
local x = 10
local y = 32
print("x + y =", x + y)"""
                    }
                ) { Text("Example") }

                Button(
                    enabled = !running,
                    onClick = {
                        source = """-- Try changing the values
local player = {name = "Alex", level = 7}

if player.level >= 5 then
    print(player.name .. " unlocked the next lesson")
else
    print("Keep learning!")
end"""
                    }
                ) { Text("Lesson") }

                Spacer(Modifier.weight(1f))
                Button(
                    enabled = !running,
                    onClick = {
                        running = true
                        output = "Running..."
                        val code = source
                        // Execute away from the UI thread.
                        kotlinx.coroutines.MainScope().launch {
                            val result = withContext(Dispatchers.Default) {
                                try { LuauEngine.execute(code) }
                                catch (e: Exception) { "App error: ${e.message}" }
                            }
                            output = result
                            running = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB51F2A))
                ) { Text(if (running) "RUNNING" else "RUN") }
            }

            Spacer(Modifier.height(12.dp))
            Text("EDITOR", color = Color(0xFFFF7777), fontSize = 12.sp)
            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFFE8E8E8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF3434),
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color(0xFFFF3434),
                    focusedContainerColor = Color(0xFF111111),
                    unfocusedContainerColor = Color(0xFF111111)
                )
            )
            Spacer(Modifier.height(12.dp))
            Text("OUTPUT / CONSOLE", color = Color(0xFFFF7777), fontSize = 12.sp)
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp, max = 170.dp),
                color = Color(0xFF111111),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    output,
                    modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                    color = Color(0xFFB8F5C4),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Educational sandbox only • No Roblox connection or game injection",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
