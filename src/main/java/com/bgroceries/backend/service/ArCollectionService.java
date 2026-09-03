package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ArCollectionDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ArCollectionService {

    List<ArCollectionDto> getAllCollections(
            String search,
            String searchBy,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    ArCollectionDto getCollectionById(Long id);

    ArCollectionDto createCollection(ArCollectionDto dto);

    ArCollectionDto updateCollection(Long id, ArCollectionDto dto);

    ArCollectionDto updateStatus(Long id, String status);

    void deleteCollection(Long id);

    String generateNextCode();
}
