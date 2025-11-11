package com.ping.usercenter.service;

import com.ping.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ping.usercenter.model.domain.User;

/**
 * @author 21877
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2025-11-09 15:21:40
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);
}
