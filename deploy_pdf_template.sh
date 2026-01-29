#!/bin/bash

# PDF 模版表部署脚本
# 使用方法: ./deploy_pdf_template.sh [database_name] [username]

DB_NAME=${1:-ry}
DB_USER=${2:-root}

echo "======================================"
echo "PDF 模版表部署脚本"
echo "======================================"
echo "数据库名: $DB_NAME"
echo "用户名: $DB_USER"
echo ""

# 检查 SQL 文件是否存在
if [ ! -f "sql/sys_pdf_template.sql" ]; then
    echo "❌ 错误: sql/sys_pdf_template.sql 文件不存在"
    exit 1
fi

echo "📋 准备执行 SQL 文件..."
echo ""

# 方法1: 使用 mysql 命令行 (需要输入密码)
echo "方法1: 使用 mysql 命令行"
echo "执行命令: mysql -u $DB_USER -p $DB_NAME < sql/sys_pdf_template.sql"
echo ""
read -p "是否使用此方法? (y/n): " choice1

if [ "$choice1" = "y" ] || [ "$choice1" = "Y" ]; then
    mysql -u $DB_USER -p $DB_NAME < sql/sys_pdf_template.sql
    if [ $? -eq 0 ]; then
        echo "✅ SQL 执行成功!"
    else
        echo "❌ SQL 执行失败!"
        exit 1
    fi
    exit 0
fi

# 方法2: 显示 SQL 内容，手动执行
echo ""
echo "方法2: 手动复制 SQL 到数据库工具执行"
echo "======================================"
cat sql/sys_pdf_template.sql
echo "======================================"
echo ""
echo "请复制上述 SQL 语句到您的数据库管理工具 (如 Navicat, DBeaver, phpMyAdmin) 中执行"
echo ""

# 验证表是否创建成功
echo "执行完成后，可以运行以下命令验证:"
echo "mysql -u $DB_USER -p -e \"USE $DB_NAME; SELECT * FROM sys_pdf_template;\""
