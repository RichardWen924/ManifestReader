package com.ruoyi.system.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

/**
 * BillOfLadingValidator 单元测试
 * 
 * @author ruoyi
 * @date 2026-01-30
 */
public class BillOfLadingValidatorTest {

    /**
     * 测试规则1：blNo与bookingNo强制同步
     * 场景：两者不一致时，bookingNo应被blNo覆盖
     */
    @Test
    public void testBlNoAndBookingNoSync_whenDifferent_shouldSyncToBlNo() {
        // 准备测试数据
        Map<String, Object> data = new HashMap<>();
        data.put("blNo", "EVHL20260129");
        data.put("bookingNo", "DIFFERENT123");

        // 执行业务规则
        BillOfLadingValidator.applyBusinessRules(data);

        // 验证结果
        assertEquals("bookingNo should be synced to blNo", "EVHL20260129", data.get("bookingNo"));
    }

    /**
     * 测试规则1：blNo与bookingNo一致时不变
     */
    @Test
    public void testBlNoAndBookingNoSync_whenSame_shouldRemainUnchanged() {
        Map<String, Object> data = new HashMap<>();
        data.put("blNo", "EVHL20260129");
        data.put("bookingNo", "EVHL20260129");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("EVHL20260129", data.get("bookingNo"));
    }

    /**
     * 测试规则2：Freight Collect 运费逻辑
     * 场景：collectAmount应为"AS ARRANGED"，prepaidAmount应为null
     */
    @Test
    public void testFreightTermLogic_whenCollect_shouldSetCollectAndClearPrepaid() {
        Map<String, Object> data = new HashMap<>();
        data.put("freightTerm", "Freight Collect");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("AS ARRANGED", data.get("collectAmount"));
        assertNull("prepaidAmount should be null", data.get("prepaidAmount"));
    }

    /**
     * 测试规则2：Freight Prepaid 运费逻辑
     * 场景：prepaidAmount应为"AS ARRANGED"，collectAmount应为null
     */
    @Test
    public void testFreightTermLogic_whenPrepaid_shouldSetPrepaidAndClearCollect() {
        Map<String, Object> data = new HashMap<>();
        data.put("freightTerm", "Freight Prepaid");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("AS ARRANGED", data.get("prepaidAmount"));
        assertNull("collectAmount should be null", data.get("collectAmount"));
    }

    /**
     * 测试规则2：Freight Prepaid 且已有金额
     * 场景：应保留现有金额，不覆盖
     */
    @Test
    public void testFreightTermLogic_whenPrepaidWithAmount_shouldKeepExistingAmount() {
        Map<String, Object> data = new HashMap<>();
        data.put("freightTerm", "Freight Prepaid");
        data.put("prepaidAmount", "USD 1500");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("USD 1500", data.get("prepaidAmount"));
        assertNull(data.get("collectAmount"));
    }

    /**
     * 测试规则3：重量单位自动补全 - 纯数字
     * 场景：输入"50"，应输出"50 KGS"
     */
    @Test
    public void testWeightUnitAutoAppend_whenNumberOnly_shouldAppendKGS() {
        Map<String, Object> data = new HashMap<>();
        data.put("grossWeight", "50");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("50 KGS", data.get("grossWeight"));
    }

    /**
     * 测试规则3：重量单位自动补全 - 小数
     */
    @Test
    public void testWeightUnitAutoAppend_whenDecimal_shouldAppendKGS() {
        Map<String, Object> data = new HashMap<>();
        data.put("grossWeight", "50.5");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("50.5 KGS", data.get("grossWeight"));
    }

    /**
     * 测试规则3：重量已包含KGS
     * 场景：输入"50 KGS"，应保持不变
     */
    @Test
    public void testWeightUnitAutoAppend_whenAlreadyHasKGS_shouldRemainUnchanged() {
        Map<String, Object> data = new HashMap<>();
        data.put("grossWeight", "50 KGS");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("50 KGS", data.get("grossWeight"));
    }

    /**
     * 测试综合场景：所有规则一起应用
     */
    @Test
    public void testApplyBusinessRules_comprehensiveScenario() {
        Map<String, Object> data = new HashMap<>();
        data.put("blNo", "EVHL20260129");
        data.put("bookingNo", "985360"); // 不一致
        data.put("freightTerm", "Freight Collect");
        data.put("grossWeight", "50");

        BillOfLadingValidator.applyBusinessRules(data);

        // 验证所有规则都被应用
        assertEquals("EVHL20260129", data.get("bookingNo")); // 规则1
        assertEquals("AS ARRANGED", data.get("collectAmount")); // 规则2
        assertNull(data.get("prepaidAmount")); // 规则2
        assertEquals("50 KGS", data.get("grossWeight")); // 规则3
    }

    /**
     * 测试空数据处理
     */
    @Test
    public void testApplyBusinessRules_withEmptyData_shouldNotThrowException() {
        Map<String, Object> data = new HashMap<>();

        // 不应抛出异常
        BillOfLadingValidator.applyBusinessRules(data);

        assertTrue(data.isEmpty());
    }

    /**
     * 测试null数据处理
     */
    @Test
    public void testApplyBusinessRules_withNullData_shouldNotThrowException() {
        // 不应抛出异常
        Map<String, Object> result = BillOfLadingValidator.applyBusinessRules(null);

        assertNull(result);
    }

    /**
     * 测试多种重量字段名
     */
    @Test
    public void testWeightUnitAutoAppend_differentFieldNames() {
        Map<String, Object> data = new HashMap<>();
        data.put("grossWeightKgs", "100");
        data.put("cargoGrossWeight", "200");

        BillOfLadingValidator.applyBusinessRules(data);

        assertEquals("100 KGS", data.get("grossWeightKgs"));
        assertEquals("200 KGS", data.get("cargoGrossWeight"));
    }
}
