package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

//DiscussPost实体类的接口
@Mapper
@Repository
public interface DiscussPostMapper {
    /**
     * 分页查找帖子，根据userId是否为0进行动态sql的查询
     * 为0，就是全部帖子
     * 不为0，就是某一个用户的帖子
     * @param userId
     * @param offset 起始页
     * @param limit 每一页的数量
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);


    /**
     * 查询帖子数量
     * 注意：由于userId参数是动态的可有可无的，
     * 如果该方法参数只有这一种，并且它是用在sql语句中的if条件中，那么必须加@Param注解给他取别名
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

}
