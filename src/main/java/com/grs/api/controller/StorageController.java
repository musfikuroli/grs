package com.grs.api.controller;

import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.core.model.EmptyJsonResponse;
import com.grs.core.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Created by Acer on 10/2/2017.
 */

@Slf4j
@RestController
public class StorageController {

    @Autowired
    private StorageService storageService;

    @RequestMapping(value = "/api/file/upload", method = RequestMethod.POST)
    public FileContainerDTO addFile(Principal principal, @RequestParam("files[]") MultipartFile[] files, HttpServletRequest request) throws Exception{
        request.setCharacterEncoding("UTF-8");
        return this.storageService.storeFile(principal, files);
    }

    @RequestMapping(value = "/api/file/upload_new", method = RequestMethod.POST)
    public FileContainerDTO addFileNew(Principal principal, @RequestParam("files[]") MultipartFile[] files, HttpServletRequest request) throws Exception{
        request.setCharacterEncoding("UTF-8");
        return this.storageService.storeFileNew(principal, files);
    }
    @RequestMapping(value = "/api/file/upload", method = RequestMethod.GET)
    public EmptyJsonResponse getSomething(Principal principal) {
        return new EmptyJsonResponse();
    }

    @RequestMapping(value = "/api/file/upload/{folderName}/{fileName}/", method = RequestMethod.DELETE)
    public Object deleteFile(Principal principal,
                             @PathVariable("folderName") String folderName,
                             @PathVariable("fileName") String fileName) {
        return this.storageService.deleteFile(principal, folderName, fileName);
    }

    @RequestMapping(value = "/api/file/upload/{folderName}/{fileName}/", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getFile(Principal principal,
                                                       @PathVariable("folderName") String folderName,
                                                       @PathVariable("fileName") String fileName) {
        return this.storageService.getFile(principal, folderName, fileName);
    }

    @RequestMapping(value = "/uploadFile.do", method = RequestMethod.GET)
    public ModelAndView getFileUploadPage() {
        return new ModelAndView("fileUploadPage");
    }
}
