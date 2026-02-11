// @author Richard
// 字段名称映射（根据bill_of_lading_v3数据库表结构 - 31个字段）
var fieldLabels = {
    // 基础编号与引用
    'blNo': 'B/L NO.',
    'bookingNo': 'BOOKING NO.',
    'docNo': 'DOC. NO.',
    'serialNo': 'SERIAL NO.',

    // 运输主体信息
    'shipper': 'SHIPPER 发货人',
    'consignee': 'CONSIGNEE 收货人',
    'notifyParty': 'NOTIFY PARTY 通知方',
    'carrierAgent': 'CARRIER AGENT 承运人代理',
    'deliveryAgent': 'DELIVERY AGENT 交货代理',

    // 运输路径与航次
    'vesselVoyage': 'VESSEL/VOYAGE 船名航次',
    'placeOfReceipt': 'PLACE OF RECEIPT 收货地',
    'portOfLoading': 'PORT OF LOADING 装货港',
    'portOfDischarge': 'PORT OF DISCHARGE 卸货港',
    'placeOfDelivery': 'PLACE OF DELIVERY 交货地',

    // 货物与包装明细
    'containerSealInfo': 'CONTAINER/SEAL 集装箱封条',
    'packageQuantity': 'QUANTITY 件数',
    'packageUnit': 'PACKAGE UNIT 包装单位',
    'goodsDescription': 'GOODS DESCRIPTION 货物描述',
    'grossWeightKgs': 'GROSS WEIGHT(KGS) 毛重',
    'measurementCbm': 'MEASUREMENT(CBM) 体积',

    // 服务、费用与计费
    'serviceType': 'SERVICE TYPE 服务类型',
    'revenueTons': 'REVENUE TONS 计费吨',
    'freightTerm': 'FREIGHT TERM 运费条款',
    'freightRate': 'RATE 费率',
    'prepaidAmount': 'PREPAID 预付',
    'collectAmount': 'COLLECT 到付',
    'payableAt': 'PAYABLE AT 支付地',

    // 签发与法律效力
    'originalBlCount': 'ORIGINAL B/L COUNT 正本份数',
    'issuePlace': 'PLACE OF ISSUE 签发地',
    'ladenOnBoard': 'LADEN ON BOARD 装船批注',

    // 兼容旧字段（Dify可能返回）
    'vesselName': 'VESSEL NAME 船名',
    'voyageNo': 'VOYAGE NO. 航次',
    'containerNo': 'CONTAINER NO. 集装箱号',
    'sealNo': 'SEAL NO. 封条号',
    'marks': 'MARKS 唛头',
    'description': 'DESCRIPTION 描述',
    'grossWeight': 'GROSS WEIGHT 毛重',
    'measurement': 'MEASUREMENT 体积'
};

// TEXT类型字段列表（需要使用textarea）
var textAreaFields = ['shipper', 'consignee', 'notifyParty', 'deliveryAgent',
    'goodsDescription', 'ladenOnBoard', 'marks', 'description'];

for (var k in data) {
    if (k === 'originalFilePath' || k === 'templateFilePath') continue;
    var label = fieldLabels[k] || k;
    var value = data[k] || '';
    formHtml += '<div class="form-group">';
    formHtml += '<label class="col-sm-3 control-label">' + label + ':</label>';
    formHtml += '<div class="col-sm-8">';

    // 根据字段类型选择input或textarea
    if (textAreaFields.indexOf(k) !== -1) {
        formHtml += '<textarea class="form-control field-input" data-key="' + k + '" rows="4">' + value + '</textarea>';
    } else {
        formHtml += '<input type="text" class="form-control field-input" data-key="' + k + '" value="' + value + '">';
    }
    formHtml += '</div></div>';
}
