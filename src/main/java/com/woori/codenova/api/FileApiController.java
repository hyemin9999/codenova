package com.woori.codenova.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.woori.codenova.entity.UploadFile;
import com.woori.codenova.repository.UploadFileRepository;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tui-editor")
@RequiredArgsConstructor
public class FileApiController {

	@Value("${upload.path}")
	private String uploadDir;
	private final String uploadDirImages = "images";

	private final UploadFileRepository uploadFileRepository;

	/**
	 * 에디터 이미지 업로드
	 * 
	 * @param image 파일 객체
	 * @retrun 업로드된 파일명
	 */
	@PostMapping("/image-upload")
	public UploadFile uploadEditorImage(Model model, @RequestParam(value = "image") final MultipartFile image,
			@RequestParam(value = "type", defaultValue = "") final String type,
			@RequestParam(value = "mode", defaultValue = "") final String mode) {

		if (image.isEmpty()) {
			return null;
		}

		log.info("image :: " + image);

		String orgFilename = image.getOriginalFilename();
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		String extension = orgFilename.substring(orgFilename.lastIndexOf(".") + 1);
		String saveFilename = uuid + "." + extension;

		String absolutePath = new File("").getAbsolutePath() + "\\";
		String path = uploadDir + uploadDirImages;
		File dir = new File(path);
		if (dir.exists() == false) {
			if (dir.mkdirs()) {
				System.out.println("디렉토리 생성 성공: " + dir.getAbsolutePath());
			} else {
				System.out.println("디렉토리 생성 실패 또는 이미 존재");
			}
		}

		try {
			File uploadFile = new File(absolutePath + "/" + path + "/" + saveFilename);
			image.transferTo(uploadFile);

			UploadFile item = new UploadFile();

			if (mode == "mofidy") { // 수정일때
				if (type == "board") {// 게시글일때
					item.setBoard(null);
				} else { // 공지사항일때
					item.setNotice(null);
				}
			} else { // 등록일때
				item.setBoard(null);
				item.setNotice(null);
			}

			item.setOriginalFilename(orgFilename);
			item.setSaveFilename(saveFilename);
			item.setExtension(extension);
			item.setFileSize(image.getSize()); // TODO :: 이미지 크기
			item.setSaveFilepath(path);
			item.setUploadDate(LocalDateTime.now());

			return uploadFileRepository.save(item);

		} catch (IOException e) {
			// 예외 처리는 따로 해주는 게 좋습니다.
			throw new RuntimeException(e);
		}
	}

	/**
	 * 디스크에 업로드된 파일을 byte[]로 반환
	 * 
	 * @param filename 디스크에 업로드된 파일명
	 * @return image byte array
	 */
	@GetMapping(value = "/image-print", produces = { MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE,
			MediaType.IMAGE_PNG_VALUE })
	public byte[] printEditorImage(@RequestParam(value = "filename") final String filename) {

		String absolutePath = new File("").getAbsolutePath() + "\\";
		String path = uploadDir + uploadDirImages;

		// 파일이 없는 경우 예외 throw
		File uploadedFile = new File(absolutePath + "/" + path + "/" + filename);
		if (uploadedFile.exists() == false) {
			throw new RuntimeException();
		}

		try {
			// 이미지 파일을 byte[]로 변환 후 반환
			byte[] imageBytes = Files.readAllBytes(uploadedFile.toPath());
			return imageBytes;
		} catch (IOException e) {
			// 예외 처리는 따로 해주는 게 좋습니다.
			throw new RuntimeException(e);
		}
	}

}
