#!/bin/bash

# 更新现有 sys_pdf_template 表结构的部署脚本
# 使用方法: ./update_pdf_template.sh [database_name] [username]

DB_NAME=${1:-ry}
DB_USER=${2:-root}

echo "======================================"
echo "更新 sys_pdf_template 表结构"
echo "======================================"
echo "数据库名: $DB_NAME"
echo "用户名: $DB_USER"
echo ""

# 检查 SQL 文件是否存在
if [ ! -f "sql/update_sys_pdf_template.sql" ]; then
    echo "❌ 错误: sql/update_sys_pdf_template.sql 文件不存在"
    exit 1
fi

echo "📋 此脚本将安全地更新表结构，不会删除现有数据"
echo ""
echo "将执行以下操作:"
echo "  1. 添加 template_file_path 列（如果不存在）"
echo "  2. 添加 field_config 列（如果不存在）"
echo "  3. 添加 template_code 唯一索引（如果不存在）"
echo "  4. 插入/更新示例数据"
echo ""

read -p "是否继续? (y/n): " choice

if [ "$choice" != "y" ] && [ "$choice" != "Y" ]; then
    echo "操作已取消"
    exit 0
fi

echo ""
echo "执行 SQL 更新..."
mysql -u $DB_USER -p $DB_NAME < sql/update_sys_pdf_template.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 表结构更新成功!"
    echo ""
    echo "下一步:"
    echo "  1. 清除 Redis 缓存: redis-cli 然后执行 DEL pdf_edit:*"
    echo "  2. 重启应用"
    echo "  3. 测试功能"
else
    echo ""
    echo "❌ 更新失败，请检查错误信息"
    exit 1
fi
