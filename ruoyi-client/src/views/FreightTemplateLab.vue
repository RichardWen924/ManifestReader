<template>
  <div class="dashboard-page">
    <!-- Sidebar (Consistent with Dashboard/History) -->
    <nav class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">
          <i class="fas fa-ship"></i>
        </div>
        <div class="logo-text">
          <h1>提单导出系统</h1>
          <span>Shipping Document System</span>
        </div>
      </div>
      <ul class="nav-links">
        <li :class="{ active: $route.path === '/' }">
          <router-link to="/"><i class="fas fa-magic"></i> AI Analysis</router-link>
        </li>
        <li :class="{ active: $route.path === '/history' }">
          <router-link to="/history"><i class="fas fa-history"></i> My Records</router-link>
        </li>
        <li :class="{ active: $route.path === '/lab' }">
          <router-link to="/lab"><i class="fas fa-flask"></i> Template Lab</router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="user-info" title="Current User">
          <i class="fas fa-user-circle"></i>
          <span class="user-abbr">{{ userAbbr }}</span>
          <span class="user-code">{{ currentUser }}</span>
        </div>
      </div>
    </nav>

    <!-- Main Content Area -->
    <main class="main-content">
      <div class="template-lab-content">
        <header class="lab-header">
          <div class="title-area">
            <h2>智能模版实验室 <span class="badge">BETA</span></h2>
            <p>上传 Docx 文档，AI 自动识别并生成 mustache {{}} 动态模版</p>
          </div>
          <div class="actions">
            <label class="btn-primary">
              <i class="fas fa-upload"></i> 上传原始文档
              <input type="file" @change="handleFileChange" accept=".docx" hidden>
            </label>
            <button class="btn-success" @click="syncPreview" :disabled="!file || previewLoading">
              <i class="fas fa-sync" :class="{ 'fa-spin': previewLoading }"></i> 同步预览
            </button>
            <button class="btn-warning" @click="handleSave" :disabled="!file">
              <i class="fas fa-save"></i> 保存模版
            </button>
          </div>
        </header>

        <div class="lab-workspace">
          <!-- Left: Mapping Table -->
          <div class="mapping-pane glass-card">
            <div class="pane-header">
              <h3><i class="fas fa-list-ul"></i> 数据映射列表</h3>
            </div>
            <div class="table-container">
               <div v-if="analyzeLoading" class="analyze-loader">
                <div class="spinner"></div>
                <p>AI 智能解析中，请稍候...</p>
              </div>
              <table v-else class="lab-table">
                <thead>
                  <tr>
                    <th>原文内容</th>
                    <th>占位符变量 (key)</th>
                    <th>类型</th>
                    <th>说明</th>
                    <th width="40"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in mappings" :key="index" 
                      @mouseenter="handleMouseEnter(item)" 
                      @mouseleave="handleMouseLeave">
                    <td class="original-text-cell" :title="item.originalText">{{ item.originalText }}</td>
                    <td>
                      <input v-model="item.placeholderKey" class="lab-input" placeholder="变量名..." spellcheck="false">
                    </td>
                    <td>
                      <span class="type-badge" :class="item.dataType">{{ item.dataType || 'string' }}</span>
                    </td>
                    <td>
                      <input v-model="item.description" class="lab-input" placeholder="字段说明..." spellcheck="false">
                    </td>
                    <td>
                      <button class="btn-icon-delete" @click="removeMapping(index)">
                        <i class="fas fa-times"></i>
                      </button>
                    </td>
                  </tr>
                  <tr v-if="mappings.length === 0">
                    <td colspan="5" class="empty-msg">请上传文档以识别字段</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Right: Visual Preview -->
          <div class="preview-pane glass-card">
            <div class="pane-header">
              <h3><i class="fas fa-eye"></i> 可视化预览</h3>
            </div>
            <div id="preview-container">
              <div v-if="!file" class="empty-preview">
                <i class="fas fa-file-word"></i>
                <p>实时渲染区域</p>
              </div>
            </div>
            <div v-if="previewLoading" class="preview-loader">
              <div class="spinner"></div>
              <p>生成预览中...</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue'
