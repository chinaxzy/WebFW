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
public class BranchService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Map> traverseBranch(String fDept){

        Map param = new HashMap();
        String stmt = "branch.selectByPrimaryKey";

        List<Map> branchList = sqlSession.selectList(stmt,param);

        List<Map> tree = new ArrayList<>();

        // 0000, all , 0
//        return buildTree(fDept,branchList,tree);

        return build(fDept, branchList);

    }

    public List<Map> build(String fDept,List<Map> treeNodes) {

        List<Map> trees = new ArrayList<Map>();

        for (Map treeNode : treeNodes) {

            if (fDept.equals(treeNode.get("fId"))) {
                trees.add(treeNode);
            }

            for (Map it : treeNodes) {

                if (it.get("fDept").equals( treeNode.get("fId"))) {
                    if (treeNode.get("subBranch") == null) {
                        treeNode.put("subBranch",new ArrayList<Map>());
                    }
                    List<Map> subBranch = (List<Map>) treeNode.get("subBranch");
                    subBranch.add(it);
                }
            }
        }
        return trees;
    }

    public Object service(Map paramMap) {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        return traverseBranch(String.valueOf(paramMap.get("fId")));

    }

    @Autowired
    private SqlSession sqlSession;
}
