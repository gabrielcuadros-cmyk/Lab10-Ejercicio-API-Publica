package com.example.lab10ejercicio.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.lab10ejercicio.data.PostApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsApp() {
    val urlBase = "https://jsonplaceholder.typicode.com/" // API Pública Online
    val retrofit = Retrofit.Builder()
        .baseUrl(urlBase)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val servicio = retrofit.create(PostApiService::class.java)
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Publica CRUD", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("postNuevo") }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "posts", modifier = Modifier.padding(padding)) {
            composable("posts") { ContenidoPostsListado(navController, servicio) }
            composable("postNuevo") { ContenidoPostEditar(navController, servicio, 0) }
            composable("postVer/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                ContenidoPostEditar(navController, servicio, it.arguments!!.getInt("id"))
            }
            composable("postDel/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                ContenidoPostEliminar(navController, servicio, it.arguments!!.getInt("id"))
            }
        }
    }
}