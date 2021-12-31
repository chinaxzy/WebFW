package com.jeremy.service;

import com.jeremy.util.DateUtils;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class UploadService {

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
        String maxid = sqlSession.selectOne("soft.selectMaxId", param);
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
        paramMap.put("fDtime",sDateSuffix);

        String relativePath = (String) paramMap.get("filePath");
        String fileName = (String) paramMap.get("fileName");

        HashMap pathMap = new HashMap();
        pathMap.put("fSection","SYS");
        pathMap.put("fKey","Tmppath");
        String tmpPath = sqlSession.selectOne("syscfg.selectKeyBySection",pathMap);
        File tmpPathFile = new File(tmpPath+fileName);

        pathMap = new HashMap();
        pathMap.put("fSection","Agent");
        pathMap.put("fKey","ftpbase");
        String serverPath = sqlSession.selectOne("syscfg.selectKeyBySection",pathMap);

        String fDevtype = (String) paramMap.get("fDevtype");
        String fDevmark = (String) paramMap.get("fDevmark");
        String fDevspc = (String) paramMap.get("fDevspc");
        String fVer = (String) paramMap.get("fVer");




        String rulePath = fDevtype+"_"+ getPinYin(fDevmark)+File.separator;
        String rulePathWin = fDevtype+"_"+ getPinYin(fDevmark)+"\\";
        if(fDevspc != null){
            rulePath += fDevspc + File.separator;
            rulePathWin += fDevspc + "\\";
        }
        rulePath += fVer + File.separator;
        rulePathWin += fVer + "\\";

        String serverFilePath = serverPath + rulePath;

        new File(serverFilePath).mkdirs();
        File serverPathFile = new File(serverFilePath+fileName);

        FileUtil.nioTransferCopy(tmpPathFile, serverPathFile);

        paramMap.put("fFile",rulePathWin+fileName);
        return sqlSession.insert(tradeCode,paramMap);

    }

    public String getPinYin(String src) {
        char[] t1 = null;
        t1 = src.toCharArray();
        String[] t2 = new String[t1.length];
        // 设置汉字拼音输出的格式
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        String t4 = "";
        int t0 = t1.length;
        try {
            for (int i = 0; i < t0; i++) {
                // 判断是否为汉字字符
                if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                    t4 += t2[0];
                } else {
                    t4 += Character.toString(t1[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            System.out.println("传递的表达式无效...");
            e.printStackTrace();
        }catch (NullPointerException e) {
            System.out.println("没有找到匹配...");
            e.printStackTrace();
        }
        return t4;
    }

    public Object todo(Map paramMap) throws Exception {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
        String sDateSuffix = dateformat.format(date);

        HashMap param = new HashMap();
        param.put("fDate",sDateSuffix);
        String maxId = sqlSession.selectOne("todo.selectMaxId", param);
        System.out.println(maxId);

        if(maxId == null){
            maxId = "1";
        }
        int maxIdInt = Integer.valueOf(maxId);
        System.out.println(maxId);



        if(paramMap.get("fDtime") == null || "".equals(paramMap.get("fDtime"))){
            dateformat = new SimpleDateFormat("yyyyMMddHHmmdd");
            sDateSuffix = dateformat.format(date);
            paramMap.put("fDtime",sDateSuffix);
            paramMap.put("fNexttime",sDateSuffix);
        }

        String fSoftid = (String) paramMap.get("fSoftid");
        String fAction = (String) paramMap.get("fAction");
        param = new HashMap();
        param.put("fId",fSoftid);
        List<Map> softList = sqlSession.selectList("soft.selectByPrimaryKey",param);
        if(softList.size() != 1 && "101".equals(fAction)){
            throw new Exception("软件包ID数据有误，请检查");
        }else{
            paramMap.put("fFile",softList.get(0).get("fFile"));
            paramMap.put("fZipfg",softList.get(0).get("fZipfg"));
        }

        paramMap.put("fDeftime",sDateSuffix);
        paramMap.put("fTrymax",5);
        paramMap.put("fDone",0);
        paramMap.put("fRcinfo","等待执行");

        String str_fDelay = (String) paramMap.get("fDelay");

        int fDelay = 0;

        if(str_fDelay != null && !"".equals(str_fDelay)){
            fDelay = Integer.parseInt(str_fDelay);
        }

        String devIdArrayStr = (String) paramMap.get("fDevidList");
        if(devIdArrayStr != null){
            String[] devIdArray = devIdArrayStr.split(",");
            for (String devid : devIdArray) {
                paramMap.put("fDevid",devid);
                paramMap.put("fId",++maxIdInt);


                int ret = sqlSession.insert(tradeCode,paramMap);

                if(ret != 1){
                    throw new Exception("添加任务失败");
                }

                if(fDelay != 0){
                    paramMap.put("fDtime",DateUtils.addMinuteToDate(fDelay, (String) paramMap.get("fDtime"),"yyyymmddHHmmss"));
                }
            }
        }

        return 1;

    }

    public String getTmpPath(){
        HashMap pathMap = new HashMap();
        pathMap.put("fSection","SYS");
        pathMap.put("fKey","Tmppath");
        String tmpPath = sqlSession.selectOne("syscfg.selectKeyBySection",pathMap);
        System.out.println("Tmppath:"+tmpPath);
        return tmpPath;
    }

    @Autowired
    private SqlSession sqlSession;
}
