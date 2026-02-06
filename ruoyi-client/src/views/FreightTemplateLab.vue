<template>
  <div class="template-lab">
    <div class="header">
      <div class="title-area">
        <h1>智能模版实验室 <span class="badge">BETA</span></h1>
        <p>上传 Docx 文档，AI 自动识别并生成 mustache {{}} 动态模版</p>
      </div>
      <div class="actions">
        <el-upload
          class="upload-demo"
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleFileChange"
        >
          <el-button type="primary" icon="el-icon-upload">上传原始文档</el-button>
        </el-upload>
        <el-button type="success" icon="el-icon-refresh" @click="syncPreview" :loading="previewLoading" :disabled="!file">同步预览</el-button>
        <el-button type="warning" icon="el-icon-document-checked" @click="handleSave" :disabled="!file">保存模版</el-button>
      </div>
    </div>

    <div class="lab-container">
      <!-- 左侧：数据校验区 -->
      <div class="mapping-pane card">
        <div class="pane-header">
          <h3>数据映射列表</h3>
          <el-tooltip content="修改占位符后点击同步预览" placement="top">
            <i class="el-icon-info"></i>
          </el-tooltip>
        </div>
        <el-table 
          :data="mappings" 
          height="100%" 
          style="width: 100%"
          @cell-mouse-enter="handleMouseEnter"
          @cell-mouse-leave="handleMouseLeave"
        >
          <el-table-column prop="originalText" label="原文内容" width="150" show-overflow-tooltip />
          <el-table-column label="占位符变量 (placeholder_key)">
            <template slot-scope="scope">
              <el-input v-model="scope.row.placeholderKey" size="mini" placeholder="mustache key" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template slot-scope="scope">
              <el-button type="text" style="color: #f56c6c" icon="el-icon-delete" @click="removeMapping(scope.$index)" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 右侧：实时预览区 -->
      <div class="preview-pane card">
        <div class="pane-header">
          <h3>预览效果 (Docx-Preview)</h3>
        </div>
        <div id="preview-container" v-loading="previewLoading">
          <div v-if="!file" class="empty-preview">
            <i class="el-icon-document"></i>
            <p>请先上传文档进行解析</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { renderAsync } from 'docx-preview'
import axios from 'axios'

export default {
  name: 'FreightTemplateLab',
  data() {
    return {
      file: null,
      mappings: [],
      previewLoading: false,
      baseUrl: process.env.VUE_APP_BASE_API
    }
  },
  methods: {
    // 上传文件并分析
    handleFileChange(uploadFile) {
      this.file = uploadFile.raw
      this.analyzeDocument()
    },

    async analyzeDocument() {
      const formData = new FormData()
      formData.append('file', this.file)
      
      try {
        const res = await axios.post(`${this.baseUrl}/system/template-lab/analyze`, formData)
        if (res.data.code === 200) {
          this.mappings = res.data.data
          this.$message.success('文档识别成功')
          this.initialPreview()
        }
      } catch (err) {
        this.$message.error('解析失败')
      }
    },

    // 初始渲染原文档
    async initialPreview() {
      const container = document.getElementById('preview-container')
      await renderAsync(this.file, container)
    },

    // 同步预览（将映射数据发送给后端进行替换）
    async syncPreview() {
      this.previewLoading = true
      const formData = new FormData()
      formData.append('file', this.file)
      formData.append('mappings', JSON.stringify(this.mappings))

      try {
        const response = await axios.post(`${this.baseUrl}/system/template-lab/preview`, formData, {
          responseType: 'blob'
        })
        const container = document.getElementById('preview-container')
        container.innerHTML = '' // 清空旧预览
        await renderAsync(response.data, container)
        this.$message.success('同步预览成功')
      } catch (err) {
        this.$message.error('预览失败')
      } finally {
        this.previewLoading = false
      }
    },

    handleSave() {
      this.$message.info('保存功能开发中...')
    },

    removeMapping(index) {
      this.mappings.splice(index, 1)
    },

    // 实时高亮逻辑
    handleMouseEnter(row) {
      const targetText = row.originalText
      const container = document.getElementById('preview-container')
      // 遍历所有文本节点或 span
      const spans = container.querySelectorAll('span')
      spans.forEach(span => {
        if (span.innerText.includes(targetText)) {
          span.classList.add('lab-highlight')
        }
      })
    },

    handleMouseLeave() {
      const container = document.getElementById('preview-container')
      const highlighted = container.querySelectorAll('.lab-highlight')
      highlighted.forEach(el => el.classList.remove('lab-highlight'))
    }
  }
}
</script>

<style scoped>
.template-lab {
  padding: 20px;
  height: calc(100vh - 84px);
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-area h1 {
  font-size: 24px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge {
  font-size: 12px;
  background: #6366f1;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
}

.title-area p {
  margin: 5px 0 0;
  color: #64748b;
}

.actions {
  display: flex;
  gap: 10px;
}

.lab-container {
  display: flex;
  gap: 20px;
  flex: 1;
  overflow: hidden;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
}

.mapping-pane {
  flex: 4;
  padding: 20px;
}

.preview-pane {
  flex: 6;
  overflow-y: auto;
  position: relative;
}

.pane-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.pane-header h3 {
  margin: 0;
  font-size: 16px;
  color: #334155;
}

#preview-container {
  flex: 1;
  padding: 20px;
  background: #f8fafc;
  min-height: 400px;
}

.empty-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.empty-preview i {
  font-size: 48px;
  margin-bottom: 10px;
}

/* 全局样式覆盖，用于高亮 */
:deep(.lab-highlight) {
  background-color: #fef08a !important;
  transition: background-color 0.2s;
  border-radius: 2px;
  box-shadow: 0 0 4px rgba(234, 179, 8, 0.4);
}
</style>
