package com.reservation.service;

import com.reservation.dto.request.RoomCreateRequest;
import com.reservation.dto.response.RoomResponse;
import com.reservation.model.entity.Room;
import com.reservation.repository.RoomRepository;
import com.reservation.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;

    public RoomResponse createRoom(RoomCreateRequest request) {
        Room room = Room.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .build();

        Room savedRoom = roomRepository.save(room);
        return mapToResponse(savedRoom);
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<RoomResponse> getRoomById(Long id) {
        return roomRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<RoomResponse> getRoomsByMinCapacity(int capacity) {
        return roomRepository.findByCapacityGreaterThanEqual(capacity)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
        
        try {
            // Check if room has any events associated with it
            boolean hasEvents = eventRepository.existsByRoomId(roomId);
            if (hasEvents) {
                throw new RuntimeException("Cannot delete room. It has events associated with it. Please delete or reassign the events first.");
            }
            
            roomRepository.delete(room);
        } catch (Exception e) {
            if (e.getMessage().contains("Cannot delete room")) {
                throw e; // Re-throw our custom message
            }
            throw new RuntimeException("Failed to delete room: " + e.getMessage(), e);
        }
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .build();
    }
}
