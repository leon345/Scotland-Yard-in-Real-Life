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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import de.leonseeger.scotlandyardinreallife.ui.models.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole
import de.leonseeger.scotlandyardinreallife.gateway.FirebaseGateway
import de.leonseeger.scotlandyardinreallife.navigation.NavigationRoutes
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMapData
import de.leonseeger.scotlandyardinreallife.ui.models.GameViewModelFactory
import de.leonseeger.scotlandyardinreallife.ui.screens.DefineMapScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.GameEndScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.GameLobbyScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.GameRunningScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.GameSettingScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.HomeScreen
import de.leonseeger.scotlandyardinreallife.ui.screens.JoinGameScreen
import de.leonseeger.scotlandyardinreallife.ui.theme.ScotlandYardInRealLifeTheme
import org.maplibre.android.MapLibre
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val firebaseGateway = FirebaseGateway(FirebaseFirestore.getInstance())
    private val gameLobbyViewModel: CreateGameViewModel by viewModels {
        GameViewModelFactory(
            gameCatalogue = firebaseGateway, playerCatalogue = firebaseGateway
        )
    }
    private lateinit var navController: NavHostController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)
        enableEdgeToEdge()
        setContent {
            val playMap = remember { PlayMapData() }
            ScotlandYardInRealLifeTheme {
                navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        viewModel = gameLobbyViewModel,
                        modifier = Modifier.padding(innerPadding),
                        firebaseGateway = firebaseGateway
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
    modifier: Modifier = Modifier,
    firebaseGateway: FirebaseGateway,
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavigationRoutes.HOME) {
            HomeScreen(
                onCreateGame = {
                    navController.navigate(NavigationRoutes.PRE_LOBBY)
                },
                onJoinGame = {
                    navController.navigate(NavigationRoutes.JOIN_GAME)
                }
            )
        }

        composable(NavigationRoutes.PRE_LOBBY) {
            DefineMapScreen(
                onMapDefined = { poly ->
                    viewModel.setPlayArea(poly)
                    navController.navigate(
                        NavigationRoutes.gameLobby(mode = "CREATE")
                    )
                }
            )
        }

        composable(NavigationRoutes.JOIN_GAME) {
            JoinGameScreen(
                navController = navController,
                viewModel = viewModel
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
            val playerId = "0" //TODO nicht schön

            GameLobbyScreen(
                viewModel = viewModel,
                mode = mode,
                gameId = if (gameCode.isNullOrEmpty()) null else gameCode,
                playerId = playerId,
                playArea = null,
                onStartGame = { ->
                    navController.navigate(NavigationRoutes.GAME_RUNNING)
                },
                onNavigateToSettings = {
                    navController.navigate(NavigationRoutes.GAME_SETTINGS)
                }
            )
        }

        composable(NavigationRoutes.GAME_SETTINGS) {
            GameSettingScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            NavigationRoutes.GAME_RUNNING
        ) {
            GameRunningScreen(
                viewModel = viewModel,
                onGameEnd = { winnerTeam ->
                    var msg: String
                    when (winnerTeam){
                        PlayerRole.DETECTIVE -> msg = "Die Detektive haben gewonnen"
                        PlayerRole.BANDIT -> msg = "Die Banditen sind entkommen"
                        null -> msg = "Unentschieden"
                    }
                    navController.navigate(NavigationRoutes.gameEnding(msg))
                }
            )
        }

        composable(
            NavigationRoutes.GAME_END,
            arguments = listOf(
                navArgument("winMessage") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val msg = backStackEntry.arguments?.getString("winMessage") ?: "Unentschieden"
            GameEndScreen(
                winMsg = msg,
                onBackHome = {
                    navController.navigate(NavigationRoutes.HOME)
                }
            )
        }
    }
}
