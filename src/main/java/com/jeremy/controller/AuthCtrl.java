package com.jeremy.controller;

import com.jeremy.service.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 2017/2/13.
 */

@RestController
@RequestMapping(value = "/login")
public class AuthCtrl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebService service;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object login(HttpServletRequest request,@RequestParam Map<String, Object> paramMap) {

        Map retMap = new HashMap();
        String token = String.valueOf(request.getAttribute("token"));
        logger.debug("login controller:"+token);
        if (null == token || "".equals(token)) {
            retMap.put("rspCode", "10");
            retMap.put("rspMsg", "登录失败");
        } else {
            paramMap.put("tradeCode","users.selectByPrimaryKey");
            List list = (List) service.dbinvoke(paramMap);
            logger.debug((String) paramMap.get("fId"));
            logger.debug("list.size:"+list.size());
            if(list == null || list.size() == 0){
                retMap.put("rspCode", "11");
                retMap.put("rspMsg", "用户ID不存在");
            }else{
                Map userMap = (Map) list.get(0);

                if(paramMap.get("fPassword").equals(userMap.get("fPassword"))){
                    retMap.put("token",token);
                    retMap.put("rspCode", "00");
                    retMap.put("rspMsg", "交易成功");
                }else{
                    retMap.put("rspCode", "12");
                    retMap.put("rspMsg", "密码错误");
                }
            }
        }
        return retMap;
    }
}
