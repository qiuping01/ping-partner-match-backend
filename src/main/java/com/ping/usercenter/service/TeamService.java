package com.ping.usercenter.service;

import com.ping.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ping.usercenter.model.domain.User;
import com.ping.usercenter.model.dto.TeamQuery;
import com.ping.usercenter.model.request.TeamUpdateRequest;
import com.ping.usercenter.model.vo.TeamUserVO;
import com.ping.usercenter.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
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

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,
                       User loginUser);

    /**
     * 加入队伍
     *
     * @param teamId
     * @param teamPassword
     * @param loginUser
     * @return
     */
    boolean joinTeam(Long teamId, String teamPassword, User loginUser);

    /**
     * 用户退出队伍
     *
     * @param teamId 队伍 id
     * @param loginUser 登录用户
     * @return 退出结果
     */
    boolean quitTeam(Long teamId, User loginUser);
}
