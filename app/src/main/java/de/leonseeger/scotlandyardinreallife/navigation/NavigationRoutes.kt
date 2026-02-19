package de.leonseeger.scotlandyardinreallife.navigation


object NavigationRoutes {
    const val HOME = "home"
    const val PRE_LOBBY = "pre_lobby" //defining play area
    const val JOIN_GAME = "join_game"
    const val GAME_LOBBY = "game_lobby/{mode}/{gameCode}"
    const val GAME_SETTINGS = "game_settings"
    const val GAME_RUNNING = "game_running"
    const val GAME_END = "game_end/{winMessage}"

    fun gameLobby(mode: String, gameCode: String = ""): String {
        return "game_lobby/$mode/$gameCode"
    }

    fun gameRunning(gameId: String, playerId: String): String {
        return "game_running/$gameId/$playerId"
    }

    fun gameEnding(winMessage: String): String {
        return "game_end/$winMessage"
    }
}