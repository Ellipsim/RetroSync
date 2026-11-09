
package com.retrosync.saves.repository;

import com.retrosync.saves.model.Save;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaveRepository extends JpaRepository<Save, UUID> {

    List<Save> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
