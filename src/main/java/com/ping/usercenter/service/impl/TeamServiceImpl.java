package com.ping.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ping.usercenter.model.domain.Team;
import com.ping.usercenter.service.TeamService;
import com.ping.usercenter.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
 * @author 21877
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2025-11-09 15:21:40
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

}




