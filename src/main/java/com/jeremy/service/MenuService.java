package com.jeremy.service;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class MenuService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Object service(Map paramMap) {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        Map param = new HashMap();

        param.put("fId", paramMap.get("fId"));
        List<Map> userroleList = sqlSession.selectList("userrole.selectRoleInfoByUser",param);
        System.out.println("userroleList==>");
        System.out.println(userroleList);
        System.out.println(userroleList.size());

        param.clear();
        param.put("fType", "3");
        List<Map> menuList3 = (List) sqlSession.selectList(tradeCode, param);
        System.out.println(menuList3.size());

        param.put("fType", "2");
        List<Map> menuList2 = (List) sqlSession.selectList(tradeCode, param);
        List<Map> menuList = new ArrayList();

        System.out.println(menuList2.size());
        for (Map menuMap3 : menuList3) {

//            System.out.println("  "+userroleList.get(0).get("fWebright").toString().charAt((Integer) menuMap3.get("menuId")));
            if(userroleList.get(0).get("fWebright").toString().charAt((Integer.parseInt(""+menuMap3.get("menuId")))) == '1'){

                List<Map> subMenu = new ArrayList();

                for (Map menuMap2 : menuList2) {
                    if (menuMap3.get("menuId").equals(menuMap2.get("parentId"))) {

                        System.out.println("    "+userroleList.get(0).get("fWebright").toString().charAt((Integer.parseInt("" + menuMap2.get("menuId")))));
                        if(userroleList.get(0).get("fWebright").toString().charAt((Integer.parseInt(""+ menuMap2.get("menuId")))) == '1'){
                            menuMap2.put("set", new HashMap());
                            subMenu.add(menuMap2);
                        }
                    }
                }
                menuMap3.put("subMenu", subMenu);
                menuList.add(menuMap3);
            }

        }
        System.out.println(menuList.size());
        return menuList;

    }

    @Autowired
    private SqlSession sqlSession;

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public void devtxInsertBatch(Map<String, Object> paramMap) throws Exception {

        int ret = sqlSession.delete("devtx.deleteByPrimaryKey",paramMap);
        logger.debug("devtx.insertBatch",ret);

        ret = sqlSession.insert((String) paramMap.get("tradeCode"),paramMap);
        if(ret <= 0){
            throw new Exception("单台设备业务菜单新增失败");
        }

    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public void devtypetxInsertBatch(Map<String, Object> paramMap) throws Exception {

        int ret = sqlSession.delete("devtypetx.deleteByPrimaryKey",paramMap);
        logger.debug("devtypetx.insertBatch",ret);

        ret = sqlSession.insert((String) paramMap.get("tradeCode"),paramMap);
        if(ret <= 0){
            throw new Exception("设备类型业务菜单新增失败");
        }

    }

    public Object selectByDevJson(Map<String, Object> paramMap) throws Exception {

        List<Map> rootList = sqlSession.selectList("menu.selectByDev",paramMap);

        return selectByDevSub(rootList);

    }

    public Object selectByDevSub(List<Map> list) throws Exception {
        if(list.size() <= 0){
            return list;
        }
        for (Map map : list) {
            Map param  = new HashMap();
            param.putAll(map);
            param.put("fPid",map.get("fId"));
            List subList = sqlSession.selectList("menu.selectByDev",param);
            if(subList.size() > 0){
                map.put("subList",subList);
            }
            selectByDevSub(subList);
        }
        return list;
    }


    public Object selectByDevtypeJson(Map<String, Object> paramMap) throws Exception {

        List<Map> rootList = sqlSession.selectList("menu.selectByDevtype",paramMap);

        return selectByDevtypeSub(rootList);

    }

    public Object selectByDevtypeSub(List<Map> list) throws Exception {
        if(list.size() <= 0){
            return list;
        }
        for (Map map : list) {
            Map param  = new HashMap();
            param.putAll(map);
            param.put("fPid",map.get("fId"));
            List subList = sqlSession.selectList("menu.selectByDevtype",param);
            if(subList.size() > 0){
                map.put("subList",subList);
            }
            selectByDevtypeSub(subList);
        }
        return list;
    }


}
