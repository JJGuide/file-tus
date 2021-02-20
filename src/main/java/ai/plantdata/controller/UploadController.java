package ai.plantdata.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@CrossOrigin(exposedHeaders = {"Location", "Upload-Offset"})
@RequestMapping(value = "/api/")
//@Api(tags = "上传接口")
public class UploadController {


    private final TusFileUploadService tusFileUploadService;

    public static Path uploadDirectory;

    private final Path tusUploadDirectory;

    public UploadController(TusFileUploadService tusFileUploadService,
                            AppProperties appProperties) {
        this.tusFileUploadService = tusFileUploadService;
        this.tusUploadDirectory = Paths.get(appProperties.getTusUploadDirectory());
    }


    @ApiOperation(value = "上传接口")
    @RequestMapping(value = {"/upload", "/upload/**"}, method = {RequestMethod.POST,
            RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.DELETE, RequestMethod.GET})
    public void upload(HttpServletRequest servletRequest,
                       HttpServletResponse servletResponse
    ) throws IOException {


        //从header中读取这个路径，也需要设置返回给前端
        String filePath = servletRequest.getHeader("filePath");
        servletResponse.setHeader("filePath", filePath);

        this.uploadDirectory = Paths.get(filePath);

        System.out.println("header:path:" + filePath);


        try {
            Files.createDirectories(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tusFileUploadService.withStoragePath(filePath);

        this.tusFileUploadService.process(servletRequest, servletResponse);

        String uploadURI = servletRequest.getRequestURI();
        Path locksDir = this.tusUploadDirectory.resolve("locks");
        if (Files.exists(locksDir)) {
            try {
                this.tusFileUploadService.cleanup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        UploadInfo uploadInfo = null;
        try {
            uploadInfo = this.tusFileUploadService.getUploadInfo(uploadURI);
        } catch (IOException | TusException e) {
            e.printStackTrace();
        }

        if (uploadInfo != null && !uploadInfo.isUploadInProgress()) {
            try (InputStream is = this.tusFileUploadService.getUploadedBytes(uploadURI)) {
                Path output = this.uploadDirectory.resolve(uploadInfo.getFileName());
                Files.copy(is, output, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException | TusException e) {
                e.printStackTrace();
            }

            try {
                this.tusFileUploadService.deleteUpload(uploadURI);
            } catch (IOException | TusException e) {
                e.printStackTrace();
            }
        }


    }


    @Scheduled(fixedDelayString = "PT24H")
    private void cleanup() {
        Path locksDir = this.tusUploadDirectory.resolve("locks");
        if (Files.exists(locksDir)) {
            try {
                this.tusFileUploadService.cleanup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @ApiOperation(value = "下载接口1")
    @GetMapping("/test")
    public void download(@RequestParam String fileName, @RequestParam String filePath, HttpServletResponse response) {
        System.out.println(fileName + "  " + fileName);

        if (fileName != null) {
            FileInputStream is = null;
            BufferedInputStream bs = null;
            OutputStream os = null;
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    response.setHeader("Content-Type", "application/octet-stream");
                    response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                    is = new FileInputStream(file);
                    bs = new BufferedInputStream(is);
                    os = response.getOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = bs.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    System.out.println("文件下载成功---------------------------");
                }
            } catch (IOException ex) {
                System.out.println("文件下载异常----------: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (bs != null) {
                        bs.close();
                    }
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("文件不存在---------filePath:" + filePath);


    }


    @GetMapping("/download")
    @ApiOperation(value = "下载接口2")
//    @PostMapping("download")
    public ResponseEntity<Resource> download(@RequestParam("filePath") String filePath, HttpServletRequest request) throws IOException {
//    public ResponseEntity<Resource> download(@RequestBody String filePath, HttpServletRequest request) throws IOException {


        Resource resource = loadFileAsResource(filePath);
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            //logger.info("Could not determine file type.");
        }
        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);


    }

    public Resource loadFileAsResource(String fileName) throws MalformedURLException {
        Path fileStorageLocation = Paths.get("/Users/shizeying/Desktop/")
                .toAbsolutePath().normalize();
        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        }
        return null;
    }

}