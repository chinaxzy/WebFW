package com.jeremy.service;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class WindquetypeService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object service(Map paramMap) throws Exception {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        // 查询
        if(tradeCode.endsWith("selectWindquetype")){
            return sqlSession.selectList(tradeCode,paramMap);
        }

        // 新增 || 更新
        if(tradeCode.endsWith("insertWindquetype") || tradeCode.endsWith("updateWindquetype")){

            sqlSession.delete("windquetype.deleteByPrimaryKey",paramMap);

            String windqueList = String.valueOf(paramMap.get("fQuetypeList"));
            String[] windqueArray = windqueList.split("[,]");
            logger.debug("windque:"+Arrays.asList(windqueArray).size());
            paramMap.put("fQuetypeList", Arrays.asList(windqueArray));
            return sqlSession.insert("windquetype.insertWindquetype",paramMap);
        }

        // 删除
        if(tradeCode.endsWith("deleteWindquetype")){
            return sqlSession.delete("windquetype.deleteByPrimaryKey",paramMap);
        }

        return "";

    }

    @Autowired
    private SqlSession sqlSession;
}
