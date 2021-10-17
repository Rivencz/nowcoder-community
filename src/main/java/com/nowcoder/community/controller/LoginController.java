package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    public static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    Producer producer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 直接将注册界面返回
     *
     * @return
     */
    @GetMapping("/register")
    public String registerPage() {
        return "site/register";
    }

    /**
     * 从主页面点击登录时，返回登录界面
     *
     * @return
     */
    @GetMapping("/login")
    public String loginPage() {
        return "site/login";
    }


    /**
     * 登录界面的处理逻辑，因为要处理一个表单，所以请求方式为post，以此和另一个login方法区分
     *
     * @param model
     * @param username
     * @param password
     * @param code
     * @param rememberme 是否记住该用户，如果记住，那么它的登录凭证存活时间就会变长
     * @param session    我们需要确认用户输入的验证码是否正确，而当验证码生成时存放在了session中
     * @param response   将登录凭证存放在一个cookie中
     * @return
     */
    @PostMapping("/login")
    public String login(Model model, String username, String password, String code,
                        boolean rememberme, HttpSession session, HttpServletResponse response) {
        // 首先判断验证码是否有误，如果验证码错误直接返回到登录界面
        String kaptcha = (String) session.getAttribute("kaptcha");
        // 如果验证码为空或者输入的和生成的验证码不相同，直接返回到登录界面
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码错误！");
            return "site/login";
        }

        //下面根据用户是否点击记住我来设置过期时间，这两个变量从工具类中取
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        //调用登录的业务方法
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //如果登录凭证不是空，说明登录成功了
        if (map.containsKey("ticket")) {
            // 登录成功，重定向到首页，并且将登录凭证放入到cookie中
            String ticket = (String) map.get("ticket");
            Cookie cookie = new Cookie("ticket", ticket);
            // 设置cookie的生效路径，默认整个项目
            cookie.setPath(contextPath);
            // 设置cookie的失效时间
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            // 重定向到首页
            return "redirect:/index";
        } else {
            // 否则，说明出现了错误，将所有可能的错误信息都放到map中
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            // 返回到登录界面
            return "site/login";
        }
    }

    /**
     * 对注册表单进行操作
     *
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
//        如果map为空，说明什么错误信息都没有存入，也就是注册成功了
//        这样我们可以直接返回到一个自定义的界面，因为我们还没有激活，所以现在先不跳转到登录界面
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您发送了一封激活邮件，请尽快激活");
//          跳转链接，我们跳转到主页面
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
//        如果有错误信息，就还返回到注册界面,我们不管这三个信息哪个为空，都传入进去
//            如果为空的话我们再进行判断
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    /**
     * 激活用户的控制层实现，如果激活成功，返回到登录界面，如果重复激活或者激活失败，返回到首页
     *
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功！现在您可以登录了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEATE) {
            model.addAttribute("msg", "该账号已经被激活过，请勿重复激活！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活码错误，激活失败！");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
//        根据我们在配置类中配置的属性生成一个字符串形式的验证码
        String text = producer.createText();
//        然后将它转换成图片样式
        BufferedImage image = producer.createImage(text);

//        因为我们登录的时候还需要进行确认，所以将验证码文本放入session中
        session.setAttribute("kaptcha", text);

//        设置输出的内容格式
        response.setContentType("image/png");

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败！" + e.getMessage());
        }
    }

    /**
     * 退出用户，修改目标status即可
     * @param ticket
     * @return
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        //重定向，默认返回的是对应路径的GET请求
        return "redirect:/login";
    }
}

