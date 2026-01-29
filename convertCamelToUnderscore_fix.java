/**
 * 后端字段映射修复
 * 
 * BookingConsolidatedServiceImpl.java
 * 替换convertCamelToUnderscore方法（约533-583行）
 * 
 * 在方法末尾添加getString辅助方法（约584行之后）
 */

// ==========================================
// 第533-640行：完整替换convertCamelToUnderscore方法及辅助方法
// ==========================================

/**
 * 将驼峰命名的Map转换为下划线命名的Map（用于数据库字段映射）
 */
private Map<String, Object> convertCamelToUnderscore(Map<String, Object> camelMap) {
    Map<String, Object> underscoreMap = new HashMap<>();

    // 完整字段映射
    Map<String, String> fieldMapping = new HashMap<>();
    fieldMapping.put("blNo", "bl_no");
    fieldMapping.put("bookingNo", "booking_no");
    fieldMapping.put("docNo", "doc_no");
    fieldMapping.put("serialNo", "serial_no");
    fieldMapping.put("shipper", "shipper");
    fieldMapping.put("consignee", "consignee");
    fieldMapping.put("notifyParty", "notify_party");
    fieldMapping.put("carrierAgent", "carrier_agent");
    fieldMapping.put("deliveryAgent", "delivery_agent");
    fieldMapping.put("vesselVoyage", "vessel_voyage");
    fieldMapping.put("placeOfReceipt", "place_of_receipt");
    fieldMapping.put("portOfLoading", "port_of_loading");
    fieldMapping.put("portOfDischarge", "port_of_discharge");
    fieldMapping.put("placeOfDelivery", "place_of_delivery");
    fieldMapping.put("containerSealInfo", "container_seal_info");
    fieldMapping.put("packageUnit", "package_unit");
    fieldMapping.put("goodsDescription", "goods_description");
    fieldMapping.put("description", "goods_description");
    fieldMapping.put("marks", "goods_description");
    fieldMapping.put("grossWeightKgs", "gross_weight_kgs");
    fieldMapping.put("measurementCbm", "measurement_cbm");
    fieldMapping.put("serviceType", "service_type");
    fieldMapping.put("revenueTons", "revenue_tons");
    fieldMapping.put("freightTerm", "freight_term");
    fieldMapping.put("freightRate", "freight_rate");
    fieldMapping.put("prepaidAmount", "prepaid_amount");
    fieldMapping.put("collectAmount", "collect_amount");
    fieldMapping.put("payableAt", "payable_at");
    fieldMapping.put("originalBlCount", "original_bl_count");
    fieldMapping.put("issuePlace", "issue_place");
    fieldMapping.put("ladenOnBoard", "laden_on_board");

    // 特殊处理：vesselName + voyageNo
    if (camelMap.containsKey("vesselName") || camelMap.containsKey("voyageNo")) {
        String vessel = getStringValue(camelMap, "vesselName");
        String voyage = getStringValue(camelMap, "voyageNo");
        if (!StringUtils.isEmpty(vessel) || !StringUtils.isEmpty(voyage)) {
            underscoreMap.put("vessel_voyage", (vessel + " " + voyage).trim());
        }
    }
    
    // 特殊处理：containerNo + sealNo
    if (camelMap.containsKey("containerNo") || camelMap.containsKey("sealNo")) {
        String container = getStringValue(camelMap, "containerNo");
        String seal = getStringValue(camelMap, "sealNo");
        if (!StringUtils.isEmpty(container) || !StringUtils.isEmpty(seal)) {
            underscoreMap.put("container_seal_info", (container + " / " + seal).trim());
        }
    }
    
    // 特殊处理：packageQuantity拆分
    if (camelMap.containsKey("packageQuantity")) {
        String pkgQty = getStringValue(camelMap, "packageQuantity");
        if (!StringUtils.isEmpty(pkgQty)) {
            String[] parts = pkgQty.trim().split("\\s+", 2);
            try {
                underscoreMap.put("package_quantity", Integer.parseInt(parts[0]));
                if (parts.length > 1) {
                    underscoreMap.put("package_unit", parts[1]);
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析packageQuantity: {}", pkgQty);
            }
        }
    }
    
    // 特殊处理：grossWeight提取数字
    if (camelMap.containsKey("grossWeight")) {
        String weight = getStringValue(camelMap, "grossWeight");
        if (!StringUtils.isEmpty(weight)) {
            try {
                String numStr = weight.replaceAll("[^0-9.]", "").trim();
                if (!StringUtils.isEmpty(numStr)) {
                    underscoreMap.put("gross_weight_kgs", new BigDecimal(numStr));
                }
            } catch (Exception e) {
                log.warn("无法解析grossWeight: {}", weight);
            }
        }
    }
    
    // 特殊处理：measurement提取数字
    if (camelMap.containsKey("measurement")) {
        String measure = getStringValue(camelMap, "measurement");
        if (!StringUtils.isEmpty(measure)) {
            try {
                String numStr = measure.replaceAll("[^0-9.]", "").trim();
                if (!StringUtils.isEmpty(numStr)) {
                    underscoreMap.put("measurement_cbm", new BigDecimal(numStr));
                }
            } catch (Exception e) {
                log.warn("无法解析measurement: {}", measure);
            }
        }
    }

    // 转换所有字段
    for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
        String camelKey = entry.getKey();
        String underscoreKey = entry.getValue();
        if (camelMap.containsKey(camelKey)) {
            Object value = camelMap.get(camelKey);
            if (value != null && !StringUtils.isEmpty(value.toString())) {
                if (!underscoreMap.containsKey(underscoreKey)) {
                    underscoreMap.put(underscoreKey, value);
                }
            }
        }
    }

    // 保留内部字段
    if (camelMap.containsKey("originalFilePath")) {
        underscoreMap.put("originalFilePath", camelMap.get("originalFilePath"));
    }
    if (camelMap.containsKey("templateFilePath")) {
        underscoreMap.put("templateFilePath", camelMap.get("templateFilePath"));
    }

    log.info("字段转换：输入{}个 -> 输出{}个", camelMap.size(), underscoreMap.size());
    return underscoreMap;
}

// 辅助方法：安全获取字符串
private String getStringValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? value.toString() : "";
}
