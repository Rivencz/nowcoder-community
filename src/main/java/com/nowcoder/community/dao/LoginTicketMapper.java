package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LoginTicketMapper {
    /**
     * 这个接口的sql语句我们使用注解来写，不使用xml配置文件的方式
     */

    /**
     * 插入一个登录凭证LoginTicket
     * @param loginTicket
     * @return
     */
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    //将id设置为自增，插入时不需要设置他，数据库中会自动生成一个id
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据登录凭证ticket查询整个信息
     * @param ticket
     * @return
     */

    @Select({
            "select id,user_id,ticket,status,expired from login_ticket",
            "where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    /**
     * 修改登录凭证的状态信息
     * @param ticket
     * @param status
     * @return
     */
    @Update({
            "update login_ticket set status = #{status} ",
            "where ticket = #{ticket}"
    })
    int updateStatus(String ticket, int status);


}
