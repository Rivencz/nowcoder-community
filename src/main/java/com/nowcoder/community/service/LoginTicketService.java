package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.entity.LoginTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginTicketService {
    @Autowired
    LoginTicketMapper loginTicketMapper;

    public int insertLoginTicket(LoginTicket loginTicket){
        return loginTicketMapper.insertLoginTicket(loginTicket);
    }

    public LoginTicket selectByTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateStatus(String ticket, int status){
        return loginTicketMapper.updateStatus(ticket, status);
    }
}
