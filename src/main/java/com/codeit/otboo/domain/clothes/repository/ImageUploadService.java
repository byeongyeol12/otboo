package com.codeit.otboo.domain.clothes.repository;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {

	String upload(MultipartFile image);
}
