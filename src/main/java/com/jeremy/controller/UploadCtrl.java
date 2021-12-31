package com.jeremy.controller;

import com.jeremy.service.UploadService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jeremy on 2017/3/2.
 */
@RestController
public class UploadCtrl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/upload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @Value("${upload.url}")
    private String uploadUrl;

    @Autowired
    private UploadService uploadService;

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public Map handleFileUpload(@RequestParam(value="filename") MultipartFile file
//            ,@RequestParam("fPath") String fPath
    ){

        logger.debug("url:"+uploadUrl);
//        logger.debug(fPath);

        Map retMap = new HashMap();
        String name = file.getOriginalFilename();
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                logger.debug(file.getContentType());
                logger.debug(file.getName());
                logger.debug(file.getOriginalFilename());
                String tmpPath = uploadService.getTmpPath();
                File filePath = new File(tmpPath);
                if(!filePath.exists()){
                    filePath.mkdirs();
                }
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(tmpPath+file.getOriginalFilename())));
                stream.write(bytes);
                stream.close();

                retMap.put("rspCode","00");
                retMap.put("rspMsg","上传成功");
                retMap.put("filename",file.getOriginalFilename());
            } catch (Exception e) {
                e.printStackTrace();
                logger.debug(e.getMessage());
                retMap.put("rspCode","99");
                retMap.put("rspMsg","上传失败");
            }
        } else {
            retMap.put("rspCode","90");
            retMap.put("rspMsg","文件为空");
        }
        return  retMap;
    }

}
