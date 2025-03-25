package com.donna.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.donna.domain.Book;
import com.donna.service.IBookService;
import com.donna.service.FtpService;
import com.donna.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;

@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private IBookService bookService;

    @Autowired
    private FtpService ftpService;

    @GetMapping
    public R getAll(){
        return new R(true,bookService.list());
    }

    @PostMapping
    public R save(@RequestBody Book book) throws IOException {
        logger.info("Received book data: {}", book);
        try {
            if(book.getName().equals("123")) throw new IOException();
            boolean flag = bookService.saveBook(book);
            if (flag) {
                logger.info("Book saved successfully with ID: {}", book.getId());
                return new R(true, "添加成功");
            } else {
                logger.error("Failed to save book");
                return new R(false, "添加失败：数据库操作失败");
            }
        } catch (Exception e) {
            logger.error("Error saving book: ", e);
            String errorMessage = e.getMessage();
            if (errorMessage.contains("argument type mismatch")) {
                return new R(false, "添加失败：ID类型不匹配");
            } else if (errorMessage.contains("Could not set property")) {
                return new R(false, "添加失败：数据格式错误");
            } else {
                return new R(false, "添加失败：" + errorMessage);
            }
        }
    }

    @PutMapping
    public R update(@RequestBody Book book){
        return new R(bookService.modify(book));
    }

    @DeleteMapping("{id}")
    public R delete(@PathVariable Integer id){
        return new R(bookService.delete(id));
    }

    @GetMapping("{id}")
    public R getById(@PathVariable Integer id){
        return new R(true,bookService.getById(id));
    }

    @GetMapping("{currentPage}/{pageSize}")
    public R getPage(
            @PathVariable int currentPage,
            @PathVariable int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ){
        try {
            Book book = new Book();
            book.setType(type);
            book.setName(name);
            book.setDescription(description);
            
            logger.info("Querying books with params: currentPage={}, pageSize={}, type={}, name={}, description={}",
                    currentPage, pageSize, type, name, description);
            
            QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotEmpty(book.getType())) {
                queryWrapper.like("type", book.getType());
            }
            if (StringUtils.isNotEmpty(book.getName())) {
                queryWrapper.like("name", book.getName());
            }
            if (StringUtils.isNotEmpty(book.getDescription())) {
                queryWrapper.like("description", book.getDescription());
            }
            
            IPage<Book> page = bookService.getPage(currentPage, pageSize, queryWrapper);
            
            if(currentPage > page.getPages()){
                page = bookService.getPage((int)page.getPages(), pageSize, queryWrapper);
            }
            
            return new R(true, page);
        } catch (Exception e) {
            logger.error("Error querying books: ", e);
            return new R(false, "查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public R uploadEbook(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            
            ftpService.uploadFile(tempFile, file.getOriginalFilename());
            
            return new R(true, "文件上传成功");
        } catch (IOException e) {
            logger.error("Error uploading file: ", e);
            return new R(false, "文件上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    public void downloadEbook(@PathVariable String fileName, HttpServletResponse response) {
        try {
            File tempDir = new File("temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            String localFilePath = tempDir.getPath() + File.separator + fileName;
            File localFile = new File(localFilePath);
            
            ftpService.downloadFile(fileName, localFilePath);

            if (localFile.exists()) {
                try {
                    response.setContentType("application/octet-stream");
                    String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
                    response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
                    
                    try (FileInputStream fis = new FileInputStream(localFile);
                         OutputStream os = response.getOutputStream()) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                    }
                } finally {
                    localFile.delete();
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("File not found");
            }
        } catch (IOException e) {
            logger.error("Error downloading file: ", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Download failed: " + e.getMessage());
            } catch (IOException ex) {
                logger.error("Error writing error response: ", ex);
            }
        }
    }
}