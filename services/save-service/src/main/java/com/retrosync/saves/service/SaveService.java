
package com.retrosync.saves.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.retrosync.saves.model.Save;
import com.retrosync.saves.repository.SaveRepository;

@Service
public class SaveService {

    private final SaveRepository saveRepository;
    private final Path storageLocation;

    public SaveService(
            SaveRepository saveRepository,
            @Value("${retrosync.storage.location}") String storageLocation
    ) throws IOException {

        this.saveRepository = saveRepository;
        this.storageLocation = Paths.get(storageLocation)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(this.storageLocation);
    }

    @Transactional
    public Save upload(UUID userId, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String originalFileName = Paths.get(
                file.getOriginalFilename() == null
                        ? "save"
                        : file.getOriginalFilename()
        ).getFileName().toString();

        String storedFileName = UUID.randomUUID() + ".sav";

        Path destination = storageLocation.resolve(storedFileName);

        Files.copy(file.getInputStream(), destination);

        try {
            Save save = new Save(
                    userId,
                    originalFileName,
                    storedFileName,
                    file.getSize()
            );

            return saveRepository.save(save);

        } catch (RuntimeException exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }
    }

    public List<Save> findByUser(UUID userId) {
        return saveRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Path getFilePath(UUID saveId, UUID userId) {
        Save save = findOwnedSave(saveId, userId);

        Path file = storageLocation.resolve(save.getFilePath())
                .normalize();

        if (!file.startsWith(storageLocation)) {
            throw new IllegalStateException("Ruta de archivo no válida");
        }

        if (!Files.exists(file)) {
            throw new IllegalArgumentException("El archivo no existe");
        }

        return file;
    }

    @Transactional
    public void delete(UUID saveId, UUID userId) throws IOException {
        Save save = findOwnedSave(saveId, userId);

        Path file = storageLocation.resolve(save.getFilePath())
                .normalize();

        if (!file.startsWith(storageLocation)) {
            throw new IllegalStateException("Ruta de archivo no válida");
        }

        Files.deleteIfExists(file);
        saveRepository.delete(save);
    }

    private Save findOwnedSave(UUID saveId, UUID userId) {
        Save save = saveRepository.findById(saveId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Partida no encontrada")
                );

        if (!save.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Partida no encontrada");
        }

        return save;
    }
}

