package ru.skypro.homework.dto.comment;

import lombok.Data;

import java.util.List;

@Data
public class Comments {
    private int count;
    private List<Comment> results;
}
