package com.ping.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ping.usercenter.common.BaseResponse;
import com.ping.usercenter.common.ErrorCode;
import com.ping.usercenter.common.ResultUtils;
import com.ping.usercenter.exception.BusinessException;
import com.ping.usercenter.model.domain.User;
import com.ping.usercenter.model.domain.request.UserLoginRequest;
import com.ping.usercenter.model.domain.request.UserRegisterRequest;
import com.ping.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.ping.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author ping
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = { "http://user.qiutang.icu"}, allowCredentials =
// "true")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getUserPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword,
                checkPassword, planetCode);
        return ResultUtils.success(result);
    }


    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                        HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        //先拿到用户态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //对于用户信息频繁变化的场景来说建议查库
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId); //查数据库
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,
                                                HttpServletRequest request) {

        // 鉴权 - 仅管理员可查询
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username); // like 允许包含
        }
        List<User> userList = userService.list(queryWrapper);
        //过滤密码
        // Java8 遍历userList的每一个元素，把元素密码设置为空再拼成list返回
        List<User> list = userList.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTagsBySQL(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageNum, long pageSize
                                                   , HttpServletRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage =
                userService.page(new Page<>(pageNum,pageSize), queryWrapper);
        return ResultUtils.success(userPage);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,
                                            HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 鉴权
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,
                                            HttpServletRequest request) {
        // 鉴权 - 仅管理员可查询
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

}
