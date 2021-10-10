package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    /**
     * 返回一个UUID随机字符串
     * @return
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 将密码进行MD5加密，为了增加安全性，每个用户有一个salt属性是一个随机字符串，加他和用户输入密码
     * 合并之后再进行加密可以降低泄漏风险
     * @param key
     * @return
     */
    public static String md5(String key){
//        如果要加密的字符串为null就直接返回null
        if(StringUtils.isBlank(key)){
            return null;
        }
//        通过工具类对传入的字符串进行MD5加密
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
