/**
 * 只保存到数据库（不生成PDF）
 */
@Override
@Transactional(rollbackFor = Exception.class)
public BookingConsolidated saveToDbOnly(BookingConsolidatedDto userDto) {
    log.info("只保存数据库，UUID: {}", userDto.getUuid());

    // 从 Redis 获取缓存
    BookingConsolidatedDto cachedDto = (BookingConsolidatedDto) redisTemplate.opsForValue()
            .get(REDIS_PREFIX + userDto.getUuid());
    if (cachedDto == null) {
        throw new RuntimeException("会话已过期，请重新上传文件");
    }

    // 合并数据：从缓存获取完整数据，然后用用户编辑的数据覆盖
    Map<String, Object> cachedData = (Map<String, Object>) cachedDto.getBusinessData();
    Map<String, Object> mergedData = new HashMap<>(cachedData);
    Map<String, Object> userData = (Map<String, Object>) userDto.getBusinessData();
    if (userData != null) {
        mergedData.putAll(userData);
    }
    log.info("合并后的数据（原始）: {}", mergedData);

    // 转换字段名：驼峰 -> 下划线（用于数据库保存）
    Map<String, Object> dbData = convertCamelToUnderscore(mergedData);
    log.info("转换后的数据（数据库格式）: {}", dbData);

    // 确保 booking_no 存在
    if (StringUtils.isEmpty((String) dbData.get("booking_no"))) {
        String generatedBookingNo = "BK" + System.currentTimeMillis();
        dbData.put("booking_no", generatedBookingNo);
        log.warn("booking_no 不存在，生成默认值: {}", generatedBookingNo);
    }

    // 保存到数据库
    com.ruoyi.system.domain.BillOfLading bl = mapMapToBillOfLading(dbData);
    
    // 确保bl_no存在
    if (StringUtils.isEmpty(bl.getBlNo())) {
        bl.setBlNo("BL" + System.currentTimeMillis());
    }

    log.info("准备插入bill_of_lading_v3表，bl_no: {}, booking_no: {}",
            bl.getBlNo(), bl.getBookingNo());
    billOfLadingMapper.insertBillOfLading(bl);

    // 返回结果
    BookingConsolidated bc = new BookingConsolidated();
    bc.setBookingNo(bl.getBookingNo());
    return bc;
}
