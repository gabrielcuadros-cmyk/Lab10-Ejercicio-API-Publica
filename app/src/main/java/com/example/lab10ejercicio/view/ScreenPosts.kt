package com.example.lab10ejercicio.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10ejercicio.data.PostApiService
import com.example.lab10ejercicio.data.PostModel
import kotlinx.coroutines.launch

// Guardamos la lista en memoria local compartida
val listaPostsLocal = mutableStateListOf<PostModel>()

@Composable
fun ContenidoPostsListado(navController: NavHostController, servicio: PostApiService) {
    var cargando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (listaPostsLocal.isEmpty()) {
            cargando = true
            try {
                val listado = servicio.selectPosts()
                listaPostsLocal.clear()
                listado.take(15).forEach { listaPostsLocal.add(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cargando = false
            }
        }
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("PUBLICACIONES DE LA API", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
            }
            items(listaPostsLocal) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.8f)) {
                            Text(text = "ID: ${item.id}", fontWeight = FontWeight.Bold)
                            Text(text = item.title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(text = item.body, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { navController.navigate("postVer/${item.id}") }) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { navController.navigate("postDel/${item.id}") }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoPostEditar(navController: NavHostController, servicio: PostApiService, pid: Int = 0) {
    val id by remember { mutableStateOf(pid) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (id != 0) {
        LaunchedEffect(Unit) {
            val localItem = listaPostsLocal.find { it.id == id }
            if (localItem != null) {
                title = localItem.title
                body = localItem.body
            } else {
                try {
                    val objPost = servicio.selectPost(id.toString())
                    title = objPost.body()?.title ?: ""
                    body = objPost.body()?.body ?: ""
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = if (id == 0) "Nuevo Post" else "Editar Post", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Contenido") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = !enviando,
            onClick = {
                enviando = true
                scope.launch {
                    try {
                        val objPost = PostModel(id = id, title = title, body = body)

                        if (id == 0) {
                            // Petición real a la API
                            val res = servicio.insertPost(objPost)
                            Log.d("RETROFIT", "POST enviado: ${res.code()}")

                            // Actualización en lista local
                            val nuevoId = if (listaPostsLocal.isNotEmpty()) listaPostsLocal.maxOf { it.id } + 1 else 1
                            listaPostsLocal.add(0, objPost.copy(id = nuevoId))
                        } else {
                            // Petición real a la API
                            val res = servicio.updatePost(id.toString(), objPost)
                            Log.d("RETROFIT", "PUT enviado: ${res.code()}")

                            // Actualización en lista local
                            val index = listaPostsLocal.indexOfFirst { it.id == id }
                            if (index != -1) {
                                listaPostsLocal[index] = objPost
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        enviando = false
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (enviando) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            } else {
                Text("Guardar en la API")
            }
        }
    }
}

@Composable
fun ContenidoPostEliminar(navController: NavHostController, servicio: PostApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar Registro") },
            text = { Text("¿Deseas eliminar el Post con ID $id en la API?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    scope.launch {
                        try {
                            val res = servicio.deletePost(id.toString())
                            Log.d("RETROFIT", "DELETE enviado: ${res.code()}")

                            // Eliminación en lista local
                            listaPostsLocal.removeAll { it.id == id }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            navController.popBackStack()
                        }
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    navController.popBackStack()
                }) { Text("Cancelar") }
            }
        )
    }
}