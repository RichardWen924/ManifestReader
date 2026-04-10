-- 更新PDF模板坐标配置
-- 假设sys_pdf_template表已存在，更新field_config字段

UPDATE sys_pdf_template 
SET field_config = '{
  "shipper": {"page": 1, "x": 45, "y": 780, "w": 300, "h": 80, "source_id": 34},
  "consignee": {"page": 1, "x": 45, "y": 680, "w": 300, "h": 80, "source_id": 36},
  "notifyParty": {"page": 1, "x": 45, "y": 580, "w": 300, "h": 80, "source_id": 45},
  "blNo": {"page": 1, "x": 450, "y": 250, "w": 130, "h": 20, "source_id": 76},
  "bookingNo": {"page": 1, "x": 450, "y": 765, "w": 130, "h": 20, "source_id": 40},
  "docNo": {"page": 1, "x": 450, "y": 800, "w": 130, "h": 20, "source_id": 39},
  "serialNo": {"page": 1, "x": 450, "y": 730, "w": 130, "h": 15, "source_id": 43},
  "vesselVoyage": {"page": 1, "x": 45, "y": 485, "w": 250, "h": 15, "source_id": 48},
  "placeOfReceipt": {"page": 1, "x": 45, "y": 440, "w": 180, "h": 15, "source_id": 54},
  "portOfLoading": {"page": 1, "x": 230, "y": 440, "w": 180, "h": 15, "source_id": 55},
  "portOfDischarge": {"page": 1, "x": 45, "y": 400, "w": 180, "h": 15, "source_id": 49},
  "placeOfDelivery": {"page": 1, "x": 230, "y": 400, "w": 180, "h": 15, "source_id": 56},
  "containerSealInfo": {"page": 1, "x": 45, "y": 320, "w": 200, "h": 60, "source_id": 53},
  "packageQuantity": {"page": 1, "x": 255, "y": 320, "w": 100, "h": 60, "source_id": 61},
  "goodsDescription": {"page": 1, "x": 365, "y": 320, "w": 150, "h": 100, "source_id": 64},
  "grossWeightKgs": {"page": 1, "x": 525, "y": 320, "w": 70, "h": 30, "source_id": 66},
  "measurementCbm": {"page": 1, "x": 525, "y": 290, "w": 70, "h": 30, "source_id": 67},
  "revenueTons": {"page": 1, "x": 45, "y": 190, "w": 90, "h": 15, "source_id": 70},
  "freightRate": {"page": 1, "x": 140, "y": 190, "w": 90, "h": 15, "source_id": 71},
  "prepaidAmount": {"page": 1, "x": 240, "y": 190, "w": 90, "h": 15, "source_id": 73},
  "collectAmount": {"page": 1, "x": 340, "y": 190, "w": 90, "h": 15, "source_id": 74},
  "serviceType": {"page": 1, "x": 450, "y": 210, "w": 130, "h": 15, "source_id": 78},
  "originalBlCount": {"page": 1, "x": 45, "y": 125, "w": 150, "h": 15, "source_id": 79},
  "issuePlace": {"page": 1, "x": 45, "y": 90, "w": 250, "h": 15, "source_id": 81},
  "ladenOnBoard": {"page": 1, "x": 45, "y": 55, "w": 250, "h": 15, "source_id": 83},
  "payableAt": {"page": 1, "x": 450, "y": 55, "w": 130, "h": 15, "source_id": 84}
}'
WHERE template_name = 'bill_of_lading'  -- 根据实际模板名称修改
  OR id = 1;  -- 或者根据实际ID修改

-- 如果字段名使用snake_case，请使用以下SQL：
/*
UPDATE sys_pdf_template 
SET field_config = '{
  "shipper": {"page": 1, "x": 45, "y": 780, "w": 300, "h": 80, "source_id": 34},
  "consignee": {"page": 1, "x": 45, "y": 680, "w": 300, "h": 80, "source_id": 36},
  "notify_party": {"page": 1, "x": 45, "y": 580, "w": 300, "h": 80, "source_id": 45},
  "bl_no": {"page": 1, "x": 450, "y": 250, "w": 130, "h": 20, "source_id": 76},
  "booking_no": {"page": 1, "x": 450, "y": 765, "w": 130, "h": 20, "source_id": 40},
  "doc_no": {"page": 1, "x": 450, "y": 800, "w": 130, "h": 20, "source_id": 39},
  "serial_no": {"page": 1, "x": 450, "y": 730, "w": 130, "h": 15, "source_id": 43},
  "vessel_voyage": {"page": 1, "x": 45, "y": 485, "w": 250, "h": 15, "source_id": 48},
  "place_of_receipt": {"page": 1, "x": 45, "y": 440, "w": 180, "h": 15, "source_id": 54},
  "port_of_loading": {"page": 1, "x": 230, "y": 440, "w": 180, "h": 15, "source_id": 55},
  "port_of_discharge": {"page": 1, "x": 45, "y": 400, "w": 180, "h": 15, "source_id": 49},
  "place_of_delivery": {"page": 1, "x": 230, "y": 400, "w": 180, "h": 15, "source_id": 56},
  "container_seal_info": {"page": 1, "x": 45, "y": 320, "w": 200, "h": 60, "source_id": 53},
  "package_quantity": {"page": 1, "x": 255, "y": 320, "w": 100, "h": 60, "source_id": 61},
  "goods_description": {"page": 1, "x": 365, "y": 320, "w": 150, "h": 100, "source_id": 64},
  "gross_weight_kgs": {"page": 1, "x": 525, "y": 320, "w": 70, "h": 30, "source_id": 66},
  "measurement_cbm": {"page": 1, "x": 525, "y": 290, "w": 70, "h": 30, "source_id": 67},
  "revenue_tons": {"page": 1, "x": 45, "y": 190, "w": 90, "h": 15, "source_id": 70},
  "freight_rate": {"page": 1, "x": 140, "y": 190, "w": 90, "h": 15, "source_id": 71},
  "prepaid_amount": {"page": 1, "x": 240, "y": 190, "w": 90, "h": 15, "source_id": 73},
  "collect_amount": {"page": 1, "x": 340, "y": 190, "w": 90, "h": 15, "source_id": 74},
  "service_type": {"page": 1, "x": 450, "y": 210, "w": 130, "h": 15, "source_id": 78},
  "original_bl_count": {"page": 1, "x": 45, "y": 125, "w": 150, "h": 15, "source_id": 79},
  "issue_place": {"page": 1, "x": 45, "y": 90, "w": 250, "h": 15, "source_id": 81},
  "laden_on_board": {"page": 1, "x": 45, "y": 55, "w": 250, "h": 15, "source_id": 83},
  "payable_at": {"page": 1, "x": 450, "y": 55, "w": 130, "h": 15, "source_id": 84}
}'
WHERE template_name = 'bill_of_lading';
*/
