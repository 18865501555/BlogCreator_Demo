package com;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.io.File;
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
     * 生成的博客网站目录
     */
    public static final File WebappDir = new File("./src/main/webapp/myBlog");

    //thymeleaf模版引擎
    private TemplateEngine te;

    /**
     * 构造方法，用来初始化
     */
    public Creator(){
        //实例化Thymeleaf引擎
        FileTemplateResolver tr = new FileTemplateResolver();
        //模版页面是html格式的
        tr.setTemplateMode("html");
        //模版页面字符集是UTF-8
        tr.setCharacterEncoding("UTF-8");
        //创建模版引擎，用来将数据绑定页面
        te = new TemplateEngine();
        te.setTemplateResolver(tr);
    }


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
        //4生成栏目信息
        createSubject(ds);
        //5拷贝资源文件
        copyResourceFile();
    }

    /**
     * 将datasource中所有栏目下的资源文件(除md文件之外的内容)拷贝到生成的网站对应栏目目录中，
     * 以便生成的页面依然可以引用它们
     */
    private void copyResourceFile(){
        //获取datasource目录下的所有栏目目录
        File[] subjectDirs = DataSourceDir.listFiles((f)->f.isDirectory());
        //遍历每一个栏目目录
        for (File subjectDir : subjectDirs
             ) {
            //扫描该栏目下面所有非md文件
            File[] subs = subjectDir.listFiles((f)->f.isFile()&&!f.getName().endsWith(".md"));
            //遍历每一个非md文件，并拷贝到生成的网站中对应栏目目录中
            File descDir = new File(WebappDir,subjectDir.getName().split("_")[0]);
            for (File resource : subs
                 ) {
                try(
                    FileInputStream fis = new FileInputStream(resource);
                    FileOutputStream fos = new FileOutputStream(new File(descDir,resource.getName()));
                ){
                    byte[] data = new byte[1024*10];
                    int len = -1;
                    while ((len=fis.read())!=-1){
                        fos.write(data,0,len);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 生成栏目信息
     * @param ds
     */
    private void createSubject(DataSource ds){
        /*
        1: 遍历DataSource中保存所有文章信息的dataSource这个Map,将所有的key获取到，key是datasource目录下每个栏目的名字，
           格式pathName_subjectName对应的在WebappDir这个目录下生成这些保存文章页面的栏目的目录名，目录名只用到pathName
           部分。
        2: 获取每个栏目下的所有文章信息，并对应的在该栏目目录下生成文章页面
        3: 在栏目目录下生成一个当前栏目的首页index.html
         */
        Set<Entry<String,List<Article>>> set = ds.getDataSource().entrySet();
        for (Entry<String ,List<Article>> e: set
             ) {
            String[] data = e.getKey().split("_");
            String fileName = data[0];
            String subjectName = data[1];
            File dir = new File(WebappDir,fileName);
            if(!dir.exists()){
                dir.mkdirs();
            }
            //在对应的栏目目录下生成文章页面
            List<Article> list = e.getValue();
            for (Article article : list
                 ) {
                //利用thymeleaf将Article与detail.html结合
                Context context = new Context();
                context.setVariable("datasource",ds);
                context.setVariable("article",article);
                File file = new File(dir,article.getFileName());
                createHtml("./src/main/webapp/template/category/detail.html",context,file);
            }
            //3生成栏目首页
            Context context = new Context();
            //用于上方栏目超链接和右侧15篇新文章超链接使用
            context.setVariable("datasource",ds);
            //栏目名，用于显示在栏目首页上的标题
            context.setVariable("subjectName",subjectName);
            //当前栏目所有文章，用于生成栏目首页上文章列表超链接
            context.setVariable("articles",list);

            File index = new File(dir,"index.html");
            createHtml("./src/main/webapp/template/category/index.html",context,index);
        }
    }
    /**
     * 将给定的模版和给定的Context结合，将生成的html代码写入指定的文件中
     * @param tempPath      模版页面路径
     * @param context       模版要结合的数据
     * @param file          生成的html文件对应的File
     */
    private  void createHtml(String tempPath,Context context,File file){
        String html = te.process(tempPath,context);
        try(
            FileOutputStream fos = new FileOutputStream(file);
        ){
            fos.write(html.getBytes("UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
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
