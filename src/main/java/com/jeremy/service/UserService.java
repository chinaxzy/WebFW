package com.jeremy.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebService webService;


    public Object service(Map paramMap) throws Exception {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        List<Map> userList = sqlSession.selectList(tradeCode,paramMap);

        if(userList.size() > 1){
            throw new Exception("后台数据有误");
        }else if (userList.size() == 0){
            throw new Exception("用户ID不存在");
        }else if (userList.size() == 1){
            logger.info("userList.get(0).get(\"fCanlogin\")="+userList.get(0).get("fCanlogin"));
            if(!"1".equals(userList.get(0).get("fCanlogin"))){
                throw new Exception("该用户不能登录，请联系管理员");
            }

            if(!userList.get(0).get("fPasswd").equals(paramMap.get("fPasswd"))){
                throw new Exception("密码错误");
            }
        }
        userList.get(0).remove("fPasswd");


        String token = Jwts.builder().setSubject((String) paramMap.get("fId"))
            .claim("roles", paramMap.get("fId")).setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, "secretkey").compact();

        Map param = new HashMap();
        param.put("tradeCode","users.updateByPrimaryKeySelective");
        param.put("fId",paramMap.get("fId"));
        param.put("fToken",token);
        int ret = (int) webService.dbinvoke(param);
        if(ret != 1){
            throw new Exception("token保存失败");
        }

        userList.get(0).put("token",token);

        return userList;

    }

    @Autowired
    private SqlSession sqlSession;
}
