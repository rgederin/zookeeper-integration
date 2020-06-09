package com.gederin.repository;

import com.gederin.model.Book;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Repository
@Data
public class BookRepository {
    private final List<Book> books = new ArrayList<>();
}
