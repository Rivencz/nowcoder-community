package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    UserMapper userMapper;

    //   由于注册需要发HTML邮件，因此要把相关类和域名等都注入进来
    @Autowired
    MailClient mailClient;

    @Autowired
    TemplateEngine templateEngine;

    /**
     * LoginTicket类相关的三个方法的service层实现
     */
    @Autowired
    LoginTicketMapper loginTicketMapper;

    //    网站域名
    @Value("${community.path.domain}")
    String domain;
    //     项目根目录
    @Value("${server.servlet.context-path}")
    String contextPath;

    /**
     * 根据用户id查询用户
     *
     * @param id
     * @return
     */
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 注册用户的业务类，由于可能会出现用户，邮箱重复等多种情况，因此我们返回一个Map集合可以包含各种类型信息
     *
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
//      先检查用户名，密码，邮箱是否为空
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

//        检查用户名和邮箱是否可用
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已经存在");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已经被占用");
            return map;
        }

//      由于随机字符串比较长，只取五位和用户输入密码合并然后进行加密
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
//      之后重新对密码进行设置，放置被破解
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
//      将一些信息补全
//        设置type为0表示普通用户
        user.setType(0);
//        设置status为0表示未激活，需要激活码来将它激活
        user.setStatus(0);
//        设置一个激活码
        user.setActivationCode(CommunityUtil.generateUUID());
//        设置一个随机的头像，随机数表示0t~1000t之间随机选择一个数，都有一个随机的图片，用作头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
//        设置创建时间
        user.setCreateTime(new Date());

//        最后将这个用户放入数据库中
        userMapper.insertUser(user);

//        不要忘记还要发送激活邮件给当前用户
        Context context = new Context();
        context.setVariable("email", user.getEmail());
//        设置激活路径，也就是你要求怎么激活这个用户，
//        我们可以使用下面这个格式，通过后续实现activation激活功能根据传入的userid和激活码来激活他
//        http://localhost/8080/community/activation/userId/code
        String url = domain + "/" + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

//        通过模板引擎执行来获取页面内容，并给发送邮件调用方法显示
        String content = templateEngine.process("mail/activation", context);

        mailClient.sendMail(user.getEmail(), "激活账户", content);

        return map;
    }


    /**
     * 激活用户业务方法，先根据用户id查询出当前用户的status，防止重复激活，然后再根据激活码判断是否可以激活
     *
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
//        先判断是否存在，如果存在，那么就是重复激活
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEATE;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 登录业务，因为可能会有很多不同的业务，因此返回一个map集合，可以记录各种信息
     *
     * @param username
     * @param password
     * @param expiredseconds 注意必须是long类型，否则后面*1000会造成数据溢出
     * @return
     */
    public Map<String, Object> login(String username, String password, long expiredseconds) {
        Map<String, Object> map = new HashMap<>();
        //首先检查用户名和密码是否为空
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        //然后检查用户名和密码是否合格
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该用户不存在！");
            return map;
        }
        //检查该用户是否未激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        //账户存在了，检查对应的密码是否正确
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }

        //所有都检查完了，生成一个登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        //0表示有效登录，1表示退出
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredseconds * 1000));

        loginTicketMapper.insertLoginTicket(loginTicket);

//      将这个登录凭证字符串放入map，用这个ticket来寻找对应的登录凭证，从而可以根据凭证中的userid找到对应的用户
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    /**
     * 退出登录，根据传入的ticket，找到对应的登录凭证，修改状态status即可
     *
     * @param ticket
     * @return
     */
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

}
