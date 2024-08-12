package org.jeecg.modules.coderQ.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.coderQ.entity.FileInfo;
import org.jeecg.modules.coderQ.service.IFileInfoService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

/**
 * @Description: file_info
 * @Author: 公众号：程序员辉哥 (技术答疑,项目分享)
 * @Date: 2023-04-27
 * @Version: V1.0
 */
@Api(tags = "file_info")
@RestController
@RequestMapping("/coderQ/fileInfo")
@Slf4j
public class FileInfoController {
    @Autowired
    private IFileInfoService fileInfoService;

    /**
     * 分页列表查询
     *
     * @param fileInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "file_info-分页列表查询")
    @ApiOperation(value = "file_info-分页列表查询", notes = "file_info-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(FileInfo fileInfo,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(fileInfo.getFilename()), FileInfo::getFilename, fileInfo.getFilename());
        queryWrapper.eq(FileInfo::getCreateBy, loginUser.getUsername());
        queryWrapper.orderByDesc(FileInfo::getCreateTime);
        Page<FileInfo> page = new Page<FileInfo>(pageNo, pageSize);
        IPage<FileInfo> pageList = fileInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param fileInfo
     * @return
     */
    @AutoLog(value = "file_info-添加")
    @ApiOperation(value = "file_info-添加", notes = "file_info-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody FileInfo fileInfo) {
        fileInfoService.save(fileInfo);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param fileInfo
     * @return
     */
    @AutoLog(value = "file_info-编辑")
    @ApiOperation(value = "file_info-编辑", notes = "file_info-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody FileInfo fileInfo) {
        fileInfoService.updateById(fileInfo);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "file_info-通过id删除")
    @ApiOperation(value = "file_info-通过id删除", notes = "file_info-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        fileInfoService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "file_info-批量删除")
    @ApiOperation(value = "file_info-批量删除", notes = "file_info-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.fileInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "file_info-通过id查询")
    @ApiOperation(value = "file_info-通过id查询", notes = "file_info-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        FileInfo fileInfo = fileInfoService.getById(id);
        if (fileInfo == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(fileInfo);
    }

    /**
     * 修改数据状态
     */
    @GetMapping("/updateStatus")
    public Result<?> updateStatus(String id, String dataZt, String field) {
        UpdateWrapper<FileInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(FileInfo::getId, id);
        updateWrapper.set(field, dataZt);
        fileInfoService.update(updateWrapper);
        return Result.OK();
    }


    @GetMapping(value = "/getPubList")
    public Result<?> getPubList(FileInfo fileInfo,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(fileInfo.getFilename()), FileInfo::getFilename, fileInfo.getFilename());
        queryWrapper.like(StrUtil.isNotBlank(fileInfo.getCreateBy()), FileInfo::getCreateBy, fileInfo.getCreateBy());
        queryWrapper.eq(FileInfo::getShare, 1);
        queryWrapper.orderByDesc(FileInfo::getCreateTime);
        Page<FileInfo> page = new Page<FileInfo>(pageNo, pageSize);
        IPage<FileInfo> pageList = fileInfoService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
}
