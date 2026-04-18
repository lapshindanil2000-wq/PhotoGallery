package com.example.photogallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.photogallery.database.FavoriteDatabase
import com.example.photogallery.database.FavoritePhoto
import com.example.photogallery.database.FavoriteRepository
import com.example.photogallery.ui.PhotoGalleryUiState
import com.example.photogallery.ui.PhotoGalleryViewModel
import com.example.photogallery.ui.PhotoGalleryViewModelFactory
import com.example.photogallery.ui.theme.PhotoGalleryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoGalleryTheme {
                val database = FavoriteDatabase.getInstance(this)
                val repository = FavoriteRepository(database.favoritePhotoDao())
                val viewModel: PhotoGalleryViewModel = viewModel(
                    factory = PhotoGalleryViewModelFactory(repository)
                )
                PhotoGalleryApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryApp(viewModel: PhotoGalleryViewModel) {
    var showFavorites by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showFavorites) "Favorites" else "Photo Gallery") },
                navigationIcon = {
                    if (showFavorites) {
                        IconButton(onClick = { showFavorites = false }) {
                            Text("←")
                        }
                    }
                },
                actions = {
                    // Кнопка меню (три точки)
                    IconButton(onClick = { menuExpanded = true }) {
                        Text("⋮")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Favorites") },
                            onClick = {
                                showFavorites = true
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Favorites") },
                            onClick = {
                                viewModel.clearAllFavorites()
                                menuExpanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (showFavorites) {
                FavoritesScreen(viewModel)
            } else {
                PhotoGalleryScreen(viewModel)
            }
        }
    }
}

@Composable
fun PhotoGalleryScreen(viewModel: PhotoGalleryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is PhotoGalleryUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Generating images...", modifier = Modifier.padding(top = 16.dp), color = Color.White)
                }
            }
            is PhotoGalleryUiState.Success -> {
                val photos = (uiState as PhotoGalleryUiState.Success).photos
                val filteredPhotos by remember(searchText, photos) {
                    derivedStateOf {
                        if (searchText.isBlank()) photos
                        else photos.filter { it.prompt.contains(searchText, ignoreCase = true) }
                    }
                }
                Column {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search by keyword") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Blue,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Blue,
                            unfocusedLabelColor = Color.DarkGray,
                            cursorColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    LazyColumn {
                        items(filteredPhotos) { photo ->
                            PhotoItem(photo = photo, onItemClick = { viewModel.saveToFavorites(photo) })
                        }
                        if (filteredPhotos.isEmpty() && searchText.isNotBlank()) {
                            item {
                                Text(
                                    text = "No results for \"$searchText\"",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            is PhotoGalleryUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${(uiState as PhotoGalleryUiState.Error).message}", color = Color.Red, modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.generateImages() }) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
fun PhotoItem(photo: com.example.photogallery.model.Photo, onItemClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.url)
                .crossfade(true)
                .build(),
            contentDescription = photo.prompt,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = photo.prompt,
            modifier = Modifier.padding(top = 4.dp),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun FavoritesScreen(viewModel: PhotoGalleryViewModel) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No favorites yet", color = Color.White)
        }
    } else {
        LazyColumn {
            items(favorites) { favorite ->
                FavoriteItem(favorite = favorite, onItemClick = { viewModel.removeFromFavorites(favorite) })
            }
        }
    }
}

@Composable
fun FavoriteItem(favorite: FavoritePhoto, onItemClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(favorite.url)
                .crossfade(true)
                .build(),
            contentDescription = favorite.prompt,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = favorite.prompt,
            modifier = Modifier.padding(top = 4.dp),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}