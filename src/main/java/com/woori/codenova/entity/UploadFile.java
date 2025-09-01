package com.woori.codenova.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UploadFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String originalFilename;

	@Column(nullable = false)
	private String saveFilename;

	@Column(nullable = false)
	private String saveFilepath;

	@Column(nullable = false)
	private String extension;

	@Column(nullable = false)
	private Long fileSize;

	@Column(nullable = false)
	private LocalDateTime uploadDate;

	@ManyToOne
	@JoinColumn(name = "boardId")
	private Board board;

	@ManyToOne
	@JoinColumn(name = "noticeId")
	private Notice notice;

}
