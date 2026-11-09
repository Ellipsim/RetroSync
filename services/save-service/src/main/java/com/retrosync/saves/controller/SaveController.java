
package com.retrosync.saves.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.retrosync.saves.dto.SaveResponse;
import com.retrosync.saves.model.Save;
import com.retrosync.saves.service.SaveService;

@RestController
@RequestMapping("/api/saves")
public class SaveController {

    private final SaveService saveService;

    public SaveController(SaveService saveService) {
        this.saveService = saveService;
    }

    @PostMapping(
        value = "/{userId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<SaveResponse> upload(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file) {

        try {
            Save save = saveService.upload(userId, file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SaveResponse.from(save));

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar el archivo"
            );
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<SaveResponse>> list(
            @PathVariable UUID userId) {

        List<SaveResponse> saves = saveService.findByUser(userId)
                .stream()
                .map(SaveResponse::from)
                .toList();

        return ResponseEntity.ok(saves);
    }

    @GetMapping("/{userId}/{saveId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable UUID userId,
            @PathVariable UUID saveId) {

        try {
            Path filePath = saveService.getFilePath(saveId, userId);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                            resource.getFilename() + "\""
                    )
                    .body(resource);

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage()
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo descargar el archivo"
            );
        }
    }

    @DeleteMapping("/{userId}/{saveId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId,
            @PathVariable UUID saveId) {

        try {
            saveService.delete(saveId, userId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage()
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo eliminar el archivo"
            );
        }
    }
}

