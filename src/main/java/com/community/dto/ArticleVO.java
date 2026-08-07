package com.community.dto;

import com.community.entity.Article;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleVO extends Article {
    private List<String> tags;
}
