package com.nowcoder.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaControllerDemo1 {

    @RequestMapping("/hello")
    @ResponseBody
    public String test1() {
        return "Hello World!";
    }

    @GetMapping("/students")
    @ResponseBody
    public String test2(
            @RequestParam(value = "name", required = false, defaultValue = "ZhangSan") String name,
            @RequestParam(value = "age", required = false, defaultValue = "22") int age) {
        System.out.println(name);
        System.out.println(age);
        return "I'm a Good Student";
    }

    //    浏览器向服务器提交数据
    @PostMapping("/student")
    @ResponseBody
    public String test3(String username, int age) {
//        表单名称要和方法的参数名称相同，这样可以保证自动传入
        System.out.println(username);
        System.out.println(age);
        return "Post Success!";
    }

    //    访问一个视图界面
    @GetMapping("/getCompany")
    public String test4(Model model) {
        model.addAttribute("name", "PDD");
        return "demo/company";
    }

    //    JSON数据的自动转化，Java对象返回到页面中会自动转换成JSON格式
    @GetMapping("/getEmployee")
    @ResponseBody
    public List<Map<String, Object>> getEmp() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 22);
        list.add(map);

        map = new HashMap<>();
        map.put("name", "李四");
        map.put("age", 24);
        list.add(map);

        map = new HashMap<>();
        map.put("name", "王五");
        map.put("age", 25);
        list.add(map);

        return list;
    }

    @GetMapping("/cookie/set")
    @ResponseBody
    public String cookieSet(HttpServletResponse response){
        Cookie cookie = new Cookie("name", "riven");
//        设置生效路径和存活时间
        cookie.setPath("/community/alpha");
        cookie.setMaxAge(60 * 60);
//        将cookie通过响应头添加给服务器端
        response.addCookie(cookie);
        return "cookie test";
    }

    @GetMapping("/cookie/get")
    @ResponseBody
    public String cookieGet(@CookieValue("name") String value){
        System.out.println(value);
        return value;
    }

    @GetMapping("/session/set")
    @ResponseBody
    public String sessionSet(HttpSession httpSession){
        httpSession.setAttribute("Id", "111222333");
        httpSession.setAttribute("name", "德玛西亚！");
        return "Session set";
    }

    @GetMapping("/session/get")
    @ResponseBody
    public String sessionGet(HttpSession session){
        System.out.println(session.getAttribute("Id"));
        System.out.println(session.getAttribute("name"));
        System.out.println(session.getAttribute("ss"));
        return "session Get";
    }
}
