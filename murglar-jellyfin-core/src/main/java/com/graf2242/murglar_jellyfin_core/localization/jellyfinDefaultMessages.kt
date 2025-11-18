package com.graf2242.murglar_jellyfin_core.localization

import com.badmanners.murglar.lib.core.localization.DefaultMessages


object JellyfinDefaultMessages : DefaultMessages(), JellyfinMessages {
    override val serviceName = "Jellyfin"
    override val serverUrlSummary = "Jellyfin server address"
    override val serverUrlTitle = "Server Address"
}