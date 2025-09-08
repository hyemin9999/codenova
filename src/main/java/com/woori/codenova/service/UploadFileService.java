package com.woori.codenova.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Notice;
import com.woori.codenova.entity.UploadFile;
import com.woori.codenova.repository.BoardRepository;
import com.woori.codenova.repository.NoticeRepository;
import com.woori.codenova.repository.UploadFileRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UploadFileService {

	@Value("${upload.path}")
	private String uploadDir;
	private final String uploadDirImages = "images";

	private final UploadFileRepository uploadFileRepository;
	private final BoardRepository boardRepository;
	private final NoticeRepository noticeRepository;

	public UploadFile create(MultipartFile image, String type, String mode, String id) {

		if (image.isEmpty()) {
			return new UploadFile();
		}

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
			if ("modify".equals(mode)) { // 수정일때
				if ("board".equals(type)) {// 게시글일때
					item.setBoard(boardRepository.findById(Integer.parseInt(id)).orElse(null));
				} else { // 공지사항일때
					item.setNotice(noticeRepository.findById(Integer.parseInt(id)).orElse(null));
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
			return new UploadFile();
		}
	}

	public List<UploadFile> getListByBoardList(List<Board> blist) {
		return null;
	}

	public List<UploadFile> getListByNoticeList(List<Notice> nlist) {
		return null;
	}

	public void deleteList(List<UploadFile> list) {
		String absolutePath = new File("").getAbsolutePath() + "\\";
		String path = uploadDir + uploadDirImages;

		if (list != null) {
			for (UploadFile item : list) {

				File delete = new File(absolutePath + "/" + path + "/" + item.getSaveFilename());
				if (delete.exists()) {
					delete.delete();
				}
			}
		}
	}

	public byte[] print(String filename) {
		String absolutePath = new File("").getAbsolutePath() + "\\";
		String path = uploadDir + uploadDirImages;

		// 파일이 없는 경우 예외 throw
		File uploadedFile = new File(absolutePath + "/" + path + "/" + filename);
		if (uploadedFile.exists() == false) {
			// throw new RuntimeException();
			return null;
		}

		try {
			// 이미지 파일을 byte[]로 변환 후 반환
			byte[] imageBytes = Files.readAllBytes(uploadedFile.toPath());
			return imageBytes;
		} catch (IOException e) {
			// 예외 처리는 따로 해주는 게 좋습니다.
			return null;
		}
	}

}
