package com;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

/**
 * 主类
 * @author orange
 * @create 2020-06-28 10:53 下午
 */
public class Creator {
    /**
     * 数据源目录
     */
    public static final File DataSourceDir = new File("./src/main/webapp/datasource");
    /**
     *
     */
    public static final File WebappDir = new File("./src/main/webapp/myBlog");


    public static void main(String[] args) {
        Creator creator = new Creator();
        creator.start();
    }
    /**
     * 开始生成
     */
    public void start(){
        //1解析数据源目录生成DataSource对象
        DataSource ds = loadDataSource();
        //2生成导出博客页面的根目录
        createWebappDir();
        //3生成首页
        createIndexPage(ds);
    }

    /**
     * 生成网站首页
     * @param ds
     */
    private void createIndexPage(DataSource ds){
        /*
        利用Thymeleaf将模版页面中的首页和DataSource结合，生成博客首页
         */
        //实例化Thymeleaf引擎
        FileTemplateResolver tr = new FileTemplateResolver();
        //模版页面是html格式的
        tr.setTemplateMode("html");
        //模版页面字符集是UTF-8
        tr.setCharacterEncoding("UTF-8");
        //创建模版引擎，用来将数据绑定页面
        TemplateEngine te = new TemplateEngine();
        te.setTemplateResolver(tr);

        Context context = new Context();
        context.setVariable("datasource",ds);

        String html = te.process("./src/main/webapp/template/index.html",context);
        //将生成的首页代码写入文件
        File index = new File(WebappDir,"index.html");
        try {
            FileOutputStream fos = new FileOutputStream(index);
            fos.write(html.getBytes("UTF-8"));
            System.out.println("首页已生成!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成保存导出的博客页面的根目录
     */
    private void createWebappDir(){
        //若目录不存在则创建
        if(!WebappDir.exists()){
            WebappDir.mkdirs();
        }
    }

    /**
     * 读取数据源目录，扫描所有的栏目目录及对应的md文件
     * 组建数据并以DataSource对象形式返回
     * @return
     */
    public DataSource loadDataSource(){
     /*
     首先创建一个Map,key为String,value为List<Article>

     然后获取DataSourceDir(源数据目录)中每个子目录(每个子目录相当于一个栏目)，用该目录的名字作为key

     然后再获取每个栏目中的所有md文件，并将每个md文件转化为一个Article对象并存入List集合，再将该集合作为value

     最终得到的map中有若干组键值对，每一组是一个栏目key为栏目名，value为栏目下所有的文章
      */
        Map<String, List<Article>> map = new HashMap<>();
        //获取datesource下的所有栏目目录
        File[] subjects = DataSourceDir.listFiles(f->f.isDirectory());
        //遍历每一个栏目
        for (File subject : subjects
             ) {
            /*
            获取每个栏目下的所有md文件，系统有时候会生成临时文件，名字与该文件比前面会多一个"."或者"_"
            比如:_page1.md       .page1.md
            因此获取时要忽略这些临时文件
             */
            File[] articleFiles = subject.listFiles(
                    f->f.getName().endsWith(".md")
                    && !f.getName().startsWith(".")//忽略临时文件
                    && !f.getName().startsWith("_")//忽略临时文件
            );
            //对一个栏目下所有文章按修改时间排序
            Arrays.sort(articleFiles,(f1,f2)->f1.lastModified()>f2.lastModified()?-1:1);

            List<Article> list = new ArrayList<>();
            //遍历每一个文章
            for (File articleFile : articleFiles
                 ) {
                Article article = new Article(articleFile);
                list.add(article);
            }
            map.put(subject.getName(),list);
        }
        return new DataSource(map);
    }
}
