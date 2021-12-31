package com.jeremy.service;

import com.jeremy.util.FileUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class AdvertiseService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${upload.url}")
    private String uploadUrl;

    public Object service(Map paramMap) {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
        String sDateSuffix = dateformat.format(date);

        HashMap param = new HashMap();
        param.put("fDate",sDateSuffix);
        String maxid = sqlSession.selectOne("advertise.selectMaxId", param);
        System.out.println(maxid);

        if(maxid != null){
            maxid = String.format("%03d", Integer.parseInt(maxid)+1);
        }else{
            maxid = "001";
        }
        System.out.println(maxid);


        paramMap.put("fId",sDateSuffix+"-"+maxid);

        dateformat = new SimpleDateFormat("yyyyMMddHHmmdd");
        sDateSuffix = dateformat.format(date);
        paramMap.put("fUpdtime",sDateSuffix);

        String fileName = (String) paramMap.get("fileName");
        String fileExt = fileName.substring(fileName.indexOf("."),fileName.length());

        HashMap pathMap = new HashMap();
        pathMap.put("fSection","SYS");
        pathMap.put("fKey","Tmppath");
        String tmpPath = sqlSession.selectOne("syscfg.selectKeyBySection",pathMap);
        File tmpPathFile = new File(tmpPath+fileName);

        pathMap = new HashMap();
        pathMap.put("fSection","SYS");
        pathMap.put("fKey","Advpath");
        String serverPath = sqlSession.selectOne("syscfg.selectKeyBySection",pathMap);

        String serverFilePath = serverPath;

        String relativePath = paramMap.get("fDept")+File.separator+paramMap.get("fDevtype")+File.separator+paramMap.get("fScrntp")+File.separator;

        new File(serverFilePath+relativePath).mkdirs();
        File serverPathFile = new File(serverFilePath +relativePath + paramMap.get("fId") +fileExt);

        FileUtil.nioTransferCopy(tmpPathFile, serverPathFile);

        paramMap.put("fFile",paramMap.get("fId")+fileExt);
        return sqlSession.insert(tradeCode,paramMap);

    }

    public Object update(Map paramMap){
        String tradeCode = (String) paramMap.get("tradeCode");
        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmdd");
        String sDateSuffix = dateformat.format(date);
        paramMap.put("fUpdtime",sDateSuffix);


        String fileName = (String) paramMap.get("fileName");
        if(fileName != null && !"".equals(fileName)){
            String fileExt = fileName.substring(fileName.indexOf("."),fileName.length());

            HashMap pathMap = new HashMap();
            pathMap.put("fSection","SYS");
            pathMap.put("fKey","Tmppath");
            String tmpPath = sqlSession.selectOne("syscfg.selectKeyBySection",pathMap);
            File tmpPathFile = new File(tmpPath+fileName);

            pathMap = new HashMap();
            pathMap.put("fSection","SYS");
            pathMap.put("fKey","Advpath");
            String serverPath = sqlSession.selectOne("syscfg.selectKeyBySection",pathMap);

            String serverFilePath = serverPath;

            new File(serverFilePath).mkdirs();
            File serverPathFile = new File(serverFilePath + paramMap.get("ofId") +fileExt);
            if(serverPathFile.exists()){
                serverPathFile.delete();
            }
            FileUtil.nioTransferCopy(tmpPathFile, serverPathFile);

            paramMap.put("fFile",paramMap.get("ofId")+fileExt);
        }

        return sqlSession.update(tradeCode,paramMap);
    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object delete(Map paramMap) throws Exception {
        String tradeCode = (String) paramMap.get("tradeCode");
        List<Map> advertiseList = sqlSession.selectList("advertise.selectByPrimaryKey",paramMap);

        if(advertiseList.size() == 0 || advertiseList.size() > 1){
            throw new Exception("未查询到广告");
        }
        Map advertise = advertiseList.get(0);
        if(advertiseList.size() == 1){
            Map param = new HashMap();
            param.put("fDept",advertise.get("fDept"));
            param.put("fDevtype",advertise.get("fDevtype"));
            param.put("fScrntp",advertise.get("fScrntp"));

            Date date = new Date();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmdd");
            String sDateSuffix = dateformat.format(date);
            param.put("fUpdtime",sDateSuffix);

            sqlSession.update("advertise.updateWhenDelete",param);

        }
        return sqlSession.update(tradeCode,paramMap);
    }
    @Autowired
    private SqlSession sqlSession;
}
