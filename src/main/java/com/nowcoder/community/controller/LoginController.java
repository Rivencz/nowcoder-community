package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    UserService userService;

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
     * 返回登录界面
     *
     * @return
     */
    @GetMapping("/login")
    public String loginPage() {
        return "site/login";
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
}

