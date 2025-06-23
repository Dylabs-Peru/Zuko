package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.playlistExceptions.*;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
import com.dylabs.zuko.mapper.PlaylistMapper;
import com.dylabs.zuko.model.Playlist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.PlaylistRepository;
import com.dylabs.zuko.repository.SongRepository;
import com.dylabs.zuko.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaylistServiceUnitTest {

    @InjectMocks
    private PlaylistService playlistService;

    @Mock private PlaylistRepository playlistRepository;
    @Mock private SongRepository songRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlaylistMapper playlistMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. createPlaylist
    @Test
    void createPlaylist_Success() {
        String userId = "1";
        PlaylistRequest request = new PlaylistRequest("Test", "desc", true, "si");
        User user = new User(); user.setId(1L);
        Playlist playlist = new Playlist(); playlist.setUser(user);
        Playlist saved = new Playlist(); saved.setUser(user);
        PlaylistResponse response = new PlaylistResponse(1L, "Test", "desc", true, null, Set.of(), "si");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistRepository.existsByNameIgnoreCaseAndUser_id("Test", 1L)).thenReturn(false);
        when(playlistMapper.toEntity(request)).thenReturn(playlist);
        when(playlistRepository.save(playlist)).thenReturn(saved);
        when(playlistMapper.toResponse(saved)).thenReturn(response);

        PlaylistResponse result = playlistService.createPlaylist(userId, request);
        assertEquals("Test", result.name());
    }

    @Test
    void createPlaylist_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundExeption.class, () -> playlistService.createPlaylist("1", new PlaylistRequest("Test", "desc", true, "si")));
    }

    @Test
    void createPlaylist_AlreadyExists() {
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistRepository.existsByNameIgnoreCaseAndUser_id("Test", 1L)).thenReturn(true);
        assertThrows(PlaylistAlreadyExistsException.class, () -> playlistService.createPlaylist("1", new PlaylistRequest("Test", "desc", true, "si")));
    }

    // 2. getPlaylistById
    @Test
    void getPlaylistById_Owner_Success() {
        String userId = "1";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User user = new User(); user.setId(1L);
        playlist.setUser(user);
        PlaylistResponse response = mock(PlaylistResponse.class);

        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistMapper.toResponse(playlist)).thenReturn(response);

        PlaylistResponse result = playlistService.getPlaylistById(userId, 10L);
        assertEquals(response, result);
    }

    @Test
    void getPlaylistById_NotOwnerNotPublic() {
        String userId = "2";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User owner = new User(); owner.setId(1L);
        playlist.setUser(owner); playlist.setPublic(false);

        User user = new User(); user.setId(2L);

        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(PlaylistNotPublicException.class, () -> playlistService.getPlaylistById(userId, 10L));
    }

    // 3. deletePlaylist
    @Test
    void deletePlaylist_Owner_Success() {
        String userId = "1";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User user = new User(); user.setId(1L); user.setUserRoleName("USER");
        playlist.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));

        assertDoesNotThrow(() -> playlistService.deletePlaylist(userId, 10L));
        verify(playlistRepository).delete(playlist);
    }

    @Test
    void deletePlaylist_NotOwnerNotAdmin() {
        String userId = "2";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User owner = new User(); owner.setId(1L); owner.setUserRoleName("USER");
        playlist.setUser(owner);
        User user = new User(); user.setId(2L); user.setUserRoleName("USER");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));

        assertThrows(PlaylistAccessDeniedException.class, () -> playlistService.deletePlaylist(userId, 10L));
    }

    @Test
    void deletePlaylist_AdminCanDeleteOthersPlaylist() {
        String userId = "2"; // admin, no owner
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User owner = new User(); owner.setId(1L); owner.setUserRoleName("USER");
        playlist.setUser(owner);

        User admin = new User(); admin.setId(2L); admin.setUserRoleName("ADMIN");

        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));

        assertDoesNotThrow(() -> playlistService.deletePlaylist(userId, 10L));
        verify(playlistRepository).delete(playlist);
    }


    // 4. listSongsInPlaylist
    @Test
    void listSongsInPlaylist_Owner_Success() {
        String userId = "1";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User user = new User(); user.setId(1L);
        playlist.setUser(user);
        playlist.setSongs(Set.of(new Song()));
        SongResponse songResponse = mock(SongResponse.class);

        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistMapper.toSongResponse(any(Song.class))).thenReturn(songResponse);

        List<SongResponse> result = playlistService.listSongsInPlaylist(userId, 10L);
        assertFalse(result.isEmpty());
    }

    @Test
    void listSongsInPlaylist_NotOwnerAndPrivate_ThrowsException() {
        String userId = "2"; // usuario que NO es el dueño
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User owner = new User(); owner.setId(1L);
        playlist.setUser(owner);
        playlist.setPublic(false); // La playlist es privada

        User notOwner = new User(); notOwner.setId(2L);

        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(2L)).thenReturn(Optional.of(notOwner));

        assertThrows(PlaylistNotPublicException.class, () ->
                playlistService.listSongsInPlaylist(userId, 10L));
    }

    // 5. addSongToPlaylist
    @Test
    void addSongToPlaylist_Success() {
        String userId = "1";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User user = new User(); user.setId(1L); user.setUserRoleName("USER");
        playlist.setUser(user);
        Song song = new Song(); song.setId(5L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(5L)).thenReturn(Optional.of(song));

        assertDoesNotThrow(() -> playlistService.addSongToPlaylist(userId, 10L, 5L));
        assertTrue(playlist.getSongs().contains(song));
    }

    @Test
    void addSongToPlaylist_NoPerms() {
        String userId = "2";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User owner = new User(); owner.setId(1L); owner.setUserRoleName("USER");
        playlist.setUser(owner);
        User user = new User(); user.setId(2L); user.setUserRoleName("USER");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));

        assertThrows(PlaylistAccessDeniedException.class, () -> playlistService.addSongToPlaylist(userId, 10L, 1L));
    }

    // 6. removeSongFromPlaylist
    @Test
    void removeSongFromPlaylist_Success() {
        String userId = "1";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User user = new User(); user.setId(1L); user.setUserRoleName("USER");
        playlist.setUser(user);
        Song song = new Song(); song.setId(5L);
        playlist.setSongs(new HashSet<>(List.of(song)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(5L)).thenReturn(Optional.of(song));

        assertDoesNotThrow(() -> playlistService.removeSongFromPlaylist(userId, 10L, 5L));
        assertFalse(playlist.getSongs().contains(song));
    }

    @Test
    void removeSongFromPlaylist_SongNotInPlaylist() {
        String userId = "1";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User user = new User(); user.setId(1L); user.setUserRoleName("USER");
        playlist.setUser(user);
        Song song = new Song(); song.setId(5L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(5L)).thenReturn(Optional.of(song));

        assertThrows(SongNotInPlaylistException.class, () -> playlistService.removeSongFromPlaylist(userId, 10L, 5L));
    }

    @Test
    void removeSongFromPlaylist_NoPerms_ThrowsException() {
        String userId = "2";
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User owner = new User(); owner.setId(1L); owner.setUserRoleName("USER");
        playlist.setUser(owner);

        // El usuario autenticado NO es owner ni admin
        User notOwner = new User(); notOwner.setId(2L); notOwner.setUserRoleName("USER");

        Song song = new Song(); song.setId(5L);
        playlist.setSongs(new HashSet<>(List.of(song)));

        when(userRepository.findById(2L)).thenReturn(Optional.of(notOwner));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(5L)).thenReturn(Optional.of(song));

        assertThrows(PlaylistAccessDeniedException.class, () ->
                playlistService.removeSongFromPlaylist(userId, 10L, 5L));
    }

    @Test
    void removeSongFromPlaylist_AdminCanRemoveFromOthersPlaylist() {
        String userId = "2"; // admin, no owner
        Playlist playlist = new Playlist(); playlist.setPlaylistId(10L);
        User owner = new User(); owner.setId(1L);
        playlist.setUser(owner);

        Song song = new Song(); song.setId(5L);
        playlist.setSongs(new HashSet<>(List.of(song)));

        User admin = new User(); admin.setId(2L); admin.setUserRoleName("ADMIN");

        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(playlistRepository.findById(10L)).thenReturn(Optional.of(playlist));
        when(songRepository.findById(5L)).thenReturn(Optional.of(song));

        assertDoesNotThrow(() -> playlistService.removeSongFromPlaylist(userId, 10L, 5L));
        assertFalse(playlist.getSongs().contains(song));
    }




}
