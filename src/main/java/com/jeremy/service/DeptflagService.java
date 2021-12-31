package com.jeremy.service;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class DeptflagService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Object service(Map paramMap) throws Exception {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        String tradeCodeList = (String) paramMap.get("tradeCodeList");

        Map param = new HashMap();
        param.put("fDept",paramMap.get("fDept"));
        param.put("fModified","1");

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        param.put("fLmtime",df.format(new Date()));
        if("branch.insertSelective".equals(tradeCode)){
            param.put("fDept",paramMap.get("fId"));
            sqlSession.insert("deptflag.insertSelective",param);
        }

        if(tradeCodeList != null &&
                ("wind.insertSelective,windquetype.insertWindquetype".equals(tradeCodeList)
        || "wind.deleteByPrimaryKey,windquetype.deleteByPrimaryKey".equals(tradeCodeList))){
            sqlSession.insert("deptflag.updateByPrimaryKeySelective",param);
        }

        if("wind.updateByPrimaryKeySelective".equals(tradeCode)
                ||"windquetype.insertSelective".equals(tradeCode)
                ||"windquetype.deleteByPrimaryKey".equals(tradeCode)
                ||"windquetype.updateByPrimaryKeySelective".equals(tradeCode)){
            sqlSession.insert("deptflag.updateByPrimaryKeySelective",param);
        }

        return 1;

    }

    @Autowired
    private SqlSession sqlSession;
}
