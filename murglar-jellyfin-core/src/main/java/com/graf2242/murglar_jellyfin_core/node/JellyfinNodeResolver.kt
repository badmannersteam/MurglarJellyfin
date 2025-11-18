package com.graf2242.murglar_jellyfin_core.node

import com.badmanners.murglar.lib.core.model.node.NamedPath
import com.badmanners.murglar.lib.core.model.node.Node
import com.badmanners.murglar.lib.core.model.node.Node.Companion.to
import com.badmanners.murglar.lib.core.model.node.NodeParameters.PagingType.NON_PAGEABLE
import com.badmanners.murglar.lib.core.model.node.NodeParameters.PagingType.PAGEABLE
import com.badmanners.murglar.lib.core.model.node.NodeType.ALBUM
import com.badmanners.murglar.lib.core.model.node.NodeType.ARTIST
import com.badmanners.murglar.lib.core.model.node.NodeType.NODE
import com.badmanners.murglar.lib.core.model.node.NodeType.TRACK
import com.badmanners.murglar.lib.core.model.node.Path
import com.badmanners.murglar.lib.core.node.BaseNodeResolver
import com.badmanners.murglar.lib.core.node.Directory
import com.badmanners.murglar.lib.core.node.LikeConfig
import com.badmanners.murglar.lib.core.node.MappedEntity
import com.badmanners.murglar.lib.core.node.Root
import com.badmanners.murglar.lib.core.node.Search
import com.badmanners.murglar.lib.core.node.Track
import com.graf2242.murglar_jellyfin_core.JellyfinMurglar
import com.graf2242.murglar_jellyfin_core.localization.JellyfinMessages
import com.graf2242.murglar_jellyfin_core.model.JellyfinAlbum
import com.graf2242.murglar_jellyfin_core.model.JellyfinArtist
import com.graf2242.murglar_jellyfin_core.model.JellyfinTrack

