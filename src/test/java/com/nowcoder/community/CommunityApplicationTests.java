package com.nowcoder.community;

import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.sql.DataSource;
import java.util.*;

@SpringBootTest

@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());
//       同时，可以通过在Repository注解上添加别名的方式来访问指定的实现类
        AlphaDao alphaDao2 = applicationContext.getBean("hibernateImpl", AlphaDao.class);
        System.out.println(alphaDao2.select());
    }

    @Autowired
    @Qualifier(value = "hibernateImpl")
    AlphaDao alphaDao;

    /**
     * UserMapper查询方法测试
     */
    @Test
    void contextLoads() {
//        对UserMapper的测试
        User user = userMapper.selectById(1);
        System.out.println(user);

        user = userMapper.selectByName("zhangfei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder111@sina.com");
        System.out.println(user);
    }

    /**
     * UserMapper插入方法测试
     */
    @Test
    void insertTest() {
        User user = new User();
        user.setUsername("Riven");
        user.setPassword("123456");
        user.setSalt("hahaha");
        user.setEmail("Riven@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int row = userMapper.insertUser(user);
        System.out.println(row);
        System.out.println(user.getId());
    }

    @Test
    public void updateTest() {
        int row = userMapper.updateStatus(150, 1);
        System.out.println(row);
        row = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(row);
        row = userMapper.updatePassword(150, "222222");
        System.out.println(row);
    }

    /**
     * DiscussPostMapper接口方法测试
     */
    @Test
    public void test2() {
//        查询方法
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void test3() {
        int i = discussPostMapper.selectDiscussPostRows(103);
        System.out.println(i);
    }

    public static final Logger logger = LoggerFactory.getLogger(CommunityApplicationTests.class);

    @Test
    public void LogTest() {
//        根据设置的日志显示级别来显示，级别从低到高：trace<debug<info<warn<error
        logger.debug("debug test");
        logger.info("info test");
        logger.warn("warn test");
        logger.error("error test");
    }

    @Autowired
    MailClient mailClient;

    /**
     * MailClient发送邮件工具类测试
     */
    @Test
    public void sendTextMail() {
        mailClient.sendMail("fiorac@163.com", "MailTest", "我渴望，有价值的对手！");
    }

    //模板引擎
    @Autowired
    TemplateEngine templateEngine;

    @Test
    public void sendHtmlMail(){
//        首先创建一个Context对象存储一些信息
        Context context = new Context();
        context.setVariable("username", "没错吧！");

//        传入执行的页面路径，不用带后缀名，以及一些自己添加的模板信息
        String content = templateEngine.process("mail/demo", context);
        System.out.println(content);

//        最后仍旧使用发送邮件类的sendMail方法，将刚才的页面内容传入
        mailClient.sendMail("xxx@xxx.com", "HTMLTest", content);
    }


}
