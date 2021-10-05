package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//将这个实体类注册为Bean组件
@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    DiscussPostService discussPostService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page) {
//      方法调用前，SpringMVC会自动实例化Model和Page，并将page注入到Model

//        默认设置数据为全部的帖子数量
        page.setRows(discussPostService.findDiscussPostRows(0));
//        设置页面的访问路径
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
//         用来存放所有用户-帖子的集合
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                User user = userService.findUserById(discussPost.getUserId());
        //      使用一个Map集合存放用户信息和对应的帖子信息
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                map.put("user", user);
                discussPosts.add(map);
            }
        }
//
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }
}
