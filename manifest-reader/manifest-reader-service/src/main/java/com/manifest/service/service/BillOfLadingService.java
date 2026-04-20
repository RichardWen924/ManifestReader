package com.manifest.service.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manifest.common.exception.ServiceException;
import com.manifest.model.dto.BillOfLadingDto;
import com.manifest.model.dto.BillOfLadingDto.FieldLocation;
import com.manifest.model.entity.*;
import com.manifest.service.mapper.*;
import com.manifest.service.utils.BillOfLadingDataProcessor;
import com.manifest.service.utils.BillOfLadingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 提单业务 Service
 * 继承 ServiceImpl → 拥有 MyBatis-Plus 全套 API（save/update/remove/page/lambdaQuery）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillOfLadingService extends ServiceImpl<BillOfLadingMapper, BillOfLading> {

    private static final String DIFY_API_URL   = "http://localhost/v1/workflows/run";
    private static final String DIFY_API_KEY   = "app-X43NmigYjalp8zaatv1O20Nf";
    private static final String REDIS_PREFIX   = "bl:analyze:";
    private static final String DEFAULT_TMPL   = "booking_standard";

    // 子表 Mapper（单表操作全走 MP，无需 XML）
    private final BlPartiesMapper   blPartiesMapper;
    private final BlCargoMapper     blCargoMapper;
    private final BlFreightMapper   blFreightMapper;
    private final PdfTemplateMapper pdfTemplateMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    // ======================== 查询 ========================

    /**
     * 级联查询单条（JOIN 三张子表，走 XML ResultMap）
     */
    public BillOfLading getWithDetails(Long id) {
        return baseMapper.selectWithDetailsById(id);
    }

    /**
     * 分页查询（MP 原生，仅主表字段，轻量列表用）
     */
    public Page<BillOfLading> pageByCompany(String companyCode, int pageNum, int pageSize) {
        return lambdaQuery()
                .eq(BillOfLading::getCreateBy, companyCode)
                .orderByDesc(BillOfLading::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 级联列表查询（JOIN 三张子表，走 XML）
     */
    public List<BillOfLading> listWithDetailsByCompany(String companyCode) {
        return baseMapper.selectListByCompany(companyCode);
    }

    // ======================== 新增 ========================

    @Transactional(rollbackFor = Exception.class)
    public void saveBillOfLading(BillOfLading bl) {
        // MP save() 自动调用 MetaObjectHandler 填充 createTime/updateTime
        save(bl);
        Long blId = bl.getId();
        // 子表用 MP 原生 insert，无 XML
        if (bl.getParties() != null) { bl.getParties().setBlId(blId); blPartiesMapper.insert(bl.getParties()); }
        if (bl.getCargo()   != null) { bl.getCargo().setBlId(blId);   blCargoMapper.insert(bl.getCargo()); }
        if (bl.getFreight() != null) { bl.getFreight().setBlId(blId); blFreightMapper.insert(bl.getFreight()); }
    }

    // ======================== 更新 ========================

    @Transactional(rollbackFor = Exception.class)
    public void updateBillOfLading(BillOfLading bl) {
        // MP updateById() 只更新非 null 字段（FieldStrategy.NOT_NULL）
        updateById(bl);
        if (bl.getParties() != null) blPartiesMapper.updateById(bl.getParties());
        if (bl.getCargo()   != null) blCargoMapper.updateById(bl.getCargo());
        if (bl.getFreight() != null) blFreightMapper.updateById(bl.getFreight());
    }

    // ======================== 删除 ========================

    @Transactional(rollbackFor = Exception.class)
    public void removeBillOfLading(Long id) {
        // 子表配置了 ON DELETE CASCADE，但此处显式删除更安全
        blPartiesMapper.deleteById(id);
        blCargoMapper.deleteById(id);
        blFreightMapper.deleteById(id);
        removeById(id);
    }

    // ======================== AI 解析 ========================

    /**
     * 第一步：上传文件 → Dify 解析 → 结果缓存 Redis → 返回前端预览
     */
    public BillOfLadingDto analyzeFile(String filePath) {
        log.info("[AI] 开始解析文件: {}", filePath);

        // 调用 Dify Workflow
        Map<String, Object> rawData = callDifyWorkflow(filePath);

        // 查询模版配置（MP lambdaQuery）
        PdfTemplate tmpl = pdfTemplateMapper.selectOne(
                new LambdaQueryWrapper<PdfTemplate>()
                        .eq(PdfTemplate::getTemplateCode, DEFAULT_TMPL));
        if (tmpl == null) throw new ServiceException("PDF模版配置不存在，请先在后台维护");

        // 数据清洗
        Map<String, Object> processed = BillOfLadingDataProcessor.process(rawData);

        BillOfLadingDto dto = new BillOfLadingDto();
        dto.setUuid(IdUtil.simpleUUID());
        dto.setBusinessData(processed);
        dto.setFieldLocations(parseFieldConfig(tmpl.getFieldConfig()));

        // 写入 Redis（30分钟）
        redisTemplate.opsForValue().set(REDIS_PREFIX + dto.getUuid(), dto, 30, TimeUnit.MINUTES);
        log.info("[AI] 解析完成, uuid={}", dto.getUuid());
        return dto;
    }

    /**
     * 第二步：用户确认 → 校验业务规则 → 入库
     */
    @Transactional(rollbackFor = Exception.class)
    public BillOfLading confirmAndSave(BillOfLadingDto userDto) {
        BillOfLadingDto cached = (BillOfLadingDto) redisTemplate.opsForValue()
                .get(REDIS_PREFIX + userDto.getUuid());
        if (cached == null) throw new ServiceException("会话已过期，请重新上传文件");

        Map<String, Object> data = userDto.getBusinessData() != null
                ? userDto.getBusinessData() : cached.getBusinessData();
        BillOfLadingValidator.applyBusinessRules(data);

        BillOfLading bl = buildEntity(data);
        saveBillOfLading(bl);

        redisTemplate.delete(REDIS_PREFIX + userDto.getUuid());
        return bl;
    }

    // ======================== 私有方法 ========================

    private Map<String, Object> callDifyWorkflow(String filePath) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(DIFY_API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", Collections.singletonMap("file_path", filePath));
        body.put("response_mode", "blocking");
        body.put("user", "manifest-service");

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    DIFY_API_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            if (resp.getBody() != null) {
                Object outputs = ((Map<?, ?>) resp.getBody().get("data")).get("outputs");
                if (outputs instanceof Map) return (Map<String, Object>) outputs;
            }
        } catch (Exception e) {
            log.error("[Dify] 调用失败: {}", e.getMessage(), e);
            throw new ServiceException("AI解析服务异常，请稍后重试");
        }
        return new HashMap<>();
    }

    private Map<String, FieldLocation> parseFieldConfig(String json) {
        Map<String, FieldLocation> result = new HashMap<>();
        if (StrUtil.isEmpty(json)) return result;
        try {
            Map<String, Map<String, Object>> config = JSONUtil.toBean(json, Map.class);
            config.forEach((k, v) -> result.put(k, new FieldLocation(
                    ((Number) v.get("page")).intValue(),
                    ((Number) v.get("x")).floatValue(),
                    ((Number) v.get("y")).floatValue(),
                    ((Number) v.get("w")).floatValue(),
                    ((Number) v.get("h")).floatValue())));
        } catch (Exception e) {
            log.error("[Template] 坐标解析失败", e);
        }
        return result;
    }

    private BillOfLading buildEntity(Map<String, Object> d) {
        BillOfLading bl = new BillOfLading();
        bl.setBlNo(s(d, "blNo")); bl.setBookingNo(s(d, "bookingNo")); bl.setDocNo(s(d, "docNo"));
        bl.setVesselVoyage(s(d, "vesselVoyage")); bl.setPortOfLoading(s(d, "portOfLoading"));
        bl.setPortOfDischarge(s(d, "portOfDischarge")); bl.setPlaceOfReceipt(s(d, "placeOfReceipt"));
        bl.setPlaceOfDelivery(s(d, "placeOfDelivery")); bl.setPreCarriageBy(s(d, "preCarriageBy"));

        BlParties p = new BlParties();
        p.setShipper(s(d,"shipper")); p.setConsignee(s(d,"consignee"));
        p.setNotifyParty(s(d,"notifyParty")); p.setCarrierAgent(s(d,"carrierAgent"));
        bl.setParties(p);

        BlCargo c = new BlCargo();
        c.setContainerNo(s(d,"containerNo")); c.setSealNo(s(d,"sealNo"));
        c.setGoodsDescription(s(d,"goodsDescription")); c.setMarks(s(d,"marks"));
        bl.setCargo(c);

        BlFreight f = new BlFreight();
        f.setFreightTerm(s(d,"freightTerm")); f.setPrepaidAmount(s(d,"prepaidAmount"));
        f.setCollectAmount(s(d,"collectAmount")); f.setIssuePlace(s(d,"issuePlace"));
        f.setLadenOnBoard(s(d,"ladenOnBoard"));
        bl.setFreight(f);

        return bl;
    }

    private String s(Map<String, Object> d, String k) {
        Object v = d.get(k); return v == null ? null : v.toString();
    }
}
