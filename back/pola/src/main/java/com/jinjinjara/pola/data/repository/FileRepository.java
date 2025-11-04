package com.jinjinjara.pola.data.repository;
import com.jinjinjara.pola.data.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}