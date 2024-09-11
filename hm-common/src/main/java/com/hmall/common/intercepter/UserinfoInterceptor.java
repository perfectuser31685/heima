package com.hmall.common.intercepter;

import cn.hutool.core.util.StrUtil;
import com.ctc.wstx.util.StringUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserinfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取用户信息,String类型是因为过滤器传递的就是String类型
        String userinfo = request.getHeader("user-info");
        //存入ThreadLocal
        if(StrUtil.isNotBlank(userinfo)){
            UserContext.setUser(Long.valueOf(userinfo));
        }
        //放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
