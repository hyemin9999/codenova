package com.woori.codenova.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.woori.codenova.entity.UploadFile;
import com.woori.codenova.service.UploadFileService;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tui-editor")
@RequiredArgsConstructor
public class FileApiController {

	private final UploadFileService uploadFileService;

	/**
	 * 에디터 이미지 업로드
	 * 
	 * @param image 파일 객체
	 * @retrun 업로드된 파일명
	 */
	@PostMapping("/image-upload")
	public UploadFile uploadEditorImage(Model model, @RequestParam(value = "image") final MultipartFile image,
			@RequestParam(value = "type", defaultValue = "") final String type,
			@RequestParam(value = "mode", defaultValue = "") final String mode,
			@RequestParam(value = "id", defaultValue = "0") final String id) {

		return uploadFileService.create(image, type, mode, id);
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

		return uploadFileService.print(filename);
	}
}
