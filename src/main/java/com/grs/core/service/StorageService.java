package com.grs.core.service;

import com.grs.api.model.request.FileDTO;
import com.grs.api.model.response.file.*;
import com.grs.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * Created by Acer on 10/2/2017.
 */
@Slf4j
@Service
public class StorageService {

    @Value("${upload.file.directory}")
    private String uploadDirectory;

    @Value("${max.allowed.upload.size:10}")
    private long maxAllowedUploadSize;

    private final Path rootLocation = Paths.get(uploadDirectory + File.separator + Constant.storedFilesFolderName);
    private MessageDigest messageDigest;


    public void init() {
        try {
            Files.createDirectory(rootLocation);
            this.messageDigest = MessageDigest.getInstance("MD5");
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        } catch (Exception e) {
            throw new RuntimeException("MD5 Hashing Fail/could not initialize storage");
        }
    }


    public Path getRootLocation() {
        return Paths.get(uploadDirectory + File.separator + Constant.storedFilesFolderName);
    }

    public Path getTempLocation() {
        return Paths.get(uploadDirectory + File.separator + Constant.tempFilesDirectoryName);
    }

    private String getMD5String(String id) {
        if (this.messageDigest == null) {
            try {
                this.messageDigest = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                throw new RuntimeException("MD5 Hash creation failed");
            }
        }
        this.messageDigest.update(id.getBytes(), 0, id.length());
        return new BigInteger(1, messageDigest.digest()).toString(32);
    }

    public FileContainerDTO storeFile(Principal principal, MultipartFile[] files) {
        String userFolderName;
        if (principal != null) {
            userFolderName = this.getMD5String(principal.getName());
        } else {
            userFolderName = this.getMD5String("some things i guess");
        }
        Path uploadLocation = Paths.get(this.getRootLocation().toString() + File.separator + userFolderName);
        File directory = new File(uploadLocation.toString());
        if (!directory.exists()) {
            try {
                directory.mkdirs();
            } catch (Exception e) {
                throw new RuntimeException("Making Directory Failed");
            }
        }
        List<FileBaseDTO> fileDTOList = new ArrayList<FileBaseDTO>();
        for (MultipartFile file : files) {
            try {
                DateFormat df = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                String extension = getExtension(file.getOriginalFilename());
                String fileName = df.format(new Date()) + "_" + Constant.fileNameSuffix + "." + extension;
                Files.copy(file.getInputStream(), uploadLocation.resolve(fileName));

                String fileLocation = Paths.get(this.getRootLocation().toString() + File.separator + userFolderName) + File.separator + fileName;
                String thumbnailUrl = getThumbnailLinkImageForFile(fileLocation);
                String previewerCode = this.getPreviewerCode(file.getName(), uploadLocation.resolve(fileName).toString());

                fileDTOList.add(FileDerivedDTO.builder()
                        .name(file.getOriginalFilename())
                        .size(String.valueOf(file.getSize()))
                        .url("/api/file/upload/" + userFolderName + "/" + (fileName + "/"))
                        .thumbnailUrl(thumbnailUrl)
                        .deleteUrl("/api/file/upload/" + userFolderName + "/" + (fileName + "/"))
                        .deleteType("DELETE")
                        .previewerCode(previewerCode)
                        .build());

            } catch (Exception e) {
                e.printStackTrace();
                fileDTOList.add(FileDerivedWithErrorDTO.builder()
                        .name(file.getOriginalFilename())
                        .size(String.valueOf(file.getSize()))
                        .error("Could Not Upload, Sorry")
                        .build());
            }
        }
        return FileContainerDTO.builder()
                .files(fileDTOList)
                .build();

    }

