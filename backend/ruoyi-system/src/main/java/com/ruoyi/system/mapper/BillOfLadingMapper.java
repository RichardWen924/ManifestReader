package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BillOfLading;

/**
 * 提单信息Mapper接口
 * 
 * @author Richard
 * @date 2026-01-29
 */
public interface BillOfLadingMapper {
    /**
     * 查询提单信息
     * 
     * @param id 提单信息主键
     * @return 提单信息
     */
    public BillOfLading selectBillOfLadingById(Long id);

    /**
     * 根据提单号查询
     * 
     * @param blNo 提单号
     * @return 提单信息
     */
    public BillOfLading selectBillOfLadingByBlNo(String blNo);

    /**
     * 查询提单信息列表
     * 
     * @param billOfLading 提单信息
     * @return 提单信息集合
     */
    public List<BillOfLading> selectBillOfLadingList(BillOfLading billOfLading);

    /**
     * 新增提单信息
     * 
     * @param billOfLading 提单信息
     * @return 结果
     */
    public int insertBillOfLading(BillOfLading billOfLading);

    /**
     * 修改提单信息
     * 
     * @param billOfLading 提单信息
     * @return 结果
     */
    public int updateBillOfLading(BillOfLading billOfLading);

    /**
     * 删除提单信息
     * 
     * @param id 提单信息主键
     * @return 结果
     */
    public int deleteBillOfLadingById(Long id);

    /**
     * 批量删除提单信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBillOfLadingByIds(String[] ids);

    public int checkDocNoUnique(@org.apache.ibatis.annotations.Param("docNo") String docNo,
            @org.apache.ibatis.annotations.Param("id") Long id);

    /**
     * 更新指定用户的提单号前缀
     */
    public int updateDocNoPrefix(@org.apache.ibatis.annotations.Param("newPrefix") String newPrefix,
            @org.apache.ibatis.annotations.Param("createBy") String createBy);
}
