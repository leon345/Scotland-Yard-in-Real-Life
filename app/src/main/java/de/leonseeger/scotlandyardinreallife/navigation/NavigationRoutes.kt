package de.leonseeger.scotlandyardinreallife.navigation


object NavigationRoutes {
    const val HOME = "home"
    const val JOIN_GAME = "join_game"
    const val GAME_LOBBY = "game_lobby/{mode}/{gameCode}"

    fun gameLobby(mode: String, gameCode: String = ""): String {
        return "game_lobby/$mode/$gameCode"
    }
}