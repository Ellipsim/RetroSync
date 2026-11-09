package com.retrosync.saves.dto;

import java.time.Instant;
import java.util.UUID;

import com.retrosync.saves.model.Save;

public record SaveResponse(
    UUID id,
    UUID userId,
    String fileName,
    Long fileSize,
    Instant createdAt
) {
    public static SaveResponse from(Save save) {
        return new SaveResponse(
            save.getId(),
            save.getUserId(),
            save.getFileName(),
            save.getFileSize(),
            save.getCreatedAt()
        );
    }
}
