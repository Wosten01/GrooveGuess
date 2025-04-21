package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.model.Track
import org.springframework.web.bind.annotation.*
import com.grooveguess.backend.service.TrackService



@RestController
@RequestMapping("/api/tracks")
class TrackController(private val trackService: TrackService) {

    @GetMapping
    fun getAll(): List<Track> = trackService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Track = trackService.find(id)

    @PostMapping
    fun create(@RequestBody track: Track, @RequestParam creatorId: Long): Track =
        trackService.create(track, creatorId)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody track: Track, @RequestParam userId: Long): Track? =
        trackService.update(id, track, userId)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @RequestParam userId: Long) {
        trackService.delete(id, userId)
    }
}