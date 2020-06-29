package com;

import java.util.*;

/**
 * 数据源对象
 * 保存datasource目录下的所有文章及栏目信息并且根据不同页面上显示的内容分别保存这些数据
 * @author orange
 * @create 2020-06-29 6:50 下午
 */
public class DataSource {
    /**
     * 所有页面右侧最新文章的条目数
     */
    public static final int TOP_15 = 15;
    /**
     * 首页上最新文章预览的文章数
     */
    public static final int INDEX_PAGE_TOP_5 = 5;

    //所有栏目及对应的文章对象
    private Map<String, List<Article>> dataSource;
    //所有页面右侧的最新文章列表，size应当是<=TOP_15指定数量
    private List<Article> topArticles = new ArrayList<>();
    //网站首页上的最新文章预览的文章
    private List<Article> indexPageTopArticles = new ArrayList<>();
    /*
    每个页面的上方都有栏目的超链接，下面集合中每个Map就是便于页面生成栏目超链接使用
    key有两个，分别是:pathName,subjectName
    value分别对应：目录名，栏目名
    它们对应datasource每个栏目目录名字，pathName_subjectName
     */
    private List<Map<String ,String >> allSubjectName = new ArrayList<>();

    public DataSource(Map<String,List<Article>> dataSource){
        this.dataSource = dataSource;
        //初始化最新15篇文章
        initTop15Articles();
        //初始化首页需要的最新5篇文章
        initIndexPageTopArticles();
        //初始化所有栏目信息(为了各页面上超链接使用)
        initAllSubjectNames();
    }
    private void initTop15Articles(){
        /*
        1: 将dataSource这个Map中所有栏目下的所有文章存入同一个集合中
        2: 再将该集合按照最后修改时间顺序
        3: 截取该集合前15各元素
         */
        //先将所有文章放到同一个集合中
        Collection<List<Article>> values = dataSource.values();
        for (List<Article> e : values
             ) {
            topArticles.addAll(e);
        }
        //对所有文章整体排序
        topArticles.sort((a1,a2)->a1.getLastModified()>a2.getLastModified()?-1:1);
        //截取前15条
        topArticles = topArticles.subList(0,topArticles.size()>=TOP_15?TOP_15:topArticles.size());
    }
    private void initIndexPageTopArticles(){
        indexPageTopArticles = topArticles.subList(
                0,topArticles.size()>=INDEX_PAGE_TOP_5?INDEX_PAGE_TOP_5:topArticles.size()
        );
    }
    private void initAllSubjectNames(){
        /*
        遍历dataSource的所有key(每个key是datasource下栏目的目录名，格式pathName_subjectName)
        将每个名字按照"_"拆分为pathName和subjectName每个栏目信息用一个Map保存，该Map有两组键值对
        第一个组:key是字符串"pathName",value为pathName部分
        第二个组:key是字符串"subjectName",value为subjectName部分

        将该Map存入allSubjectNames集合中，以便将来再所有页面最上方生成栏目超链接
         */
        Set<String> set = dataSource.keySet();

        for (String key : set
             ) {
            String[] data = key.split("_");
            Map<String ,String> map = new HashMap<>();
            map.put("pathName",data[0]);
            map.put("subjectName",data[1]);
            allSubjectName.add(map);
        }
    }

    public Map<String, List<Article>> getDataSource() {
        return dataSource;
    }

    public void setDataSource(Map<String, List<Article>> dataSource) {
        this.dataSource = dataSource;
    }

    public List<Article> getTopArticles() {
        return topArticles;
    }

    public void setTopArticles(List<Article> topArticles) {
        this.topArticles = topArticles;
    }

    public List<Article> getIndexPageTopArticles() {
        return indexPageTopArticles;
    }

    public void setIndexPageTopArticles(List<Article> indexPageTopArticles) {
        this.indexPageTopArticles = indexPageTopArticles;
    }

    public List<Map<String, String>> getAllSubjectName() {
        return allSubjectName;
    }

    public void setAllSubjectName(List<Map<String, String>> allSubjectName) {
        this.allSubjectName = allSubjectName;
    }
}
