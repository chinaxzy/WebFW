package com.jeremy.service;

import com.jeremy.util.RandomCharacter;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Jeremy on 28/03/2017.
 */
@Service
public class DevService {

    @Autowired
    private WebService webService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object startDev(Map paramMap) throws Exception {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        String fDevid = String.valueOf(paramMap.get("fId"));
        String fType = (String) paramMap.get("fType");

        List<Map> dev = sqlSession.selectList("dev.selectByPrimaryKey",paramMap);
        int fWindcnt = 0;

        if("0".equals(dev.get(0).get("fUsestat"))){
            throw new Exception("设备已启用，请刷新页面");
        }

        try {
            fWindcnt = (int) dev.get(0).get("fWindcnt");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        sqlSession.delete("devstate.deleteByPrimaryKey",paramMap);


        for (int i = 0; i < 50; i++) {
            String datetime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String loopbox = "0";
            String stateid = "";

            paramMap.put("fId",fType);
            List<Map> devTypeList = sqlSession.selectList("devtype.selectByPrimaryKey",paramMap);
            if(1 != devTypeList.size()){
                throw new Exception("devtype select error");
            }
            Map devTypeMap = devTypeList.get(0);
            String fState = String.valueOf(devTypeMap.get("fParts")).trim();
            String fDevtype = String.valueOf(devTypeMap.get("fType")).trim();

            stateid = Integer.toString(Integer.parseInt(fDevtype) * 1000 + i + 1);
            if (Integer.parseInt(stateid) > 1002 && Integer.parseInt(stateid) <= 1010) {
                loopbox = "1";
            }

            // PDJ, 20~50,


            if ("PDJ".equals(fType)){
                if ((i < 30 && i >= 20+fWindcnt) || (i < 40 && i >= 30+fWindcnt) || (i < 50 && i >= 40+fWindcnt)){
                    logger.debug("PDJ::"+i);
                }else if(i >20 && i < 50) {

                    paramMap.put("fDevid",fDevid);
                    paramMap.put("fStateid",stateid);
                    paramMap.put("fValue",0);
                    paramMap.put("fLoopbox",loopbox);
                    paramMap.put("fCntmax",0);
                    paramMap.put("fCount",0);
                    paramMap.put("fFlag","0");
                    paramMap.put("fMdn",0);
                    paramMap.put("fWtype",0);
                    paramMap.put("fWbroad","");
                    paramMap.put("fEbroad","");
                    paramMap.put("fErr","");
                    paramMap.put("fDtime",datetime);
                    paramMap.put("fPad1","");
                    sqlSession.insert("devstate.insertSelective",paramMap);

                }else {

                    if(fState.substring(i, i + 1).equals("1")) {

                        paramMap.put("fDevid",fDevid);
                        paramMap.put("fStateid",stateid);
                        paramMap.put("fValue",0);
                        paramMap.put("fLoopbox",loopbox);
                        paramMap.put("fCntmax",0);
                        paramMap.put("fCount",0);
                        paramMap.put("fFlag","0");
                        paramMap.put("fMdn",0);
                        paramMap.put("fWtype",0);
                        paramMap.put("fWbroad","");
                        paramMap.put("fEbroad","");
                        paramMap.put("fErr","");
                        paramMap.put("fDtime",datetime);
                        paramMap.put("fPad1","");
                        sqlSession.insert("devstate.insertSelective",paramMap);

                    }

                }
            }else {


                if(fState.substring(i, i + 1).equals("1")){

                    paramMap.put("fDevid",fDevid);
                    paramMap.put("fStateid",stateid);
                    paramMap.put("fValue",0);
                    paramMap.put("fLoopbox",loopbox);
                    paramMap.put("fCntmax",0);
                    paramMap.put("fCount",0);
                    paramMap.put("fFlag","0");
                    paramMap.put("fMdn",0);
                    paramMap.put("fWtype",0);
                    paramMap.put("fWbroad","");
                    paramMap.put("fEbroad","");
                    paramMap.put("fErr","");
                    paramMap.put("fDtime",datetime);
                    paramMap.put("fPad1","");
                    sqlSession.insert("devstate.insertSelective",paramMap);

                }
            }

        }
        paramMap.clear();
        paramMap.put("fId",fDevid);
        paramMap.put("fUsestat",0);
        sqlSession.update("dev.updateByPrimaryKeySelective",paramMap);


        paramMap.clear();
        paramMap.put("fSection","EBServer");
        paramMap.put("fKey","DataMask");
        String fValue = sqlSession.selectOne("syscfg.selectKeyBySection",paramMap);
        char first = fValue.charAt(0);
        paramMap.put("fValue", getRandomWithoutRepeat(first)+fValue.substring(1,fValue.length()));
        sqlSession.update("syscfg.updateByPrimaryKeySelective",paramMap);
        return 1;

    }

    private char getRandomWithoutRepeat(char c){
        char ret = RandomCharacter.getRandomUpperCaseLetter();
        if( ret == c){
            return getRandomWithoutRepeat(c);
        }else{
            return ret;
        }
    }


    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object stopDev(Map paramMap) throws Exception{
        String fDevid = String.valueOf(paramMap.get("fId"));
        sqlSession.delete("devstate.deleteByPrimaryKey",paramMap);
        paramMap.clear();
        paramMap.put("fId",fDevid);
        paramMap.put("fUsestat",2);
        sqlSession.update("dev.updateByPrimaryKeySelective",paramMap);

        paramMap.clear();
        paramMap.put("fSection","EBServer");
        paramMap.put("fKey","DataMask");
        String fValue = sqlSession.selectOne("syscfg.selectKeyBySection",paramMap);
        char first = fValue.charAt(0);
        paramMap.put("fValue", getRandomWithoutRepeat(first)+fValue.substring(1,fValue.length()));
        sqlSession.update("syscfg.updateByPrimaryKeySelective",paramMap);

        return 1;
    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object deleteDev(Map paramMap) throws Exception{

        sqlSession.delete("dev.deleteByPrimaryKey",paramMap);
        sqlSession.delete("devstate.deleteByPrimaryKey",paramMap);

        return 1;
    }

    public Object selectDevStateWithDetail(Map paramMap) throws Exception{

        List<Map> devList = sqlSession.selectList("dev.selectDevListWithState",paramMap);
        for (Map map : devList) {
            String fId = (String) map.get("fId");
            Map param = new HashMap();
            param.put("fId",fId);
            map.put("details", sqlSession.selectList("devstate.selectDevStateDetail",param));
        }

        return devList;
    }

    public Object selectDevCountWithType(Map paramMap) throws Exception{

        List<Map> typeList = sqlSession.selectList("devtype.selectByPrimaryKey");
        List<Map> facList = sqlSession.selectList("factory.selectByPrimaryKey");
        List<Map> devCountList = sqlSession.selectList("dev.selectDevCountWithType",paramMap);

        Map KV = new HashMap();
        for (Map map : devCountList) {
            logger.debug("put:"+map.get("fType")+"-"+map.get("fMark"));
            logger.debug(""+ map.get("count"));
            KV.put(map.get("fType")+"-"+map.get("fFacid"),map.get("count"));
        }

        List typeId = new ArrayList();
        List typeName = new ArrayList();
        List facId = new ArrayList();
        List facName = new ArrayList();

        for (Map type : typeList) {
            typeId.add(type.get("fId"));
        }

        for (Map type : typeList) {
            typeName.add(type.get("fName"));
        }

        for (Map fac : facList) {
            facId.add(fac.get("fId"));
        }

        for (Map fac : facList) {
            facName.add(fac.get("fName"));
        }

        List c1List = new ArrayList();
        for (Map fac : facList) {
            List c2List = new ArrayList();

            for (Map type : typeList) {
                logger.debug("key:"+type.get("fId")+"-"+fac.get("fId"));
                logger.debug("value:"+KV.get(type.get("fId")+"-"+fac.get("fId")));
                if( null == KV.get(type.get("fId")+"-"+fac.get("fId"))){
                    c2List.add(0);
                }else {
                    c2List.add(KV.get(type.get("fId")+"-"+fac.get("fId")));
                }
                logger.debug(type.get("fId")+"-"+fac.get("fId"),KV.get(type.get("fId")+"-"+fac.get("fId")));
            }
            c1List.add(c2List);
        }

        Map retMap = new HashMap();
        retMap.put("type",typeName);
        retMap.put("fac",facName);
        retMap.put("data",c1List);
        return retMap;
    }

    @Autowired
    private SqlSession sqlSession;

    public void insert(Map<String, Object> paramMap) throws Exception {

        if(paramMap.get("fIp") != null){
            Map param = new HashMap();
            param.put("tradeCode","dev.selectByPrimaryKey");
            param.put("fIp", paramMap.get("fIp"));

            List<Map> devList = (List<Map>) webService.dbinvoke(param);
            if(devList.size() > 0){
                throw new Exception(paramMap.get("fIp")+"已使用，请重新分配ip");
            }
        }

        int ret = (int) webService.dbinvoke(paramMap);
        if(ret != 1){
            throw new Exception("操作失败，请检查参数");
        }

    }
}
