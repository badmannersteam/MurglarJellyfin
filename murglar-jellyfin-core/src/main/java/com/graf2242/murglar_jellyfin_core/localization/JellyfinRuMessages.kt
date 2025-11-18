package com.graf2242.murglar_jellyfin_core.localization

import com.badmanners.murglar.lib.core.localization.RussianMessages


object JellyfinRuMessages : RussianMessages(), JellyfinMessages {
    override val serviceName = "Jellyfin"
    override val serverUrlSummary = "Адрес до сервера Jellyfin"
    override val serverUrlTitle = "Адрес сервера"
}