import { renderAsync } from 'docx-preview'
import { analyzeTemplate, previewTemplate, saveTemplate } from '../api/lab'

export default defineComponent({
  name: 'FreightTemplateLab',
  setup() {
    const file = ref(null)
    const mappings = ref([])
    const previewLoading = ref(false)
    const analyzeLoading = ref(false)
    const currentUser = ref('')
    const userAbbr = ref('')

    onMounted(() => {
      const userData = JSON.parse(localStorage.getItem('client_user') || '{}')
      currentUser.value = userData.companyCode || 'Unknown'
      userAbbr.value = userData.companyAbbr || 'SYS'
    })

    const handleFileChange = (e) => {
      const selected = e.target.files[0]
      if (selected) {
        file.value = selected
        analyzeDocument()
      }
    }

    const analyzeDocument = async () => {
      const formData = new FormData()
      formData.append('file', file.value)
      analyzeLoading.value = true
      try {
        const data = await analyzeTemplate(formData)
        mappings.value = data.data || []
        renderOriginal()
      } catch (err) {
        console.error('Analysis failed:', err)
        alert('智能分析失败，请重试')
      } finally {
        analyzeLoading.value = false
      }
    }

    const renderOriginal = async () => {
      const container = document.getElementById('preview-container')
      if (!container) return
      container.innerHTML = ''
      await renderAsync(file.value, container)
    }

    const syncPreview = async () => {
      if (!file.value) return
      previewLoading.value = true
      const formData = new FormData()
      formData.append('file', file.value)
      formData.append('mappings', JSON.stringify(mappings.value))
      try {
        const blob = await previewTemplate(formData)
        const container = document.getElementById('preview-container')
        container.innerHTML = ''
        await renderAsync(blob, container)
      } catch (err) {
        console.error('Sync failed:', err)
      } finally {
        previewLoading.value = false
      }
    }

    const handleSave = async () => {
      const name = prompt('请输入模版名称:', '新模版')
      if (!name) return
      
      const formData = new FormData()
      formData.append('file', file.value)
      formData.append('mappings', JSON.stringify(mappings.value))
      formData.append('templateName', name)
      
      try {
        const res = await saveTemplate(formData)
        alert('模版保存成功: ' + res.data)
      } catch (err) {
        console.error('Save failed:', err)
        alert('保存失败')
      }
    }

    const removeMapping = (idx) => {
      mappings.value.splice(idx, 1)
    }

    const handleMouseEnter = (row) => {
      const container = document.getElementById('preview-container')
      if (!container) return
      const spans = container.querySelectorAll('span')
      spans.forEach(s => {
        if (s.innerText.includes(row.originalText)) s.classList.add('lab-highlight')
      })
    }

    const handleMouseLeave = () => {
      const container = document.getElementById('preview-container')
      if (!container) return
      container.querySelectorAll('.lab-highlight').forEach(el => el.classList.remove('lab-highlight'))
    }

    return {
      file, mappings, previewLoading, analyzeLoading, currentUser, userAbbr,
      handleFileChange, syncPreview, handleSave, removeMapping, handleMouseEnter, handleMouseLeave
    }
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-dark);
}

.sidebar {
  width: 260px;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(10px);
  border-right: 1px solid var(--glass-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar-header {
  padding: 30px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.logo-text h1 {
  font-size: 18px;
  color: white;
  margin: 0;
}

.logo-text span {
  font-size: 11px;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-links {
  list-style: none;
  padding: 20px 15px;
  flex: 1;
}

.nav-links li {
  margin-bottom: 8px;
}

.nav-links a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
  color: var(--text-dim);
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-links li.active a, .nav-links a:hover {
  background: var(--glass-bg);
  color: white;
  box-shadow: inset 0 0 0 1px var(--glass-border);
}

.nav-links i { width: 20px; text-align: center; }

.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 40px;
}

.template-lab-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
}

.lab-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 30px;
}

.title-area h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
}

.badge {
  font-size: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  padding: 4px 10px;
  border-radius: 6px;
  vertical-align: middle;
}

.title-area p { color: var(--text-dim); }

.actions { display: flex; gap: 15px; }

.lab-workspace {
  display: flex;
  gap: 25px;
  flex: 1;
  overflow: hidden;
}

.glass-card {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  padding: 25px;
  display: flex;
  flex-direction: column;
}

.mapping-pane { flex: 4.5; min-width: 500px; }
.preview-pane { flex: 5.5; position: relative; }

.pane-header {
  margin-bottom: 20px;
  border-bottom: 1px solid var(--glass-border);
  padding-bottom: 15px;
}

.pane-header h3 { font-size: 18px; display: flex; align-items: center; gap: 10px; }

.table-container { flex: 1; overflow-y: auto; }

.lab-table { width: 100%; border-collapse: collapse; }
.lab-table th { text-align: left; padding: 12px; font-size: 13px; color: var(--text-dim); border-bottom: 1px solid var(--glass-border); }
.lab-table td { padding: 12px; border-bottom: 1px solid var(--glass-border); }

.original-text-cell { max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 14px; }

.type-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255,255,255,0.1);
  color: var(--text-dim);
  text-transform: lowercase;
}