    public FileContainerDTO storeFileNew(Principal principal, MultipartFile[] files) {
        String userFolderName;
        if (principal != null) {
            userFolderName = this.getMD5String(principal.getName());
        } else {
            userFolderName = this.getMD5String("some things i guess");
        }
        Path uploadLocation = Paths.get(this.getTempLocation() + File.separator + userFolderName);
        File directory = new File(uploadLocation.toString());
        if (!directory.exists()) {
            try {
                directory.mkdirs();
            } catch (Exception e) {
                throw new RuntimeException("Making Directory Failed");
            }
        }
        List<FileBaseDTO> fileDTOList = new ArrayList<FileBaseDTO>();
        for (MultipartFile file : files) {
            try {
                DateFormat df = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                String extension = getExtension(file.getOriginalFilename());
                String fileName = df.format(new Date()) + "_" + Constant.fileNameSuffix + "." + extension;
                Files.copy(file.getInputStream(), uploadLocation.resolve(fileName));

                String fileLocation = Paths.get(this.getTempLocation().toString() + File.separator + userFolderName) + File.separator + fileName;
                String thumbnailUrl = getThumbnailLinkImageForFile(fileLocation);
                String previewerCode = this.getPreviewerCode(file.getName(), uploadLocation.resolve(fileName).toString());

                fileDTOList.add(FileDerivedDTO.builder()
                        .name(file.getOriginalFilename())
                        .size(String.valueOf(file.getSize()))
                        .url("/api/file/upload/" + userFolderName + "/" + (fileName + "/"))
                        .thumbnailUrl(thumbnailUrl)
                        .deleteUrl("/api/file/upload/" + userFolderName + "/" + (fileName + "/"))
                        .deleteType("DELETE")
                        .previewerCode(previewerCode)
                        .build());

            } catch (Exception e) {
                e.printStackTrace();
                fileDTOList.add(FileDerivedWithErrorDTO.builder()
                        .name(file.getOriginalFilename())
                        .size(String.valueOf(file.getSize()))
                        .error("Could Not Upload, Sorry")
                        .build());
            }
        }
        return FileContainerDTO.builder()
                .files(fileDTOList)
                .build();

    }

    public DerivedFileContainerDTO storeDerivedFile(Principal principal, MultipartFile[] files) {
        String userFolderName;
        if (principal != null) {
            userFolderName = this.getMD5String(principal.getName());
        } else {
            userFolderName = this.getMD5String("some things i guess");
        }
        Path uploadLocation = Paths.get(this.getRootLocation().toString() + File.separator + userFolderName);
        File directory = new File(uploadLocation.toString());
        if (!directory.exists()) {
            try {
                directory.mkdirs();
            } catch (Exception e) {
                throw new RuntimeException("Making Directory Failed");
            }
        }
        List<FileDerivedDTO> fileDTOList = new ArrayList<FileDerivedDTO>();
        for (MultipartFile file : files) {
            try {
                DateFormat df = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                String extension = getExtension(file.getOriginalFilename());
                String fileName = df.format(new Date()) + "_" + Constant.fileNameSuffix + "." + extension;
                Files.copy(file.getInputStream(), uploadLocation.resolve(fileName));

                String fileLocation = Paths.get(this.getRootLocation().toString() + File.separator + userFolderName).toString() + File.separator + fileName;
                String thumbnailUrl = getThumbnailLinkImageForFile(fileLocation);
                String previewerCode = this.getPreviewerCode(file.getName(), uploadLocation.resolve(fileName).toString());

                fileDTOList.add(FileDerivedDTO.builder()
                        .name(file.getOriginalFilename())
                        .size(String.valueOf(file.getSize()))
                        .url("/api/file/upload/" + userFolderName + "/" + (fileName + "/"))
                        .thumbnailUrl(thumbnailUrl)
                        .deleteUrl("/api/file/upload/" + userFolderName + "/" + (fileName + "/"))
                        .deleteType("DELETE")
                        .previewerCode(previewerCode)
                        .build());

            } catch (Exception e) {
                e.printStackTrace();
                fileDTOList.add(FileActualDerivedWithErrorDTO.builder()
                        .name(file.getOriginalFilename())
                        .size(String.valueOf(file.getSize()))
                        .error("Could Not Upload, Sorry")
                        .build());
            }
        }
        return DerivedFileContainerDTO.builder()
                .files(fileDTOList)
                .build();

    }

