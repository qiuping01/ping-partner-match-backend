package com.ping.usercenter.service;

import com.ping.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ping.usercenter.model.domain.User;
import com.ping.usercenter.model.dto.TeamQuery;
import com.ping.usercenter.model.vo.TeamUserVO;
import com.ping.usercenter.model.vo.UserVO;

import java.util.List;

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

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 获取脱敏队伍信息
     * @param team
     * @return
     */
    TeamUserVO getTeamUserVO(Team team);

}
