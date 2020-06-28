package com;

import java.io.File;

/**
 * 主类
 * @author orange
 * @create 2020-06-28 10:53 下午
 */
public class Creator {
    public static void main(String[] args) {
        File file = new File("./datesource/it_科技/page1.md");
        Article article = new Article(file);
    }
}
