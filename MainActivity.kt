package com.afterstatus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Bg = Color(0xFF0E0D17)
private val Panel = Color(0xFF1B1928)
private val Purple = Color(0xFFAA8CFA)
private val Pink = Color(0xFFF879B7)
private val Muted = Color(0xFFA7A4B8)
private val White = Color(0xFFF7F5FF)

data class Character(val id: String, val name: String, val handle: String, val tagline: String, val fandom: String, val emoji: String, val greeting: String)
data class Post(val id: Int, val author: String, val handle: String, val body: String, val likes: Int, val emoji: String)
data class Message(val user: Boolean, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AfterStatusApp() }
    }
}

@Composable
fun AfterStatusApp() {
    val characters = remember {
        listOf(
            Character("nova", "Nova Reyes", "@novainorbit", "Making a little magic out of ordinary days.", "Originals", "✨", "Hey, you made it ✨ Tell me what's on your mind."),
            Character("kai", "Kai Morgan", "@kaiontherun", "Late-night thoughts and early-morning plans.", "Slice of Life", "🌙", "Hey! Want to talk, plan something, or just hang out?"),
            Character("mira", "Mira Sol", "@mirasol", "Collecting stories, playlists, and tiny victories.", "Originals", "☀️", "Hi there ☀️ I saved you a spot. How's your day going?"),
            Character("rowan", "Rowan Vale", "@rowanvale", "A little mysterious. Very good at listening.", "Fantasy", "🌿", "You found me. Come sit by the fire—what brings you here?")
        )
    }
    val posts = remember {
        mutableStateListOf(
            Post(1, "Nova Reyes", "@novainorbit", "Reminder: you don't have to turn every quiet day into a productive one. Sometimes being here is enough.", 128, "✨"),
            Post(2, "Kai Morgan", "@kaiontherun", "Made a playlist for the walk home. The sky did that blue-and-pink thing again. 🌆", 86, "🌙"),
            Post(3, "Mira Sol", "@mirasol", "Tiny victory: I finally started the thing I've been putting off. Starting counts.", 204, "☀️"),
            Post(4, "Rowan Vale", "@rowanvale", "The old library is open late tonight. Some stories are better after dark.", 52, "🌿")
        )
    }
    var tab by remember { mutableStateOf("Feed") }
    var selectedCharacter by remember { mutableStateOf<Character?>(null) }
    var showComposer by remember { mutableStateOf(false) }
    var newPost by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var fandom by remember { mutableStateOf("All") }
    var displayName by remember { mutableStateOf("You") }
    var bio by remember { mutableStateOf("Just here for good stories.") }
    var editProfile by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    var localAI by remember { mutableStateOf(false) }
    var endpoint by remember { mutableStateOf("http://192.168.1.10:11434/v1/chat/completions") }
    var model by remember { mutableStateOf("qwen2.5:3b") }
    val chats = remember { mutableStateMapOf<String, MutableList<Message>>() }
    val snackbar = remember { SnackbarHostState() }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Purple, background = Bg, surface = Panel,
        onBackground = White, onSurface = White, secondary = Pink
    )) {
        Scaffold(
            containerColor = Bg,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = White) {
                    listOf("Feed", "Discover", "Chats", "Profile").forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item; selectedCharacter = null },
                            icon = { Text(when(item) { "Feed" -> "▤"; "Discover" -> "✧"; "Chats" -> "☏"; else -> "●" }, fontSize = 20.sp) },
                            label = { Text(item) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Purple, selectedTextColor = Purple, indicatorColor = Purple.copy(alpha = .16f), unselectedTextColor = Muted)
                        )
                    }
                }
            }
        ) { pad ->
            when {
                selectedCharacter != null && tab == "Chats" -> ChatScreen(
                    character = selectedCharacter!!,
                    messages = chats[selectedCharacter!!.id] ?: mutableStateListOf(Message(false, selectedCharacter!!.greeting)),
                    onBack = { selectedCharacter = null },
                    endpoint = endpoint, model = model, localAI = localAI
                )
                selectedCharacter != null && tab != "Chats" -> CharacterScreen(
                    character = selectedCharacter!!,
                    onBack = { selectedCharacter = null },
                    onChat = { c ->
                        if (!chats.containsKey(c.id)) chats[c.id] = mutableStateListOf(Message(false, c.greeting))
                        tab = "Chats"; selectedCharacter = c
                    }
                )
                tab == "Feed" -> FeedScreen(
                    posts = posts,
                    characters = characters,
                    displayName = displayName,
                    onCharacter = { selectedCharacter = it },
                    onCompose = { showComposer = true },
                    modifier = Modifier.padding(pad)
                )
                tab == "Discover" -> DiscoverScreen(
                    characters = characters, query = query, onQuery = { query = it },
                    fandom = fandom, onFandom = { fandom = it },
                    onCharacter = { selectedCharacter = it }, modifier = Modifier.padding(pad)
                )
                tab == "Chats" -> ChatsScreen(
                    characters = characters, chats = chats,
                    onOpen = { c ->
                        if (!chats.containsKey(c.id)) chats[c.id] = mutableStateListOf(Message(false, c.greeting))
                        selectedCharacter = c
                    }, modifier = Modifier.padding(pad)
                )
                else -> ProfileScreen(
                    name = displayName, bio = bio,
                    onEdit = { editProfile = true }, onSettings = { settings = true },
                    chatCount = chats.size, postCount = posts.count { it.handle == "@you" }, modifier = Modifier.padding(pad)
                )
            }
        }
        if (showComposer) {
            AlertDialog(
                onDismissRequest = { showComposer = false },
                title = { Text("Share a thought") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = newPost, onValueChange = { newPost = it }, label = { Text("What's on your mind?") }, minLines = 3)
                        Text("Posts are local demo content on this device.", color = Muted, fontSize = 12.sp)
                    }
                },
                confirmButton = { TextButton(onClick = {
                    if (newPost.isNotBlank()) posts.add(0, Post((posts.maxOfOrNull { it.id } ?: 0) + 1, displayName, "@you", newPost.trim(), 0, "💜"))
                    newPost = ""; showComposer = false
                }) { Text("Post") } },
                dismissButton = { TextButton(onClick = { showComposer = false }) { Text("Cancel") } },
                containerColor = Panel
            )
        }
        if (editProfile) {
            var tempName by remember { mutableStateOf(displayName) }
            var tempBio by remember { mutableStateOf(bio) }
            AlertDialog(
                onDismissRequest = { editProfile = false },
                title = { Text("Edit profile") },
                text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(tempName, { tempName = it }, label = { Text("Name") })
                    OutlinedTextField(tempBio, { tempBio = it }, label = { Text("Bio") })
                } },
                confirmButton = { TextButton(onClick = { displayName = tempName.ifBlank { "You" }; bio = tempBio; editProfile = false }) { Text("Save") } },
                dismissButton = { TextButton(onClick = { editProfile = false }) { Text("Cancel") } },
                containerColor = Panel
            )
        }
        if (settings) {
            AlertDialog(
                onDismissRequest = { settings = false },
                title = { Text("AI & app settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Use local AI", modifier = Modifier.weight(1f))
                            Switch(checked = localAI, onCheckedChange = { localAI = it })
                        }
                        OutlinedTextField(endpoint, { endpoint = it }, label = { Text("AI endpoint") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                        OutlinedTextField(model, { model = it }, label = { Text("Model name") })
                        Text(if (localAI) "Local inference has no app token fee, but still uses electricity." else "Built-in demo replies work offline.", color = Muted, fontSize = 12.sp)
                        Text("No subscriptions, purchases, or energy meter.", color = Purple, fontSize = 12.sp)
                    }
                },
                confirmButton = { TextButton(onClick = { settings = false }) { Text("Done") } },
                containerColor = Panel
            )
        }
    }
}

