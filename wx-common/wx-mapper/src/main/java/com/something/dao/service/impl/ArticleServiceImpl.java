package com.something.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.something.dao.domain.Article;
import com.something.dao.mapper.ArticleMapper;
import com.something.dao.service.IArticleService;
import org.springframework.stereotype.Service;

/**
* @author rock
* @description 针对表【article】的数据库操作Service实现
* @createDate 2025-05-12 09:56:45
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

}




