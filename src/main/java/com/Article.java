package com;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author orange
 * @create 2020-06-28 10:53 下午
 */
public class Article {
    //markdown转换为html的解析器
    private static MutableDataSet options = new MutableDataSet();
    private static Parser parser = Parser.builder(options).build();
    private static HtmlRenderer renderer = HtmlRenderer.builder(options).build();

    //文件名，将来用于超链接可以定位该文件
    private String fileName;

    /*
    在datasource目录下，每个栏目是以目录形式存在，而目录的命名格式为：xxx_xxx
    例如:it_科技
    这里的"it"就是将来生成一套webapp后的栏目目录名字，而"科技"将来是用于展现在页面上该栏目的名字
    因此下面两个属性分别保存它们，其中:
    pathName保存目录名:it
    subjectName保存栏目名:科技
     */
    //所在栏目的目录名
    private String pathName;

    //所在栏目的栏目名
    private String subjectName;

    //文章最后修改时间(用于自动排版最新文章使用)
    private long lastModified;

    //md文件中的元数据，md文件最开始的"---"之间的内容
    private Map<String ,String> metaData = new HashMap<>();

    //md文件中该文件的正文部分，元数据之后的所有内容的HTML格式
    private String content;//需要API将md内容转换为html后保存

    /**
     * 根据给定的File对象将其表示的markdown文件内容转化为一个Article实例
     * @param file
     */
    public Article(File file) {
        //1初始化md文件相关信息

        //2初始化md文件内容相关信息

    }
    private void initFileInfo(File file){
        System.out.println("Article:初始化md文件相关信息...");
        fileName = file.getName().replace(".md",".html");
        String[] data = file.getParentFile().getName().split("_");
        pathName = data[0];
        subjectName = data[1];
        lastModified = file.lastModified();
        System.out.println("fileName:"+fileName);
        System.out.println("pathName:"+pathName);
        System.out.println("subjectName:"+subjectName);
        System.out.println("lastModified:"+lastModified);
        System.out.println("Article:初始化md文件相关信息完毕!");
    }
    private void initContentInfo(File file){
        /*
        先读取md文件中的所有内容(都是文本数据)并用一个String保存，然后将内容拆分即可
        首先截取出开始的两个---之间的所有内容，这部分是元数据
        然后每个元数据都是一行字符串，每一行再按照":\\s*"拆分出名字和值并以key，value形式
        存入metaData这个Map
        之后的内容用API将markdown内容转换为html代码并存入到content中
         */
        try(
            RandomAccessFile raf = new RandomAccessFile(file,"r");
        ){
            byte[] data = new byte[(int)raf.length()];
            raf.read(data);
            String txt = new String(data,"UTF-8");
            System.out.println(txt);//md文件所有内容

            //截取出元数据部分
            int start = txt.indexOf("---")+3;
            int end = txt.indexOf("---",start);
            String metaStr = txt.substring(start,end);
            System.out.println(metaStr);
            //每次读取其中一行
            Scanner scanner = new Scanner(metaStr);
            while (scanner.hasNext()){
                String line = scanner.nextLine();
                if(line.trim().isEmpty()){//过滤空行
                    continue;
                }
                String[] arr = line.split(":\\s");
                metaData.put(arr[0],arr[1]);
            }
            scanner.close();
            System.out.println("metaData:"+metaData);

            //截取出文章内容
            String contentStr = txt.substring(end);
            System.out.println(contentStr);

            //将md文件中的文章正文转换为html代码格式
            Node doc = parser.parse(contentStr);
            content = renderer.render(doc);
            System.out.println(content);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
