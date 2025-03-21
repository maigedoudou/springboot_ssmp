package com.donna.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.donna.domain.Book;
import com.donna.service.IBookService;
import com.donna.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private IBookService bookService;


    @GetMapping
    public R getAll(){
        return new R(true,bookService.list());
    }

    @PostMapping
    public R save(@RequestBody Book book) throws IOException {
        // 记录接收到的数据
        logger.info("Received book data: {}", book);
        
        try {
            if(book.getName().equals("123")) throw new IOException();
            boolean flag = bookService.saveBook(book);

            // 打印添加成功或失败的信息
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
            // 构建查询条件
            Book book = new Book();
            book.setType(type);
            book.setName(name);
            book.setDescription(description);
            
            // 记录查询参数
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
            
            //如果当前页码值大于总页码值，那么重新执行查询操作，使用最大页码值作为当前页码值
            if(currentPage > page.getPages()){
                page = bookService.getPage((int)page.getPages(), pageSize, queryWrapper);
            }
            
            return new R(true, page);
        } catch (Exception e) {
            logger.error("Error querying books: ", e);
            return new R(false, "查询失败：" + e.getMessage());
        }
    }





}
