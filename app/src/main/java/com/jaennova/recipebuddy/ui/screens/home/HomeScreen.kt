package com.jaennova.recipebuddy.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jaennova.recipebuddy.data.model.Category
import com.jaennova.recipebuddy.data.model.FilteredMeal
import com.jaennova.recipebuddy.data.model.Meal
import com.jaennova.recipebuddy.ui.screens.appcomponets.ErrorMessage
import com.jaennova.recipebuddy.ui.screens.appcomponets.LoadingIndicator
import com.jaennova.recipebuddy.ui.screens.appcomponets.RecipeBottomNavigation
import com.jaennova.recipebuddy.ui.screens.appcomponets.RecipeTopAppBar
import com.jaennova.recipebuddy.viewmodel.HomeViewModel
import com.jaennova.recipebuddy.viewmodel.UIState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    navigateToDetail: (String) -> Unit,
    navController: NavController
) {
    val categoriesState by viewModel.categories.observeAsState()
    val randomMealState by viewModel.randomMeal.observeAsState()
    val categoryMealsState by viewModel.categoryMeals.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshAllData()
    }

    val isLoading = categoriesState is UIState.Loading ||
            randomMealState is UIState.Loading ||
            categoryMealsState is UIState.Loading

    Scaffold(
        topBar = { RecipeTopAppBar() },
        bottomBar = { RecipeBottomNavigation(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Título
                    item {
                        Text(
                            text = "¿Qué quieres cocinar hoy?",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Random Meal Section
                    item {
                        when (randomMealState) {
                            is UIState.Success -> {
                                val meal = (randomMealState as UIState.Success<Meal>).data
                                RandomFoodCard(
                                    foodName = meal.strMeal ?: "",
                                    foodCategory = meal.strCategory ?: "",
                                    foodArea = meal.strArea ?: "",
                                    foodImage = meal.strMealThumb ?: "",
                                    onClick = { navigateToDetail(meal.idMeal) }
                                )
                            }

                            is UIState.Error -> {
                                ErrorMessage(
                                    message = (randomMealState as UIState.Error).message,
                                    onRetry = { viewModel.loadRandomMeal() }
                                )
                            }

                            else -> { /* No hacer nada */
                            }
                        }
                    }

                    // Categories Section
                    item {
                        when (categoriesState) {
                            is UIState.Success -> {
                                val categories =
                                    (categoriesState as UIState.Success<List<Category>>).data
                                CategorySection(
                                    categories = categories,
                                    viewModel = viewModel, // Pasar el ViewModel para manejar la categoría seleccionada
                                    onViewAllClick = { /* Acción para ver todas las categorías */ }
                                )
                            }

                            is UIState.Error -> ErrorMessage(
                                message = (categoriesState as UIState.Error).message,
                                onRetry = { viewModel.loadCategories() }
                            )

                            else -> { /* No hacer nada */
                            }
                        }
                    }

                    // Category Meals Section
                    when (categoryMealsState) {
                        is UIState.Success -> {
                            val meals =
                                (categoryMealsState as UIState.Success<List<FilteredMeal>>).data
                            items(
                                count = (meals.size + 1) / 2, // Número de filas necesarias
                                key = { index -> index }
                            ) { rowIndex ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Primera comida de la fila
                                    val firstIndex = rowIndex * 2
                                    if (firstIndex < meals.size) {
                                        FoodCard(
                                            foodName = meals[firstIndex].strMeal,
                                            foodImage = meals[firstIndex].strMealThumb,
                                            onClick = { navigateToDetail(meals[firstIndex].idMeal) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Segunda comida de la fila
                                    val secondIndex = firstIndex + 1
                                    if (secondIndex < meals.size) {
                                        FoodCard(
                                            foodName = meals[secondIndex].strMeal,
                                            foodImage = meals[secondIndex].strMealThumb,
                                            onClick = { navigateToDetail(meals[secondIndex].idMeal) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        // Espacio vacío para mantener el layout consistente
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        is UIState.Error -> {
                            item {
                                ErrorMessage(
                                    message = (categoryMealsState as UIState.Error).message,
                                    onRetry = { viewModel.loadMealsByCategory("Beef") }
                                )
                            }
                        }

                        else -> { /* No hacer nada */
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(viewModel = viewModel(), {}, rememberNavController())
}