    public Object deleteFile(Principal principal, String folderName, String fileName) {
        String userFolderName;
        WeakHashMap<String, Boolean> result = new WeakHashMap<String, Boolean>();
        if (principal != null) {
            userFolderName = this.getMD5String(principal.getName());
        } else {
            userFolderName = this.getMD5String("some things i guess");
        }
        if (folderName.compareTo(userFolderName) != 0) {
            result.put(fileName, false);
        }
        Path uploadLocation = Paths.get(this.getRootLocation().toString() + File.separator + folderName);
        Path uploadedTemp = Paths.get(this.getTempLocation().toString() + File.separator + folderName);
        try {
            Files.delete(uploadLocation.resolve(fileName));
            try {
                Files.delete(uploadedTemp.resolve(fileName));
            } catch (Throwable t) {

            }
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1);
            }
            if (extension.contains("jpg") || extension.contains("png") ||
                    extension.contains("jpeg") || extension.contains("bmp") || extension.contains("gif")) {

                Files.delete(uploadLocation.resolve(fileName.replace(extension, "thumbnail." + extension)));
                try {
                    Files.delete(uploadedTemp.resolve(fileName.replace(extension, "thumbnail." + extension)));
                } catch (Throwable t) {

                }
            }
            result.put(fileName, true);
        } catch (Exception e) {
            result.put(fileName, false);
        }
        WeakHashMap<String, Object> returnObject = new WeakHashMap<String, Object>();
        returnObject.put("files", result);
        return returnObject;
    }

    public ResponseEntity<InputStreamResource> getFile(Principal principal, String folderName, String fileName) {
        String userFolderName;
        WeakHashMap<String, Boolean> result = new WeakHashMap<String, Boolean>();
        if (principal != null) {
            userFolderName = this.getMD5String(principal.getName());
        } else {
            userFolderName = this.getMD5String("some things i guess");
        }

        String path = Paths.get(this.getRootLocation().toString() + File.separator + folderName) + File.separator + fileName;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (Exception e) {
            //e.printStackTrace();
            try {
                inputStream = new FileInputStream(Paths.get(this.getTempLocation() + File.separator + folderName) + File.separator + fileName);
            } catch (Throwable t) {

            }
        }
        MediaType mediaType = this.getMediaTypeFromFileType(path);
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(mediaType)
                .body(new InputStreamResource(inputStream));
    }

    public MediaType getMediaTypeFromFileType(String path) {
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i + 1);
        }
        MediaType mediaType = null;
        if (extension.compareToIgnoreCase("jpg") == 0 || extension.compareToIgnoreCase("jpeg") == 0) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (extension.compareToIgnoreCase("png") == 0) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (extension.compareToIgnoreCase("gif") == 0) {
            mediaType = MediaType.IMAGE_GIF;
        } else if (extension.compareToIgnoreCase("pdf") == 0) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if (extension.compareToIgnoreCase("txt") == 0) {
            mediaType = MediaType.TEXT_PLAIN;
        } else if (extension.compareToIgnoreCase("doc") == 0) {
            mediaType = new MediaType("application", "msword");
        } else if (extension.compareToIgnoreCase("docx") == 0) {
            mediaType = new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if (extension.compareToIgnoreCase("xls") == 0) {
            mediaType = new MediaType("application", "vnd.ms-excel");
        } else if (extension.compareToIgnoreCase("xlsx") == 0) {
            mediaType = new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if (extension.compareToIgnoreCase("ppt") == 0) {
            mediaType = new MediaType("application", "vnd.ms-powerpoint");
        } else if (extension.compareToIgnoreCase("pptx") == 0) {
            mediaType = new MediaType("application", "vnd.openxmlformats-officedocument.presentationml.presentation");
        } else if (extension.compareToIgnoreCase("mp3") == 0) {
            mediaType = new MediaType("audio", "mpeg");
        } else if (extension.compareToIgnoreCase("mp4") == 0) {
            mediaType = new MediaType("video", "mp4");
        } else if (extension.compareToIgnoreCase("mkv") == 0) {
            mediaType = new MediaType("video", "x-matroska");
        } else if (extension.compareToIgnoreCase("avi") == 0) {
            mediaType = new MediaType("video", "avi");
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return mediaType;
    }

    public String getThumbnailLinkImageForFile(String filePath) {
        String extension = "";
        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1);
        }
        if (extension.toLowerCase().contains("jpg") || extension.toLowerCase().contains("png") ||
                extension.toLowerCase().contains("jpeg") || extension.toLowerCase().contains("bmp") || extension.toLowerCase().contains("gif")) {
            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                ImageIO.scanForPlugins();
                BufferedImage originalImage = ImageIO.read(inputStream);
                int width = 80;
                int height = (int) ((originalImage.getHeight() * 80.0 / originalImage.getWidth()));
                Image newImage = originalImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
                File outputfile = new File(filePath.replace(extension, "thumbnail." + extension));

                BufferedImage bi = new BufferedImage(newImage.getWidth(null), newImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                bi.getGraphics().drawImage(newImage, 0, 0, null);

                ImageIO.write(bi, "png", outputfile);

                String pattern = Pattern.quote(System.getProperty("file.separator"));
                String[] filePathArray = outputfile.getCanonicalPath().split(pattern);
                inputStream.close();

                return "/api/file/upload/" + filePathArray[filePathArray.length - 2] + "/" + filePathArray[filePathArray.length - 1] + "/";

            } catch (Exception e) {
                return "assets/layouts/layout/img/text-file-3-xxl.png";
            }

        } else if (extension.toLowerCase().equals("pdf") || extension.toLowerCase().equals("doc") || extension.toLowerCase().equals("docx") || extension.toLowerCase().equals("xls") || extension.toLowerCase().equals("xlsx")
                || extension.toLowerCase().equals("ppt") || extension.toLowerCase().equals("pptx") || extension.toLowerCase().equals("txt") || extension.toLowerCase().equals("mkv") || extension.toLowerCase().equals("avi")
                || extension.toLowerCase().equals("mp3") || extension.toLowerCase().equals("mp4") || extension.toLowerCase().equals("xls") || extension.toLowerCase().equals("xlsx")) {
            return "assets/layouts/layout/img/" + extension + ".png";
        } else {
            return "assets/layouts/layout/img/text-file-3-xxl.png";
        }
    }

    public String getPreviewerCode(String fileName, String path) {
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i + 1);
        }

        String previewerCode = "";

        if (extension.compareToIgnoreCase("jpg") == 0 || extension.compareToIgnoreCase("jpeg") == 0 || extension.compareToIgnoreCase("png") == 0 || extension.compareToIgnoreCase("gif") == 0) {
            previewerCode = " data-lightbox=prv_" + fileName;
        } else if (extension.compareToIgnoreCase("pdf") == 0 || extension.compareToIgnoreCase("txt") == 0 || extension.compareToIgnoreCase("doc") == 0 || extension.compareToIgnoreCase("docx") == 0 || extension.compareToIgnoreCase("xls") == 0 || extension.compareToIgnoreCase("xlsx") == 0 || extension.compareToIgnoreCase("ppt") == 0 || extension.compareToIgnoreCase("pptx") == 0) {
            previewerCode = " class=media-embed ";
        } else if (extension.compareToIgnoreCase("mp3") == 0 || extension.compareToIgnoreCase("wav") == 0) {
            previewerCode = " class=media-audio ";
        } else if (extension.compareToIgnoreCase("mp4") == 0 || extension.compareToIgnoreCase("avi") == 0 || extension.compareToIgnoreCase("mkv") == 0) {
            previewerCode = " class=media-video ";
        } else {
            previewerCode = "";
        }

        return previewerCode;
    }

    public String getExtension(String path) {
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i + 1);
        }
        return extension;
    }

    public boolean checkFileSize(List<FileDTO> files) {

        long size = 0;
        for (FileDTO fileDTO : files) {
            String[] filePathParts = fileDTO.getUrl().split("/");
            String fileName = filePathParts[filePathParts.length - 1];
            String folderName = filePathParts[filePathParts.length - 2];
            String tempFile = this.getTempLocation() + File.separator + folderName + File.separator + fileName;
            File temp = new File(tempFile);
            if (temp.exists()) {
                size += temp.length();
            }
        }

        return size <= 0 || size / (1024 * 1024) <= maxAllowedUploadSize;
    }
}
