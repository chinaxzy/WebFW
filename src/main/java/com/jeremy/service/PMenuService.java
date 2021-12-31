package com.jeremy.service;

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
public class PMenuService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    // 查询某菜单及其下级菜单
    public List<Map> traverseBranch(String fId,String fDept){
        Map param = new HashMap();
        String stmt = "pmenu.selectSimplePMenu";
        param.put("fId",fId);
        param.put("fDept",fDept);

        // 查本机构id
        List<Map> rootBranchList = sqlSession.selectList(stmt,param);
        if(rootBranchList.size() > 0)
        param.remove("fId");

        List<Map> branchList = new ArrayList<Map>();

        // 查子机构
        for (Map branchMap : rootBranchList) {
            param.put("fPid",fId);

            // 查询所有子机构
            List<Map> subBranchList = (List) sqlSession.selectList(stmt,param);

            // 遍历子机构
            List traverseList = new ArrayList();
            for (Map subMap : subBranchList) {

                // 查询孙机构
                List<Map> subList = traverseBranch(String.valueOf(subMap.get("fId")),fDept);
                if(subList.size()>0){
                    traverseList.add(subList.get(0));
                }
                branchMap.put("subMenu",traverseList);

            }
            branchList.add(branchMap);
        }

        return branchList;
    }

    // 查询某级菜单的下级菜单，不包含自己
    public List<Map> traverseBranchP(String fId,String fDept){
        Map param = new HashMap();
        String stmt = "pmenu.selectSimplePMenu";
        param.put("fPid",fId);
        param.put("fDept",fDept);

        // 查root机构list
        List<Map> rootBranchList = sqlSession.selectList(stmt,param);

        List<Map> branchList = new ArrayList<Map>();

        // 查子机构
        for (Map branchMap : rootBranchList) {

            branchMap.put("subMenu",traverseBranchP(String.valueOf(branchMap.get("fId")),fDept));
            branchList.add(branchMap);
        }

        return branchList;
    }

    public Object service(Map paramMap) {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);
        return traverseBranchP((String) paramMap.get("fId"), (String)paramMap.get("fDept"));

    }

    @Autowired
    private SqlSession sqlSession;
}
