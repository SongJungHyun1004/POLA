package com.jinjinjara.pola.data.repository;
import com.jinjinjara.pola.data.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
}