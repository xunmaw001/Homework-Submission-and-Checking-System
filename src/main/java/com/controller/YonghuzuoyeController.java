package com.controller;


import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;

import com.entity.LaoshiEntity;
import com.service.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;

import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.YonghuzuoyeEntity;

import com.entity.view.YonghuzuoyeView;
import com.entity.YonghuEntity;
import com.entity.ZuoyeEntity;

import com.utils.PageUtils;
import com.utils.R;

/**
 * 回答作业
 * 后端接口
 * @author
 * @email
 * @date 2021-04-22
*/
@RestController
@Controller
@RequestMapping("/yonghuzuoye")
public class YonghuzuoyeController {
    private static final Logger logger = LoggerFactory.getLogger(YonghuzuoyeController.class);

    @Autowired
    private YonghuzuoyeService yonghuzuoyeService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;



    //级联表service
    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private ZuoyeService zuoyeService;
    @Autowired
    private LaoshiService laoshiService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
     
        String role = String.valueOf(request.getSession().getAttribute("role"));

        if("用户".equals(role)){// 用户只能查看自己的作业
//            YonghuEntity yonghuEntity = yonghuService.selectById(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
//            params.put("xueyuanzhuanyeTypes",yonghuEntity.getXueyuanzhuanyeTypes());
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }else if( "老师".equals(role)){ // 老师只能查看自己专业的作业
            LaoshiEntity laoshiEntity = laoshiService.selectById(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
            params.put("xueyuanzhuanyeTypes",laoshiEntity.getXueyuanzhuanyeTypes());

        }
        params.put("orderBy","id");
        PageUtils page = yonghuzuoyeService.queryPage(params);

        //字典表数据转换
        List<YonghuzuoyeView> list =(List<YonghuzuoyeView>)page.getList();
        for(YonghuzuoyeView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YonghuzuoyeEntity yonghuzuoye = yonghuzuoyeService.selectById(id);
        if(yonghuzuoye !=null){
            //entity转view
            YonghuzuoyeView view = new YonghuzuoyeView();
            BeanUtils.copyProperties( yonghuzuoye , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(yonghuzuoye.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //级联表
            ZuoyeEntity zuoye = zuoyeService.selectById(yonghuzuoye.getZuoyeId());
            if(zuoye != null){
                BeanUtils.copyProperties( zuoye , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setZuoyeId(zuoye.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody YonghuzuoyeEntity yonghuzuoye, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,yonghuzuoye:{}",this.getClass().getName(),yonghuzuoye.toString());
        Wrapper<YonghuzuoyeEntity> queryWrapper = new EntityWrapper<YonghuzuoyeEntity>()
            .eq("zuoye_id", yonghuzuoye.getZuoyeId())
            .eq("yonghu_id", yonghuzuoye.getYonghuId())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuzuoyeEntity yonghuzuoyeEntity = yonghuzuoyeService.selectOne(queryWrapper);
        if(yonghuzuoyeEntity==null){
            yonghuzuoye.setInsertTime(new Date());
            yonghuzuoye.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      yonghuzuoye.set
        //  }
            yonghuzuoyeService.insert(yonghuzuoye);
            return R.ok();
        }else {
            return R.error(511,"该用户已经做了该作业了");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody YonghuzuoyeEntity yonghuzuoye, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,yonghuzuoye:{}",this.getClass().getName(),yonghuzuoye.toString());
        //根据字段查询是否有相同数据
        Wrapper<YonghuzuoyeEntity> queryWrapper = new EntityWrapper<YonghuzuoyeEntity>()
            .notIn("id",yonghuzuoye.getId())
            .andNew()
            .eq("zuoye_id", yonghuzuoye.getZuoyeId())
            .eq("yonghu_id", yonghuzuoye.getYonghuId())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuzuoyeEntity yonghuzuoyeEntity = yonghuzuoyeService.selectOne(queryWrapper);
        if("".equals(yonghuzuoye.getYonghuzuoyeFile()) || "null".equals(yonghuzuoye.getYonghuzuoyeFile())){
                yonghuzuoye.setYonghuzuoyeFile(null);
        }
        if(yonghuzuoyeEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      yonghuzuoye.set
            //  }
            yonghuzuoyeService.updateById(yonghuzuoye);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"该用户已经做了该作业了");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        yonghuzuoyeService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }






}

