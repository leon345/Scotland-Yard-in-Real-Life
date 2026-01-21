package de.leonseeger.scotlandyardinreallife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import de.leonseeger.scotlandyardinreallife.game.CreateGameViewModelFactory
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.game.gateway.FirebaseGateway
import de.leonseeger.scotlandyardinreallife.navigation.NavigationRoutes
import de.leonseeger.scotlandyardinreallife.ui.screens.GameLobbyScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.HomeScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.JoinGameScreen
import de.leonseeger.scotlandyardinreallife.ui.theme.ScotlandYardInRealLifeTheme

class MainActivity : ComponentActivity() {
    private val firebaseGateway = FirebaseGateway(FirebaseFirestore.getInstance())
    private val gameLobbyViewModel: CreateGameViewModel by viewModels {
        CreateGameViewModelFactory(
            gameCatalogue = firebaseGateway, playerCatalogue = firebaseGateway
        )
    }
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScotlandYardInRealLifeTheme {
                navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        viewModel = gameLobbyViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: CreateGameViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavigationRoutes.HOME) {
            HomeScreen(
                onCreateGame = {
                    navController.navigate(
                        NavigationRoutes.gameLobby(mode = "CREATE")
                    )
                },
                onJoinGame = {
                    navController.navigate(NavigationRoutes.JOIN_GAME)
                }
            )
        }


        composable(NavigationRoutes.JOIN_GAME) {
            JoinGameScreen(
                onJoinWithCode = { gameCode ->
                    navController.navigate(
                        NavigationRoutes.gameLobby(mode = "JOIN", gameCode = gameCode)
                    )
                }
            )
        }


        composable(
            route = NavigationRoutes.GAME_LOBBY,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                },
                navArgument("gameCode") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "CREATE"
            val gameCode = backStackEntry.arguments?.getString("gameCode")
            val playerId = "user_${System.currentTimeMillis()}"

            GameLobbyScreen(
                viewModel = viewModel,
                mode = mode,
                gameId = if (gameCode.isNullOrEmpty()) null else gameCode,
                playerId = playerId,
                onStartGame = {
                    // TODO: Navigation zum aktiven Game Screen
                    navController.popBackStack(NavigationRoutes.HOME, inclusive = false)
                }
            )
        }
    }
}