class JellyfinNodeResolver(
    murglar: JellyfinMurglar,
    messages: JellyfinMessages
) : BaseNodeResolver<JellyfinMurglar, JellyfinMessages>(murglar, messages) {
    override val configurations = listOf(
        Root(
            pattern = "myTracks",
            name = messages::myTracks,
            paging = PAGEABLE,
            hasSubdirectories = false,
            isOwn = true,
            contentNodeType = TRACK,
            nodeContentSupplier = ::getMyTracks
        ),
        Root(
            pattern = "myAlbums",
            name = messages::myAlbums,
            paging = NON_PAGEABLE,
            hasSubdirectories = true,
            isOwn = true,
            contentNodeType = ALBUM,
            nodeContentSupplier = ::getMyAlbums
        ),
        Root(
            pattern = "myArtists",
            name = messages::myArtists,
            paging = NON_PAGEABLE,
            hasSubdirectories = true,
            isOwn = true,
            contentNodeType = ARTIST,
            nodeContentSupplier = ::getMyArtists
        ),

        Search(
            pattern = "searchTracks",
            name = messages::tracksSearch,
            hasSubdirectories = false,
            contentNodeType = TRACK,
            nodeContentSupplier = ::searchTracks
        ),
        Search(
            pattern = "searchAlbums",
            name = messages::albumsSearch,
            hasSubdirectories = true,
            contentNodeType = ALBUM,
            nodeContentSupplier = ::searchAlbums
        ),
        Search(
            pattern = "searchArtists",
            name = messages::artistsSearch,
            hasSubdirectories = true,
            contentNodeType = ARTIST,
            nodeContentSupplier = ::searchArtists
        ),
        Track(
            pattern = "*/track-<trackId>",
            like = LikeConfig(rootNodePath("myTracks"), ::likeTrack),
            events = listOf(
//                EventConfig(TrackStart::class) { to<JellyfinTrack>().handleStartEvent() },
//                EventConfig(TrackEnd::class) { to<JellyfinTrack>().handleEndEvent(it.endTimeMs) }
            ),
            nodeSupplier = ::getTrack
        ),

        MappedEntity(
            pattern = "*/album-<albumId>",
            paging = NON_PAGEABLE,
            hasSubdirectories = false,
            type = ALBUM,
            contentNodeType = TRACK,
            relatedPaths = ::getAlbumRelatedPaths,
            like = LikeConfig(rootNodePath("myAlbums"), ::likeAlbum),
            nodeSupplier = ::getAlbum,
            nodeContentSupplier = ::getAlbumTracks
        ),

        Directory(
            pattern = "*/artist-<artistId>/albums",
            paging = NON_PAGEABLE,
            hasSubdirectories = true,
            contentNodeType = ALBUM,
            nodeContentSupplier = ::getArtistAlbums
        ),
        MappedEntity(
            pattern = "*/artist-<artistId>",
            paging = NON_PAGEABLE,
            hasSubdirectories = true,
            type = ARTIST,
            contentNodeType = NODE,
            relatedPaths = ::getArtistRelatedPaths,
            like = LikeConfig(rootNodePath("myArtists"), ::likeArtist),
            nodeSupplier = ::getArtist,
            nodeContentSupplier = ::getArtistSubdirectories
        )
    )


    @Suppress("UNCHECKED_CAST")
    override suspend fun getTracksByMediaIds(mediaIds: List<String>): List<JellyfinTrack> =
        murglar.getTracksByMediaIds(mediaIds).convertTracks(unmappedPath()) as List<JellyfinTrack>

    private suspend fun getMyTracks(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.getMyTracks(page).convertTracks(parentPath)

    private suspend fun getMyAlbums(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.getMyAlbums().convertAlbums(parentPath)

    private suspend fun getMyArtists(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.getMyArtists().convertArtists(parentPath)

    private suspend fun searchTracks(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.searchTracks(params.getQuery(), page!!).convertTracks(parentPath)

    private suspend fun searchAlbums(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.searchAlbums(params.getQuery(), page!!).convertAlbums(parentPath)

    private suspend fun searchArtists(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.searchArtists(params.getQuery(), page!!).convertArtists(parentPath)

    private suspend fun getAlbumTracks(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.getAlbumTracks(params["albumId"]!!).convertTracks(parentPath)

    private suspend fun getArtistAlbums(parentPath: Path, page: Int?, params: Map<String, String>) =
        murglar.getArtistAlbums(params["artistId"]!!).convertAlbums(parentPath)

    private fun getArtistSubdirectories(parentPath: Path, page: Int?, params: Map<String, String>) = listOf(
        subdirectoryNode("albums", messages.albums, parentPath),
    )

    private suspend fun getArtist(parentPath: Path, params: Map<String, String>) =
        murglar.getArtist(params["artistId"]!!).convertArtist(parentPath)

    private fun getArtistSimilarArtistsSubdirectory(parentPath: Path, params: Map<String, String>) = subdirectoryNode(
        "similarArtists", messages.similarArtists, parentPath.child("artist-${params["artistId"]}")
    )

    private suspend fun getAlbum(parentPath: Path, params: Map<String, String>) =
        murglar.getAlbum(params["albumId"]!!).convertAlbum(parentPath)

    private suspend fun getTrack(parentPath: Path, params: Map<String, String>) =
        murglar.getTrack(params["trackId"]!!, params["albumId"]).convertTrack(parentPath)

    private fun likeTrack(node: Node, like: Boolean) {
        val track = node.to<JellyfinTrack>()
        if (like)
            murglar.addToFavorite(track.id)
        else
            murglar.removeFromFavorite(track.id)
    }

    private fun likeAlbum(node: Node, like: Boolean) {
        val album = node.to<JellyfinAlbum>()
        if (like)
            murglar.addToFavorite(album.id)
        else
            murglar.removeFromFavorite(album.id)
    }

    private fun likeArtist(node: Node, like: Boolean) {
        val artist = node.to<JellyfinArtist>()
        if (like)
            murglar.addToFavorite(artist.id)
        else
            murglar.removeFromFavorite(artist.id)
    }


    private fun JellyfinTrack.handleStartEvent() {
        murglar.reportTrackStart(this)
    }

    private fun JellyfinTrack.handleEndEvent(endTimeMs: Int) {
        murglar.reportTrackEnd(this, endTimeMs)
    }

    private fun getArtistRelatedPaths(node: Node): List<NamedPath> {
        TODO("Not yet implemented")
    }

    private fun getAlbumRelatedPaths(node: Node): List<NamedPath> {
        TODO("Not yet implemented")
    }

}