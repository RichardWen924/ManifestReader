<template>
  <div class="dashboard-page">
    <!-- Sidebar (Consistent with Dashboard/History) -->
    <nav class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">
          <i class="fas fa-ship"></i>
        </div>
        <div class="logo-text">
          <h1>SHIPPING DOCFLOW</h1>
          <span>Shipping Document System</span>
        </div>
      </div>
      <ul class="nav-links">
        <li :class="{ active: $route.path === '/' }">
          <router-link to="/"><i class="fas fa-file-invoice"></i> 文档生成</router-link>
        </li>
        <li :class="{ active: $route.path === '/history' }">
          <router-link to="/history"><i class="fas fa-history"></i> 历史提单</router-link>
        </li>
        <li :class="{ active: $route.path === '/lab' }">
          <router-link to="/lab"><i class="fas fa-flask"></i> 模版生成</router-link>
        </li>
        <li :class="{ active: $route.path === '/templates' }">
          <router-link to="/templates"><i class="fas fa-layer-group"></i> 模版管理</router-link>
        </li>
        <li :class="{ active: $route.path === '/guide' }">
          <router-link to="/guide"><i class="fas fa-question-circle"></i> 使用教程</router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="user-profile" @click="openProfileModal">
          <div class="user-avatar">{{ userAbbr }}</div>
          <div class="user-info">
            <span class="name">{{ currentUser }}</span>
            <span class="role">Shipper</span>
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <i class="fas fa-sign-out-alt"></i> Logout
        </button>
      </div>
    </nav>
    
    <!-- Profile Edit Modal -->
    <div v-if="isProfileModalOpen" class="modal-overlay" @click.self="closeProfileModal">
      <div class="modal-content profile-modal card">
        <header class="modal-header-custom">
          <div class="header-left">
            <h2>Edit Profile (修改个人信息)</h2>
            <p>Update your company name and password</p>
          </div>
          <button @click="closeProfileModal" class="close-btn-custom"><i class="fas fa-times"></i></button>
        </header>

        <div class="modal-body-custom">
          <form @submit.prevent="handleUpdateProfile" class="profile-form">
            <div class="form-group-custom">
              <label>Company Name (公司名称)</label>
              <input v-model="profileForm.companyName" placeholder="Enter new company name">
            </div>

            <div class="form-group-custom">
              <label>Shipline Code (航司四字码)</label>
              <input v-model="profileForm.companyAbbr" placeholder="e.g. MSKU" maxlength="4">
              <small class="hint">Must be 4 uppercase letters</small>
            </div>
            
            <div class="form-group-custom">
              <label>New Password (新密码)</label>
              <input v-model="profileForm.password" type="password" placeholder="Leave blank to keep current password">
            </div>

            <div class="form-group-custom">
              <label>Confirm Password (确认密码)</label>
              <input v-model="profileForm.confirmPassword" type="password" placeholder="Repeat new password">
            </div>
            
            <div v-if="profileError" class="error-msg">{{ profileError }}</div>
            <div v-if="profileSuccess" class="success-msg">{{ profileSuccess }}</div>
          </form>
        </div>

        <footer class="modal-footer-custom">
          <button @click="closeProfileModal" class="outline-btn-custom">Cancel</button>
          <button @click="handleUpdateProfile" :disabled="profileLoading" class="primary-btn-custom">
            <span v-if="!profileLoading">Save Changes</span>
            <span v-else class="loader"></span>
          </button>
        </footer>
      </div>
    </div>

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
            <button class="btn-warning" @click="handleSave" :disabled="!file || analyzeLoading || mappings.length === 0">
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
                    <th>占位符变量 (key)</th>
                    <th>类型</th>
                    <th>说明</th>
                    <th>原文内容</th>
                    <th width="40"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in mappings" :key="index" 
                      @mouseenter="handleMouseEnter(item)" 
                      @mouseleave="handleMouseLeave">
                    <td>
                      <input v-model="item.placeholder_key" class="lab-input" placeholder="变量名..." spellcheck="false">
                    </td>
                    <td>
                      <span class="type-badge" :class="item.data_type">{{ item.data_type || 'string' }}</span>
                    </td>
                    <td>
                      <input v-model="item.description" class="lab-input" placeholder="字段说明..." spellcheck="false">
                    </td>
                    <td class="original-text-cell" @click="showOriginalText(item)">
                      <span class="text-preview">{{ item.original_text }}</span>
                      <i class="fas fa-search-plus text-peek-icon"></i>
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

    <!-- Original Text Detail Modal -->
    <div v-if="textModalOpen" class="modal-overlay" @click.self="textModalOpen = false">
      <div class="modal-box text-modal">
        <div class="modal-header">
          <h3><i class="fas fa-file-alt"></i> 原文内容详情</h3>
          <button class="modal-close" @click="textModalOpen = false"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div class="text-detail-field">
            <label>占位符</label>
            <span class="text-detail-value code" v-text="'{{' + textModalItem.placeholder_key + '}}'"></span>
          </div>
          <div class="text-detail-field">
            <label>说明</label>
            <span class="text-detail-value">{{ textModalItem.description }}</span>
          </div>
          <div class="text-detail-field">
            <label>原文内容</label>
            <pre class="text-detail-content">{{ textModalItem.original_text }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// @author Richard
import { defineComponent, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { renderAsync } from 'docx-preview'
import { analyzeTemplate, previewTemplate, saveTemplate } from '../api/lab'
import api from '../api/request'

export default defineComponent({
  name: 'FreightTemplateLab',
  setup() {
    const router = useRouter()
    const file = ref(null)
    const mappings = ref([])
    const previewLoading = ref(false)
    const analyzeLoading = ref(false)
    const currentUser = ref('')
    const userAbbr = ref('Loading...')
    const textModalOpen = ref(false)
    const textModalItem = ref({})

    // Profile Modal States
    const isProfileModalOpen = ref(false)
    const profileLoading = ref(false)
    const profileError = ref('')
    const profileSuccess = ref('')
    const profileForm = ref({
      companyName: '',
      companyAbbr: '',
      password: '',
      confirmPassword: ''
    })

    const fetchUserData = async () => {
      try {
        const res = await api.get('/client-api/current-user')
        if (res.code === 200 || res.code === 0) {
          userAbbr.value = res.data.companyAbbr
          currentUser.value = res.data.companyCode
          profileForm.value.companyName = res.data.companyName
          profileForm.value.companyAbbr = res.data.companyAbbr
        }
      } catch (err) {
        console.error('Failed to fetch user data:', err)
      }
    }

    const openProfileModal = () => {
      isProfileModalOpen.value = true
      profileError.value = ''
      profileSuccess.value = ''
      profileForm.value.password = ''
      profileForm.value.confirmPassword = ''
      fetchUserData()
    }

    const closeProfileModal = () => {
      isProfileModalOpen.value = false
    }

    const handleUpdateProfile = async () => {
      if (profileForm.value.password && profileForm.value.password !== profileForm.value.confirmPassword) {
        profileError.value = 'Passwords do not match'
        return
      }
      if (profileForm.value.companyAbbr && !/^[A-Z]{4}$/.test(profileForm.value.companyAbbr.toUpperCase())) {
        profileError.value = 'Shipline Code must be 4 uppercase letters'
        return
      }

      profileLoading.value = true
      profileError.value = ''
      profileSuccess.value = ''

      try {
        const res = await api.post('/client-api/update-profile', {
          companyName: profileForm.value.companyName,
          companyAbbr: profileForm.value.companyAbbr,
          password: profileForm.value.password
        })
        if (res.code === 200 || res.code === 0) {
          profileSuccess.value = 'Profile updated successfully!'
          fetchUserData()
          setTimeout(() => closeProfileModal(), 1500)
        } else {
          profileError.value = res.msg || 'Update failed'
        }
      } catch (err) {
        profileError.value = err.message || 'Network error'
      } finally {
        profileLoading.value = false
      }
    }

    onMounted(async () => {
      // 检查 Session
      try {
        await api.get('/client-api/check-auth')
        await fetchUserData()
      } catch (err) {
        router.push('/login')
      }
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
        const raw = data.data || []
        // 按 placeholder_key 去重，保留第一条
        const seen = new Set()
        mappings.value = raw.filter(m => {
          if (!m.placeholder_key) return true
          if (seen.has(m.placeholder_key)) return false
          seen.add(m.placeholder_key)
          return true
        })
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
      // 校验变量名是否完整
      const incomplete = mappings.value.some(m => !m.placeholder_key || !m.placeholder_key.trim())
      if (incomplete) {
        alert('请确保所有识别到的字段都已填写“占位符变量名”再保存')
        return
      }

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

    const handleLogout = async () => {
      try {
        await api.post('/client-api/logout')
        localStorage.removeItem('client_user')
        router.push('/login')
      } catch (err) {
        localStorage.removeItem('client_user')
        router.push('/login')
      }
    }

    const removeMapping = (idx) => {
      mappings.value.splice(idx, 1)
    }

    const handleMouseEnter = (row) => {
      const container = document.getElementById('preview-container')
      if (!container || !row.original_text) return
      // 用原文第一行（去换行）作为搜索关键词，解决长文本/多行文本匹配问题
      const firstLine = row.original_text.split('\n')[0].trim()
      if (!firstLine || firstLine.length < 2) return
      const spans = container.querySelectorAll('span')
      spans.forEach(s => {
        if (s.innerText && s.innerText.includes(firstLine)) s.classList.add('lab-highlight')
      })
    }

    const handleMouseLeave = () => {
      const container = document.getElementById('preview-container')
      if (!container) return
      container.querySelectorAll('.lab-highlight').forEach(el => el.classList.remove('lab-highlight'))
    }

    const showOriginalText = (item) => {
      textModalItem.value = item
      textModalOpen.value = true
    }

    return {
      file, mappings, previewLoading, analyzeLoading, currentUser, userAbbr,
      isProfileModalOpen, profileLoading, profileError, profileSuccess, profileForm,
      textModalOpen, textModalItem,
      handleFileChange, syncPreview, handleSave, removeMapping, handleMouseEnter, handleMouseLeave,
      handleLogout, openProfileModal, closeProfileModal, handleUpdateProfile, showOriginalText
    }
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-light);
}

/* Sidebar */
.sidebar {
  width: 260px;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
}

.sidebar-header {
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--primary-gradient);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
}

.logo-text h1 {
  font-size: 14px;
  color: var(--text-main);
  font-weight: 800;
  margin: 0;
  letter-spacing: 0.5px;
}

.logo-text span {
  font-size: 10px;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: block;
  margin-top: 2px;
}

.nav-links {
  list-style: none;
  padding: 8px 12px;
  flex: 1;
}

.nav-links li {
  margin-bottom: 4px;
}

.nav-links a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  color: var(--text-dim);
  text-decoration: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-links a i {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.nav-links li.active a {
  background: #eff6ff;
  color: var(--primary-color);
  font-weight: 600;
}

.nav-links li.active i {
  color: var(--primary-color);
}

.nav-links a:hover:not(.active a) {
  background: #f8fafc;
  color: var(--text-main);
}

.sidebar-footer {
  margin-top: auto;
  padding: 24px 16px;
  border-top: 1px solid var(--border-color);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
}

.user-profile:hover {
  background: #f1f5f9;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: #e2e8f0;
  color: var(--text-main);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.user-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.user-info .name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info .role {
  font-size: 11px;
  color: var(--text-dim);
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  color: #ef4444;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-btn:hover {
  background: #fee2e2;
}

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 48px 56px;
  background: var(--bg-light);
  min-height: 100vh;
}

.template-lab-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 96px);
}

.lab-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
  position: relative;
  z-index: 50;
}

.title-area h2 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.badge {
  font-size: 12px;
  font-weight: 700;
  background: var(--primary-gradient);
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  vertical-align: middle;
}

.title-area p {
  color: var(--text-dim);
  font-size: 16px;
}

.actions { display: flex; gap: 12px; }

.lab-workspace {
  display: flex;
  gap: 24px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.glass-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.mapping-pane { flex: 5.5; min-width: 0; display: flex; flex-direction: column; }
.preview-pane { flex: 4.5; min-width: 0; position: relative; display: flex; flex-direction: column; }

.pane-header {
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
  flex-shrink: 0;
}

.pane-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: auto;
  min-height: 0;
}

.lab-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.lab-table th {
  text-align: left;
  padding: 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-dim);
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
}

.lab-table td {
  padding: 12px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

/* Specific column widths */
.lab-table th:nth-child(1), .lab-table td:nth-child(1) { width: 22%; }
.lab-table th:nth-child(2), .lab-table td:nth-child(2) { width: 12%; }
.lab-table th:nth-child(3), .lab-table td:nth-child(3) { width: 22%; }
.lab-table th:nth-child(4), .lab-table td:nth-child(4) { width: 34%; }
.lab-table th:nth-child(5), .lab-table td:nth-child(5) { width: 50px; }

.original-text-cell {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: var(--text-main);
  cursor: pointer;
}

.original-text-cell:hover {
  color: var(--primary-color);
}

.type-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #64748b;
}

.type-badge.string { background: #eff6ff; color: #3b82f6; }
.type-badge.number { background: #ecfdf5; color: #10b981; }

.lab-input {
  width: 100%;
  padding: 8px 12px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-main);
  font-size: 13px;
  transition: all 0.2s;
}

.lab-input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: white;
}

/* Buttons */
.btn-primary, .btn-success, .btn-warning {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  border: none;
  color: white;
}

.btn-primary { background: var(--primary-gradient); }
.btn-success { background: #10b981; }
.btn-warning { background: #f59e0b; }

.btn-primary:hover, .btn-success:hover, .btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-icon-delete {
  background: none;
  border: none;
  color: #ef4444;
  opacity: 0.6;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
}

.btn-icon-delete:hover {
  opacity: 1;
  transform: scale(1.1);
}

#preview-container {
  flex: 1;
  background: #f8fafc;
  border-radius: 16px;
  overflow: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: inset 0 2px 8px rgba(0,0,0,0.05);
  min-height: 0;
  position: relative;
}

:deep(.docx-wrapper) {
  background: transparent !important;
  padding: 0 !important;
  transform: scale(0.7);
  transform-origin: top center;
  margin-bottom: -30% !important;
}

:deep(.docx) {
  background: white !important;
  box-shadow: 0 10px 25px rgba(0,0,0,0.08) !important;
  border-radius: 4px !important;
}

.preview-loader, .analyze-loader {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
  border-radius: 20px;
  color: var(--text-main);
  font-weight: 600;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e8f0;
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}

.modal-content.profile-modal, .text-modal {
  background: white;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 100%;
  overflow: hidden;
}

.modal-header-custom, .text-modal .modal-header {
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header-custom h2, .text-modal .modal-header h3 {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0;
}

.modal-body-custom, .text-modal .modal-body {
  padding: 32px;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  color: #ef4444;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.profile-form { display: flex; flex-direction: column; gap: 16px; }

.form-group-custom label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-dim);
  margin-bottom: 8px;
}

.form-group-custom input {
  width: 100%;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  color: var(--text-main);
  font-size: 14px;
}

.form-group-custom input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.modal-footer-custom {
  padding: 20px 32px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-btn-custom {
  padding: 10px 20px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

.outline-btn-custom {
  padding: 10px 20px;
  background: white;
  border: 1px solid var(--border-color);
  color: var(--text-main);
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

/* Text Detail Modal */
.text-detail-field { margin-bottom: 20px; }
.text-detail-field label { font-size: 12px; font-weight: 700; color: var(--text-dim); text-transform: uppercase; margin-bottom: 8px; display: block; }
.text-detail-value { color: var(--text-main); font-size: 14px; font-weight: 600; }
.text-detail-value.code { font-family: monospace; color: var(--primary-color); background: #eff6ff; padding: 4px 8px; border-radius: 4px; }
.text-detail-content {
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  color: var(--text-main);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
}

:deep(.lab-highlight) {
  background-color: rgba(59, 130, 246, 0.1) !important;
  color: var(--primary-color) !important;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
  border-radius: 2px;
  font-weight: 700;
}
</style>
