package com.dylabs.zuko.service;


import com.dylabs.zuko.model.Playlist;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService {

    private final List<Playlist> playlists = new ArrayList<>();
    private long nextId = 1L;

    public PlaylistService() {

    }
}
