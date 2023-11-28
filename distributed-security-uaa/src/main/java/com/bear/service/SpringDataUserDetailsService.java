package com.bear.service;

import com.bear.dao.UserDao;
import com.bear.entity.User;
import com.bear.userdetailmodel.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class SpringDataUserDetailsService implements UserDetailsService {

    @Autowired
    UserDao userDao;

    // todo 黑马的重写loadUserByUsername的方法
    //根据 账号查询用户信息
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        //将来连接数据库根据账号查询用户信息
//        UserDto userDto = userDao.getUserByUsername(username);
//        if(userDto == null){
//            //如果用户查不到，返回null，由provider来抛出异常
//            return null;
//        }
//        //根据用户的id查询用户的权限
//        List<String> permissions = userDao.findPermissionsByUserId(userDto.getId());
//        //将permissions转成数组
//        String[] permissionArray = new String[permissions.size()];
//        permissions.toArray(permissionArray);
//        //将userDto转成json
//        String principal = JSON.toJSONString(userDto);
//        UserDetails userDetails = User.withUsername(principal).password(userDto.getPassword()).authorities(permissionArray).build();
//        return userDetails;
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        //根据用户名查询用户信息
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(User::getUserName,username);
//        User user = userMapper.selectOne(wrapper);
        // todo 假设从数据库中查询到了这一条数据，使用用户名
        // todo 使用BCryptPasswordEncoder加密， 密码$2a$10$UZKtX2iQQwr6LHiywe6nquFczaxzXHgIM5VUCDfEwYDDO.Vla5vgq
        User user = new User();
        user
                .setId(1L)
                .setUserName(username)
                .setNickName("测试使用")
                .setPassword("$2a$10$UZKtX2iQQwr6LHiywe6nquFczaxzXHgIM5VUCDfEwYDDO.Vla5vgq")
                .setStatus("1")
                .setEmail("1223234@qq.com")
                .setPhonenumber("1212121212");
        //如果查询不到数据就通过抛出异常来给出提示
        if(Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误");
        }
        //TODO 根据用户查询权限信息 添加到LoginUser中
        //封装成UserDetails对象返回
        List<String> permissions = new ArrayList<>();
        permissions.add("test");
        return new LoginUser(user, permissions);
    }
}
