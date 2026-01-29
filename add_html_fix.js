// ==========================================
// 前端修复：add.html 第213-251行替换代码
// ==========================================

// 字段名称映射（根据bill_of_lading_v3数据库表结构 - 31个字段）
var fieldLabels = {
    'blNo': 'B/L NO.', 'bookingNo': 'BOOKING NO.', 'docNo': 'DOC. NO.', 'serialNo': 'SERIAL NO.',
    'shipper': 'SHIPPER 发货人', 'consignee': 'CONSIGNEE 收货人', 'notifyParty': 'NOTIFY PARTY 通知方',
    'carrierAgent': 'CARRIER AGENT 承运人代理', 'deliveryAgent': 'DELIVERY AGENT 交货代理',
    'vesselVoyage': 'VESSEL/VOYAGE 船名航次', 'placeOfReceipt': 'PLACE OF RECEIPT 收货地',
    'portOfLoading': 'PORT OF LOADING 装货港', 'portOfDischarge': 'PORT OF DISCHARGE 卸货港',
    'placeOfDelivery': 'PLACE OF DELIVERY 交货地', 'containerSealInfo': 'CONTAINER/SEAL 集装箱封条',
    'packageQuantity': 'QUANTITY 件数', 'packageUnit': 'PACKAGE UNIT 包装单位',
    'goodsDescription': 'GOODS DESCRIPTION 货物描述', 'grossWeightKgs': 'GROSS WEIGHT(KGS) 毛重',
    'measurementCbm': 'MEASUREMENT(CBM) 体积', 'serviceType': 'SERVICE TYPE 服务类型',
    'revenueTons': 'REVENUE TONS 计费吨', 'freightTerm': 'FREIGHT TERM 运费条款',
    'freightRate': 'RATE 费率', 'prepaidAmount': 'PREPAID 预付', 'collectAmount': 'COLLECT 到付',
    'payableAt': 'PAYABLE AT 支付地', 'originalBlCount': 'ORIGINAL B/L COUNT 正本份数',
    'issuePlace': 'PLACE OF ISSUE 签发地', 'ladenOnBoard': 'LADEN ON BOARD 装船批注',
    'vesselName': 'VESSEL NAME 船名', 'voyageNo': 'VOYAGE NO. 航次',
    'containerNo': 'CONTAINER NO. 集装箱号', 'sealNo': 'SEAL NO. 封条号',
    'marks': 'MARKS 唛头', 'description': 'DESCRIPTION 描述',
    'grossWeight': 'GROSS WEIGHT 毛重', 'measurement': 'MEASUREMENT 体积'
};
var textAreaFields = ['shipper', 'consignee', 'notifyParty', 'deliveryAgent', 'goodsDescription', 'ladenOnBoard', 'marks', 'description'];

// 定义字段显示顺序（所有字段）
var fieldOrder = [
    'blNo', 'bookingNo', 'docNo', 'serialNo',
    'shipper', 'consignee', 'notifyParty', 'carrierAgent', 'deliveryAgent',
    'vesselVoyage', 'vesselName', 'voyageNo',
    'placeOfReceipt', 'portOfLoading', 'portOfDischarge', 'placeOfDelivery',
    'containerSealInfo', 'containerNo', 'sealNo',
    'packageQuantity', 'packageUnit',
    'goodsDescription', 'description', 'marks',
    'grossWeightKgs', 'grossWeight',
    'measurementCbm', 'measurement',
    'serviceType', 'revenueTons', 'freightTerm', 'freightRate',
    'prepaidAmount', 'collectAmount', 'payableAt',
    'originalBlCount', 'issuePlace', 'ladenOnBoard'
];

// 遍历所有字段显示（即使没有数据也显示空输入框）
for (var i = 0; i < fieldOrder.length; i++) {
    var k = fieldOrder[i];
    var label = fieldLabels[k];
    if (!label) continue;
    var value = data[k] || '';
    formHtml += '<div class="form-group">';
    formHtml += '<label class="col-sm-3 control-label">' + label + ':</label>';
    formHtml += '<div class="col-sm-8">';
    if (textAreaFields.indexOf(k) !== -1) {
        formHtml += '<textarea class="form-control field-input" data-key="' + k + '" rows="4">' + value + '</textarea>';
    } else {
        formHtml += '<input type="text" class="form-control field-input" data-key="' + k + '" value="' + value + '">';
    }
    formHtml += '</div></div>';
}
formHtml += '</form></div></div>';
$(container).append(formHtml);
console.log("字段渲染完成，显示", fieldOrder.length, "个字段");
