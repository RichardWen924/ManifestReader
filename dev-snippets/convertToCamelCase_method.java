/**
 * 将下划线命名转换为驼峰命名（用于前端显示）
 */
private Map<String, Object> convertToCamelCase(Map<String, Object> snakeMap) {
    Map<String, Object> camelMap = new HashMap<>();
    
    // 字段映射：数据库snake_case -> 前端camelCase
    Map<String, String> fieldMapping = new HashMap<>();
    fieldMapping.put("bl_no", "blNo");
    fieldMapping.put("booking_no", "bookingNo");
    fieldMapping.put("doc_no", "docNo");
    fieldMapping.put("serial_no", "serialNo");
    fieldMapping.put("shipper", "shipper");
    fieldMapping.put("consignee", "consignee");
    fieldMapping.put("notify_party", "notifyParty");
    fieldMapping.put("carrier_agent", "carrierAgent");
    fieldMapping.put("delivery_agent", "deliveryAgent");
    fieldMapping.put("vessel_voyage", "vesselVoyage");
    fieldMapping.put("vessel_name", "vesselName");
    fieldMapping.put("voyage_no", "voyageNo");
    fieldMapping.put("place_of_receipt", "placeOfReceipt");
    fieldMapping.put("port_of_loading", "portOfLoading");
    fieldMapping.put("port_of_discharge", "portOfDischarge");
    fieldMapping.put("place_of_delivery", "placeOfDelivery");
    fieldMapping.put("container_seal_info", "containerSealInfo");
    fieldMapping.put("container_no", "containerNo");
    fieldMapping.put("seal_no", "sealNo");
    fieldMapping.put("package_quantity", "packageQuantity");
    fieldMapping.put("package_unit", "packageUnit");
    fieldMapping.put("goods_description", "goodsDescription");
    fieldMapping.put("gross_weight_kgs", "grossWeightKgs");
    fieldMapping.put("gross_weight", "grossWeight");
    fieldMapping.put("measurement_cbm", "measurementCbm");
    fieldMapping.put("measurement", "measurement");
    fieldMapping.put("service_type", "serviceType");
    fieldMapping.put("revenue_tons", "revenueTons");
    fieldMapping.put("freight_term", "freightTerm");
    fieldMapping.put("freight_rate", "freightRate");
    fieldMapping.put("prepaid_amount", "prepaidAmount");
    fieldMapping.put("collect_amount", "collectAmount");
    fieldMapping.put("payable_at", "payableAt");
    fieldMapping.put("original_bl_count", "originalBlCount");
    fieldMapping.put("issue_place", "issuePlace");
    fieldMapping.put("laden_on_board", "ladenOnBoard");
    
    for (Map.Entry<String, Object> entry : snakeMap.entrySet()) {
        String snakeKey = entry.getKey();
        String camelKey = fieldMapping.getOrDefault(snakeKey, snakeKey);
        camelMap.put(camelKey, entry.getValue());
    }
    
    log.debug("字段名转换：{} 个字段从snake_case转为camelCase", camelMap.size());
    return camelMap;
}