@Composable
fun PageTitle(eyebrow: String, title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(eyebrow.uppercase(), color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text(title, color = White, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) Text(subtitle, color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun FeedScreen(posts: MutableList<Post>, characters: List<Character>, displayName: String, onCharacter: (Character) -> Unit, onCompose: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().background(Bg), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            PageTitle("AfterStatus", "Your little corner\nof the internet.", "Stories, characters, and conversations—without paywalls.")
        }
        item {
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).clickable { onCompose() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("＋", color = Purple, fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Text("Share a thought…", color = Muted, modifier = Modifier.weight(1f))
                Text("➤", color = Purple)
            }
        }
        item { Text("Characters to meet", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                characters.take(3).forEach { c ->
                    Column(Modifier.weight(1f).background(Panel, RoundedCornerShape(18.dp)).clickable { onCharacter(c) }.padding(10.dp)) {
                        Box(Modifier.fillMaxWidth().height(70.dp).background(Purple.copy(alpha = .13f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Text(c.emoji, fontSize = 30.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(c.name, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(c.fandom, color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
        item { Text("The feed", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        items(posts, key = { it.id }) { post -> PostCard(post, displayName) }
        item { Text("Fictional community demo. Posts and characters are sample content.", color = Muted, fontSize = 11.sp) }
    }
}

@Composable
fun PostCard(post: Post, displayName: String) {
    var liked by remember(post.id) { mutableStateOf(false) }
    var count by remember(post.id) { mutableIntStateOf(post.likes) }
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(Purple.copy(alpha = .2f), CircleShape), contentAlignment = Alignment.Center) { Text(post.emoji, fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(if (post.handle == "@you") displayName else post.author, color = White, fontWeight = FontWeight.Bold)
                Text(post.handle, color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("•••", color = Muted)
        }
        Text(post.body, color = White, fontSize = 15.sp, lineHeight = 22.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (liked) "♥ $count" else "♡ $count", color = if (liked) Pink else Muted, modifier = Modifier.clickable { liked = !liked; count += if (liked) 1 else -1 })
            Spacer(Modifier.width(18.dp))
            Text("○ Reply", color = Muted)
        }
    }
}

@Composable
fun DiscoverScreen(characters: List<Character>, query: String, onQuery: (String) -> Unit, fandom: String, onFandom: (String) -> Unit, onCharacter: (Character) -> Unit, modifier: Modifier = Modifier) {
    val filtered = characters.filter { (fandom == "All" || it.fandom == fandom) && (query.isBlank() || it.name.contains(query, true) || it.tagline.contains(query, true)) }
    LazyColumn(modifier.fillMaxSize().background(Bg), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageTitle("Explore", "Find your next favorite", "Meet original characters and start a conversation.") }
        item { OutlinedTextField(query, onQuery, modifier = Modifier.fillMaxWidth(), label = { Text("Search characters") }, singleLine = true) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Originals", "Fantasy", "Slice of Life").forEach { item ->
                    FilterChip(selected = fandom == item, onClick = { onFandom(item) }, label = { Text(item, fontSize = 12.sp) })
                }
            }
        }
        items(filtered, key = { it.id }) { c ->
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).clickable { onCharacter(c) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).background(Purple.copy(alpha = .15f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text(c.emoji, fontSize = 27.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(c.name, color = White, fontWeight = FontWeight.Bold)
                    Text(c.tagline, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                    Text(c.fandom.uppercase(), color = Purple, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                Text("›", color = Muted, fontSize = 25.sp)
            }
        }
    }
}

@Composable
fun CharacterScreen(character: Character, onBack: () -> Unit, onChat: (Character) -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth()) { Text("‹ Back", color = Purple, modifier = Modifier.clickable { onBack() }) }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(190.dp).background(Purple.copy(alpha = .15f), RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) { Text(character.emoji, fontSize = 64.sp) }
        Text(character.name, color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(character.handle, color = Muted)
        Text(character.tagline, color = White, fontSize = 16.sp)
        Text("Fictional character · ${character.fandom}", color = Muted, fontSize = 12.sp)
        Button(onClick = { onChat(character) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple, contentColor = Color.Black)) { Text("Start chatting", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun ChatsScreen(characters: List<Character>, chats: Map<String, List<Message>>, onOpen: (Character) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().background(Bg), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageTitle("Messages", "Your conversations", "Choose a character to continue or start chatting.") }
        val withChats = characters.filter { chats.containsKey(it.id) }
        if (withChats.isEmpty()) item {
            Text("No chats yet. Open Discover and choose a character to start.", color = Muted, modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(18.dp))
        }
        items(withChats, key = { it.id }) { c ->
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).clickable { onOpen(c) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(c.emoji, fontSize = 30.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(c.name, color = White, fontWeight = FontWeight.Bold)
                    Text(chats[c.id]?.lastOrNull()?.text ?: c.greeting, color = Muted, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun ChatScreen(character: Character, messages: MutableList<Message>, onBack: () -> Unit, endpoint: String, model: String, localAI: Boolean) {
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Purple, fontSize = 28.sp, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(12.dp))
            Text(character.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(8.dp))
            Column { Text(character.name, color = White, fontWeight = FontWeight.Bold); Text("Fictional character", color = Muted, fontSize = 11.sp) }
        }
        androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("You're chatting with a fictional character.", color = Muted, fontSize = 11.sp) }
            items(messages) { m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.user) Arrangement.End else Arrangement.Start) {
                    Text(m.text, color = White, modifier = Modifier.widthIn(max = 290.dp).background(if (m.user) Purple.copy(alpha = .30f) else Panel, RoundedCornerShape(17.dp)).padding(13.dp))
                }
            }
            if (sending) item { CircularProgressIndicator(color = Purple, modifier = Modifier.size(20.dp)) }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f), placeholder = { Text("Message ${character.name}…") }, maxLines = 4)
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty() && !sending) {
                        draft = ""; messages.add(Message(true, text)); sending = true
                        scope.launch {
                            val answer = if (localAI) {
                                try { requestLocalAI(endpoint, model, character, messages) }
                                catch (_: Exception) { demoReply(text) }
                            } else demoReply(text)
                            messages.add(Message(false, answer)); sending = false
                        }
                    }
                },
                enabled = draft.isNotBlank() && !sending,
                colors = ButtonDefaults.buttonColors(containerColor = Purple, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 15.dp)
            ) { Text("↑", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

fun demoReply(text: String): String {
    val lower = text.lowercase()
    return when {
        "sad" in lower || "bad day" in lower || "upset" in lower -> "I'm sorry it's been heavy today. You don't have to solve everything all at once. Want to tell me what happened?"
        "hello" in lower || "hey" in lower || "hi" in lower -> "Hey! I'm glad you're here. What would make this moment a little better?"
        "?" in text -> "That's a good question. My first thought is to take it one step at a time—and I'd love to hear what you think, too."
        else -> "I'm listening. There's room for the messy, unfinished version of the story, too."
    }
}

suspend fun requestLocalAI(endpoint: String, model: String, character: Character, messages: List<Message>): String = withContext(Dispatchers.IO) {
    val url = URL(endpoint)
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"; connectTimeout = 8000; readTimeout = 90000
        doOutput = true; setRequestProperty("Content-Type", "application/json")
    }
    val history = JSONArray().put(JSONObject().put("role", "system").put("content", "You are ${character.name}, a fictional roleplay character. Personality: ${character.tagline} Be warm and concise."))
    messages.takeLast(18).forEach { history.put(JSONObject().put("role", if (it.user) "user" else "assistant").put("content", it.text)) }
    val body = JSONObject().put("model", model).put("messages", history).put("stream", false)
    conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
    val responseCode = conn.responseCode
    val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
    val response = stream.bufferedReader().use { it.readText() }
    conn.disconnect()
    if (responseCode !in 200..299) throw IllegalStateException("HTTP $responseCode")
    val obj = JSONObject(response)
    obj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
}

@Composable
fun ProfileScreen(name: String, bio: String, onEdit: () -> Unit, onSettings: () -> Unit, chatCount: Int, postCount: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Bg).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.size(100.dp).background(Purple.copy(alpha = .2f), CircleShape), contentAlignment = Alignment.Center) { Text("●", color = Purple, fontSize = 42.sp) }
        Text(name, color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(bio, color = Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Posts", "$postCount", Modifier.weight(1f))
            StatCard("Chats", "$chatCount", Modifier.weight(1f))
            StatCard("Paywalls", "0", Modifier.weight(1f))
        }
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = White)) { Text("Edit profile") }
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = White)) { Text("AI & app settings") }
        Text("AfterStatus is a free starter app. Demo chats are scripted unless you connect your own local AI server. Conversations are held in memory in this starter build.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Panel, RoundedCornerShape(16.dp)).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Muted, fontSize = 11.sp)
    }
}