.type-badge.string { background: rgba(99, 102, 241, 0.2); color: #818cf8; }
.type-badge.number { background: rgba(16, 185, 129, 0.2); color: #34d399; }

.lab-input {
  width: 100%;
  padding: 8px 12px;
  background: rgba(255,255,255,0.03);
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  color: white;
  transition: all 0.2s;
}

.lab-input:focus { outline: none; border-color: var(--primary-color); background: rgba(255,255,255,0.06); }

.btn-primary, .btn-success, .btn-warning {
  padding: 12px 24px; border-radius: 12px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; gap: 8px; transition: all 0.3s;
  border: none; color: white; font-family: inherit;
}

.btn-primary { background: var(--primary-color); }
.btn-success { background: #10b981; }
.btn-warning { background: #f59e0b; }
.btn-primary:hover, .btn-success:hover, .btn-warning:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.3); }
[disabled] { opacity: 0.5; cursor: not-allowed; transform: none !important; }

.btn-icon-delete { background: transparent; border: none; color: #ef4444; opacity: 0.5; cursor: pointer; transition: opacity 0.2s; }
.btn-icon-delete:hover { opacity: 1; }

#preview-container {
  flex: 1; background: white; border-radius: 16px; overflow-y: auto; color: #334155; padding: 30px;
}

.empty-preview {
  height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94a3b8;
}
.empty-preview i { font-size: 64px; margin-bottom: 20px; }

.preview-loader {
  position: absolute; inset: 0; background: rgba(15, 23, 42, 0.8); backdrop-filter: blur(4px);
  display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 10; border-radius: 24px;
}

.spinner {
  width: 40px; height: 40px; border: 3px solid rgba(99, 102, 241, 0.2); border-top-color: var(--primary-color);
  border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 15px;
}

.analyze-loader {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-dim);
  background: rgba(255,255,255,0.02);
  border-radius: 12px;
}

@keyframes spin { to { transform: rotate(360deg); } }

:deep(.lab-highlight) {
  background-color: #fef08a !important; color: #854d0e !important; box-shadow: 0 0 0 2px #fef08a; border-radius: 2px;
}

.sidebar-footer { padding: 25px 20px; border-top: 1px solid var(--glass-border); }
.user-info { display: flex; align-items: center; gap: 10px; color: var(--text-dim); }
.user-abbr { font-weight: 700; color: white; }
.user-code { font-size: 12px; }
</style>
