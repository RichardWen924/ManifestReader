// @author Richard
/**
 * 前端字段映射（根据bill_of_lading_v3表结构）
 * 
 * 将此代码替换到add.html的 fieldLabels 部分（约213-222行）
 */

// 字段名称映射（完整的数据库字段 - 31个字段）
var fieldLabels = {
    // 1-4: 基础编号与引用
    'blNo': 'B/L NO. 提单号',
    'bookingNo': 'BOOKING NO. 订舱号',
    'docNo': 'DOC. NO. 文件编号',
    'serialNo': 'SERIAL NO. 序列号',

    // 5-9: 运输主体信息（TEXT字段）
    'shipper': 'SHIPPER 发货人',
    'consignee': 'CONSIGNEE 收货人',
    'notifyParty': 'NOTIFY PARTY 通知方',
    'carrierAgent': 'CARRIER AGENT 承运人代理',
    'deliveryAgent': 'DELIVERY AGENT 交货代理',

    // 10-14: 运输路径与航次
    'vesselVoyage': 'VESSEL/VOYAGE 船名航次',
    'placeOfReceipt': 'PLACE OF RECEIPT 收货地',
    'portOfLoading': 'PORT OF LOADING 装货港',
    'portOfDischarge': 'PORT OF DISCHARGE 卸货港',
    'placeOfDelivery': 'PLACE OF DELIVERY 交货地',

    // 15-20: 货物与包装明细
    'containerSealInfo': 'CONTAINER/SEAL 集装箱/封条',
    'packageQuantity': 'QUANTITY 件数',
    'packageUnit': 'PACKAGE UNIT 包装单位',
    'goodsDescription': 'GOODS DESCRIPTION 货物描述',
    'grossWeightKgs': 'GROSS WEIGHT (KGS) 毛重',
    'measurementCbm': 'MEASUREMENT (CBM) 体积',

    // 21-27: 服务、费用与计费
    'serviceType': 'SERVICE TYPE 服务类型',
    'revenueTons': 'REVENUE TONS 计费吨',
    'freightTerm': 'FREIGHT TERM 运费条款',
    'freightRate': 'FREIGHT RATE 费率',
    'prepaidAmount': 'PREPAID 预付金额',
    'collectAmount': 'COLLECT 到付金额',
    'payableAt': 'PAYABLE AT 运费支付地',

    // 28-30: 签发与法律效力
    'originalBlCount': 'ORIGINAL B/L COUNT 正本提单份数',
    'issuePlace': 'PLACE OF ISSUE 签发地点',
    'ladenOnBoard': 'LADEN ON BOARD 装船批注',

    // 兼容旧字段名（Dify可能返回的字段）
    'vesselName': 'VESSEL NAME 船名',
    'voyageNo': 'VOYAGE NO. 航次',
    'containerNo': 'CONTAINER NO. 集装箱号',
    'sealNo': 'SEAL NO. 封条号',
    'marks': 'MARKS 唛头',
    'description': 'DESCRIPTION 描述',
    'grossWeight': 'GROSS WEIGHT 毛重',
    'measurement': 'MEASUREMENT 体积'
};

// TEXT类型字段（需要使用textarea）
var textAreaFields = [
    'shipper', 'consignee', 'notifyParty', 'deliveryAgent',
    'goodsDescription', 'ladenOnBoard', 'marks', 'description'
];

// 渲染表单的代码（替换224-237行）
for (var k in data) {
    if (k === 'originalFilePath' || k === 'templateFilePath') continue;
    var label = fieldLabels[k] || k;
    var value = data[k] || '';
    formHtml += '<div class="form-group">';
    formHtml += '<label class="col-sm-3 control-label">' + label + ':</label>';
    formHtml += '<div class="col-sm-8">';

    // 判断是否为多行文本字段
    if (textAreaFields.indexOf(k) !== -1) {
        formHtml += '<textarea class="form-control field-input" data-key="' + k + '" rows="4">' + value + '</textarea>';
    } else {
        formHtml += '<input type="text" class="form-control field-input" data-key="' + k + '" value="' + value + '">';
    }
    formHtml += '</div></div>';
}
