package com.woori.codenova.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.UploadFile;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {

}
