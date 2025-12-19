package com.ping.usercenter.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ping.usercenter.common.ErrorCode;
import com.ping.usercenter.exception.BusinessException;
import com.ping.usercenter.model.domain.Team;
import com.ping.usercenter.model.domain.User;
import com.ping.usercenter.model.domain.UserTeam;
import com.ping.usercenter.model.dto.TeamQuery;
import com.ping.usercenter.model.enums.TeamStatusEnum;
import com.ping.usercenter.model.request.TeamUpdateRequest;
import com.ping.usercenter.model.vo.TeamUserVO;
import com.ping.usercenter.model.vo.UserVO;
import com.ping.usercenter.service.TeamService;
import com.ping.usercenter.mapper.TeamMapper;
import com.ping.usercenter.service.UserService;
import com.ping.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 21877
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2025-11-09 15:21:40
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务 出问题抛异常
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        // 3. 校验信息
        //  3.1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //  3.2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  3.3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  3.4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍权限不满足要求");
        }
        //  3.5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.ENCRYPT.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }

        //  3.6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //  3.7. 校验用户最多创建 5 个队伍
        // todo 有 bug ， 可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum > 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }

        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 5. 插入用户 ---> 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @return
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        // 构造组合查询条件
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollUtil.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            // WHERE ... AND (name LIKE '%searchText%' OR description LIKE '%searchText%')
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw
                        .like("name", searchText)
                        .or()
                        .like("description", searchText)
                );
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "权限不足");
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        // 不展示已过期的队伍
        // WHERE (expireTime > NOW() OR expireTime IS NULL)
        queryWrapper.and(qw -> qw
                .gt("expireTime", new Date())
                .or()
                .isNull("expireTime")
        );
        // 查询并返回
        List<Team> teamList = this.list(queryWrapper);
        if (CollUtil.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            UserVO userVO = userService.getUserVO(userService.getById(userId));
            TeamUserVO teamUserVO = getTeamUserVO(team);
            teamUserVO.setCreateUser(userVO);
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * 获取脱敏队伍信息
     *
     * @param team
     * @return
     */
    @Override
    public TeamUserVO getTeamUserVO(Team team) {
        if (team == null) {
            return null;
        }
        TeamUserVO teamUserVO = new TeamUserVO();
        BeanUtil.copyProperties(team, teamUserVO);
        return teamUserVO;
    }

    /**
     * 更新队伍信息
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        // 1. 基础参数校验
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamUpdateRequest.getId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID不能为空");
        }
        // 2. 校验队伍是否存在
        Team oldTeam = this.getById(teamId);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Long loginUserId = loginUser.getId();
        if (loginUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 3. 校验权限 - 只有管理员或创建者可以修改
        if (!userService.isAdmin(loginUser) && !oldTeam.getUserId().equals(loginUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 4. 使用 UpdateWrapper 动态构建更新
        UpdateWrapper<Team> updateTeam = new UpdateWrapper<>();
        updateTeam.eq("id", teamId);
        boolean hasUpdate = false;
        // 5. 处理名称
        if (teamUpdateRequest.getName() != null) {
            if (StringUtils.isNotBlank(teamUpdateRequest.getName())) {
                if (!oldTeam.getName().equals(teamUpdateRequest.getName())) {
                    updateTeam.set("name", teamUpdateRequest.getName());
                    hasUpdate = true;
                }
            }
        }
        // 6. 处理描述
        if (teamUpdateRequest.getDescription() != null) {
            if (StringUtils.isNotBlank(teamUpdateRequest.getDescription())) {
                if (!oldTeam.getDescription().equals(teamUpdateRequest.getDescription())) {
                    updateTeam.set("description", teamUpdateRequest.getDescription());
                    hasUpdate = true;
                }
            }
        }
        // 7. 处理过期时间
        if (teamUpdateRequest.getExpireTime() != null) {
            if (teamUpdateRequest.getExpireTime().getTime() > System.currentTimeMillis()) {
                Date newExpireTime = teamUpdateRequest.getExpireTime();
                Date oldExpireTime = oldTeam.getExpireTime();
                // 将时间都转换为秒级精度进行比较
                long newExpireTimeSeconds = newExpireTime.getTime() / 1000;
                long oldExpireTimeSeconds = oldExpireTime.getTime() / 1000;
                boolean isTimeChanged = newExpireTimeSeconds != oldExpireTimeSeconds;
                if (isTimeChanged) {
                    updateTeam.set("expireTime", teamUpdateRequest.getExpireTime());
                    hasUpdate = true;
                }
            }
        }

        // 8. 处理状态和密码（重点逻辑！！！）
        Integer newTeamStatus = teamUpdateRequest.getStatus();
        Integer oldTeamStatus = oldTeam.getStatus();
        String newTeamPassword = teamUpdateRequest.getPassword();
        // 有状态值
        if (newTeamStatus != null) {
            // 8.1 状态值校验
            TeamStatusEnum newStatusEnum = TeamStatusEnum.getEnumByValue(newTeamStatus);
            if (newStatusEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不合法");
            }
            if (!TeamStatusEnum.getEnumByValue(oldTeamStatus).equals(TeamStatusEnum.getEnumByValue(newTeamStatus))) {
                updateTeam.set("status", newTeamStatus);
                hasUpdate = true;
            }
            // 8.2 加密状态特殊处理
            if (TeamStatusEnum.ENCRYPT.equals(newStatusEnum)) {
                // 情况A：从非加密改为加密 → 必须提供密码
                if (oldTeamStatus != TeamStatusEnum.ENCRYPT.getValue()) {
                    if (StringUtils.isBlank(newTeamPassword)) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密状态必须提供密码");
                    }
                    updateTeam.set("password", newTeamPassword);
                }
                // 情况B：保持加密状态，传了新密码 → 更新密码
                else if (newTeamPassword != null) {
                    if (StringUtils.isBlank(newTeamPassword)) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
                    }
                    if (!newTeamPassword.equals(oldTeam.getPassword())) {
                        updateTeam.set("password", newTeamPassword);
                        hasUpdate = true;
                    }
                }
                // 情况C：保持加密状态，没传密码 → 不用更新密码

            }
            // 8.3 从加密改为非加密 → 清空密码
            else if (TeamStatusEnum.ENCRYPT.equals(TeamStatusEnum.getEnumByValue(oldTeamStatus))) {
                updateTeam.set("password", "");
            }
        }
        // 9. 只更新密码（不更新状态）
        else if (newTeamPassword != null) {
            // 只有加密队伍才能更新密码
            if (oldTeamStatus != TeamStatusEnum.ENCRYPT.getValue()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有加密队伍才能更新密码");
            }
            if (StringUtils.isBlank(newTeamPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
            }
            updateTeam.set("password", newTeamPassword);
            hasUpdate = true;
        }
        // 10. 检查是否有更新
        if (!hasUpdate) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有更新内容");
        }

        return this.update(updateTeam);
    }

    /**
     * 加入队伍
     *
     * @param teamId
     * @param teamPassword
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean joinTeam(Long teamId, String teamPassword, User loginUser) {
        // 无需查库的校验逻辑前置
        // 1. 校验参数
        if (teamId == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验队伍状态
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 3. 校验禁止加入私有的队伍
        if (TeamStatusEnum.PRIVATE.equals(TeamStatusEnum.getEnumByValue(team.getStatus()))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "私有队伍禁止加入");
        }
        // 4. 校验队伍密码
        if (TeamStatusEnum.ENCRYPT.equals(TeamStatusEnum.getEnumByValue(team.getStatus()))) {
            if (StringUtils.isBlank(teamPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已加密");
            }
            if (!team.getPassword().equals(teamPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 查库的操作：
        Long loginUserId = loginUser.getId();
        // 只有一个线程能获取到锁
        String lockKey = "team:join:user:" + loginUserId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    // 5. 校验队伍已加入的人数
                    QueryWrapper<UserTeam> teamHasUser = new QueryWrapper<>();
                    teamHasUser.eq("teamId", teamId);
                    long teamHasUserCount = userTeamService.count(teamHasUser);
                    if (teamHasUserCount >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 6. 校验用户加入的队伍数
                    QueryWrapper<UserTeam> userJoinTeam = new QueryWrapper<>();
                    userJoinTeam.eq("userId", loginUserId);
                    long userJoinTeamCount = userTeamService.count(userJoinTeam);
                    if (userJoinTeamCount >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入和创建的队伍数不能超过5个");
                    }
                    // 7. 校验不能重复加入已加入的队伍（逻辑已包含队伍创建人不能加入自己创建的队伍）
                    QueryWrapper<UserTeam> repeatTeam = new QueryWrapper<>();
                    repeatTeam.eq("teamId", teamId);
                    repeatTeam.eq("userId", loginUserId);
                    long repeatTeamCount = userTeamService.count(repeatTeam);
                    if (repeatTeamCount > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "已加入队伍");
                    }
                    // 8. 插入数据
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(loginUserId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("joinTeam error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 用户退出队伍
     *
     * @param teamId    队伍 id
     * @param loginUser 登录用户
     * @return 退出结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(Long teamId, User loginUser) {
        // 1. 校验参数
        if (teamId == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验队伍
        // 队伍不存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        // 查询用户是否在队伍中
        // SELECT * FROM user_team WHERE team_id = #{teamId} AND user_id = #{userId}
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(loginUser.getId());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        // 队伍人数
        QueryWrapper<UserTeam> hasJoinUserCountWrapper = new QueryWrapper<>();
        hasJoinUserCountWrapper.eq("teamId", teamId);
        long teamHasUserCount = userTeamService.count(hasJoinUserCountWrapper);

        // 3. 处理退出
        // 3.1 只剩一人，队伍解散
        if (teamHasUserCount == 1) {
            this.removeById(teamId);
        } else {
            // 3.2 多人，退出队伍，队长权限顺位转移
            // 情况A：退出用户是队长，顺位转移
            Long currentUserId = loginUser.getId();
            Long teamCreatorId = team.getUserId();
            if (currentUserId.equals(teamCreatorId)) {
                // a. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeam = new QueryWrapper<>();
                userTeam.eq("teamId", teamId);
                userTeam.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeam);
                if (CollUtil.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍人数不足");
                }
                // b. 顺位转移
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍顺位转移失败");
                }
            }
        }
        // 移除关系
        return userTeamService.remove(userTeamQueryWrapper);
    }

    /**
     * 解散队伍
     *
     * @param loginUser 登录用户
     * @return 解散结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean dismissTeam(long teamId, User loginUser) {
        // 1. 校验参数
        if (loginUser == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验队伍
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        // 3. 校验是否为队长
        if (!loginUser.getId().equals(team.getUserId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非队长无法解散队伍");
        }
        // 4. 移除所有人加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 5. 移除队伍信息
        return this.removeById(teamId);
    }
}




