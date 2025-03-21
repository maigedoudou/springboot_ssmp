package com.donna.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donna.dao.BookDao;
import com.donna.domain.Book;
import com.donna.service.IBookService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl extends ServiceImpl<BookDao, Book> implements IBookService {

    @Autowired
    private BookDao bookDao;

    @Override
    public boolean saveBook(Book book) {
        return bookDao.insert(book) > 0;
    }

    @Override
    public boolean modify(Book book) {
        return bookDao.updateById(book) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return bookDao.deleteById(id) > 0;
    }

    @Override
    public IPage<Book> getPage(int currentPage, int pageSize) {
        IPage page = new Page(currentPage, pageSize);
        bookDao.selectPage(page, null);
        return page;
    }

    @Override
    public IPage<Book> getPage(int currentPage, int pageSize, QueryWrapper<Book> queryWrapper) {
        IPage<Book> page = new Page<>(currentPage, pageSize);
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<Book> getPage(int currentPage, int pageSize, Book book) {
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        
        if (Strings.isNotEmpty(book.getType())) {
            queryWrapper.like("type", book.getType());
        }
        if (Strings.isNotEmpty(book.getName())) {
            queryWrapper.like("name", book.getName());
        }
        if (Strings.isNotEmpty(book.getDescription())) {
            queryWrapper.like("description", book.getDescription());
        }

        return this.getPage(currentPage, pageSize, queryWrapper);
    }
}
