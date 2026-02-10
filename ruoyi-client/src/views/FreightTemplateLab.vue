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
        <div class="user-info" @click="openProfileModal" title="Click to edit profile">
          <i class="fas fa-user-circle"></i>
          <span class="user-abbr">{{ userAbbr }}</span>
          <span class="user-code">{{ currentUser }}</span>
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
                    <td class="original-text-cell" :title="item.original_text">{{ item.original_text }}</td>
                    <td>
                      <input v-model="item.placeholder_key" class="lab-input" placeholder="变量名..." spellcheck="false">
                    </td>
                    <td>
                      <span class="type-badge" :class="item.data_type">{{ item.data_type || 'string' }}</span>
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
      if (!container) return
      const spans = container.querySelectorAll('span')
      spans.forEach(s => {
        if (s.innerText.includes(row.original_text)) s.classList.add('lab-highlight')
      })
    }

    const handleMouseLeave = () => {
      const container = document.getElementById('preview-container')
      if (!container) return
      container.querySelectorAll('.lab-highlight').forEach(el => el.classList.remove('lab-highlight'))
    }

    return {
      file, mappings, previewLoading, analyzeLoading, currentUser, userAbbr,
      isProfileModalOpen, profileLoading, profileError, profileSuccess, profileForm,
      handleFileChange, syncPreview, handleSave, removeMapping, handleMouseEnter, handleMouseLeave,
      handleLogout, openProfileModal, closeProfileModal, handleUpdateProfile
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
  min-height: 100vh;
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
  position: relative;
  z-index: 50;
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
  flex: 1; /* Occupy remaining vertical space */
  min-height: 0; /* Critical for nested flex scrolling */
  overflow: hidden; /* Prevent workspace itself from scrolling */
}

.glass-card {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  padding: 25px;
  display: flex;
  flex-direction: column;
  height: 100%; /* Force full height of parent */
  overflow: hidden; /* Contain children */
}

.mapping-pane { 
  flex: 5.5; 
  min-width: 0; /* Allow shrinking below content size */
  display: flex; 
  flex-direction: column; 
}
.preview-pane { 
  flex: 4.5; 
  min-width: 0; /* Allow shrinking below content size */
  position: relative; 
  display: flex; 
  flex-direction: column; 
}

.pane-header {
  margin-bottom: 20px;
  border-bottom: 1px solid var(--glass-border);
  padding-bottom: 15px;
  flex-shrink: 0;
}

.pane-header h3 { font-size: 18px; display: flex; align-items: center; gap: 10px; }

.table-container { 
  flex: 1; 
  overflow-x: auto; /* Allow horizontal scroll if table is still too wide */
  overflow-y: auto; 
  min-height: 0;
}

.lab-table { 
  width: 100%; 
  border-collapse: collapse; 
  table-layout: fixed; /* Force fixed layout to prevent content-based expansion */
}

.lab-table th { 
  text-align: left; 
  padding: 12px; 
  font-size: 13px; 
  color: var(--text-dim); 
  border-bottom: 1px solid var(--glass-border); 
  position: sticky; 
  top: 0; 
  background: #0f172a; 
  z-index: 10; 
}

/* Specific column widths for 5.5 flex mapping pane */
.lab-table th:nth-child(1), .lab-table td:nth-child(1) { width: 35%; } /* 原文 */
.lab-table th:nth-child(2), .lab-table td:nth-child(2) { width: 25%; } /* 变量名 */
.lab-table th:nth-child(3), .lab-table td:nth-child(3) { width: 15%; } /* 类型 */
.lab-table th:nth-child(4), .lab-table td:nth-child(4) { width: 20%; } /* 说明 */
.lab-table th:nth-child(5), .lab-table td:nth-child(5) { width: 40px; } /* 删除按钮 */

.lab-table td { padding: 12px; border-bottom: 1px solid var(--glass-border); }

.original-text-cell { 
  max-width: 100%; 
  overflow: hidden; 
  text-overflow: ellipsis; 
  white-space: nowrap; 
  font-size: 13px; 
}

/* ... type badges ... */

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

/* Buttons */
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
  flex: 1;
  background: #f1f5f9; /* 浅灰色背景，模拟桌面 */
  border-radius: 16px;
  overflow: auto; /* Internal scrolling */
  padding: 20px; /* Comfortable padding */
  display: flex; /* Flex again for better centering control if width is fixed */
  flex-direction: column;
  align-items: center;
  box-shadow: inset 0 2px 10px rgba(0,0,0,0.2);
  min-height: 0; /* Allow shrinking */
  position: relative;
}

/* 针对 docx-preview 生成内容的深度美化 */
:deep(.docx-wrapper) {
  background: transparent !important;
  padding: 0 !important; /* Let container handle padding */
  display: block !important;
  /* Use Zoom instead of Scale to avoid layout space issues when possible, typical for docx-preview */
  /* Fallback to scale if zoom not supported, but scale affects layout flow differently */
  /* For now, keep scale but ensure container clips it properly */
  transform: scale(0.65);
  transform-origin: top center;
  /* Hack to reduce effective height taken by scaled element */
  margin-bottom: -35% !important; 
}

:deep(.docx) {
  background: white !important;
  box-shadow: 0 10px 25px rgba(0,0,0,0.15) !important;
  border-radius: 4px !important;
  margin-bottom: 20px !important;
  transition: transform 0.3s ease;
}

/* 滚动条美化 */
.table-container::-webkit-scrollbar,
#preview-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.table-container::-webkit-scrollbar-thumb,
#preview-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

#preview-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1); /* Darker thumb for light preview bg */
}

.table-container::-webkit-scrollbar-thumb:hover,
#preview-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

#preview-container::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}

.empty-preview {
  height: 100%; width: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94a3b8;
}
.empty-preview i { font-size: 64px; margin-bottom: 20px; }

.preview-loader, .analyze-loader {
  position: absolute; inset: 0; background: rgba(15, 23, 42, 0.8); backdrop-filter: blur(4px);
  display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 10; border-radius: 24px;
}

.analyze-loader {
   background: rgba(255,255,255,0.02); /* More subtle for table */
   color: var(--text-dim);
}

.spinner {
  width: 40px; height: 40px; border: 3px solid rgba(99, 102, 241, 0.2); border-top-color: var(--primary-color);
  border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 15px;
}

@keyframes spin { to { transform: rotate(360deg); } }

:deep(.lab-highlight) {
  background-color: rgba(99, 102, 241, 0.15) !important;
  color: var(--primary-color) !important;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.3);
  border-radius: 2px;
  font-weight: 600;
}

.sidebar-footer { 
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05); 
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 102, 241, 0.3);
}

.user-abbr {
  font-size: 16px;
  font-weight: 700;
  color: #10b981;
  letter-spacing: 1px;
}

.user-code {
  font-size: 12px;
  color: #64748b;
  font-family: monospace;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  color: #f87171;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-family: inherit;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  transform: none !important;
}

/* Profile Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40px;
}

.modal-content.profile-modal {
  width: 100%;
  max-width: 500px !important;
  max-height: 90vh;
  background: #0f172a;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  padding: 0;
  border-radius: 20px;
}

.modal-header-custom {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header-custom h2 { font-size: 20px; font-weight: 700; margin: 0; color: white; }
.modal-header-custom p { color: #94a3b8; margin: 4px 0 0 0; font-size: 13px; }

.close-btn-custom {
  background: none;
  border: none;
  color: #64748b;
  font-size: 20px;
  cursor: pointer;
}

.modal-body-custom {
  padding: 24px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group-custom {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group-custom label {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
}

.form-group-custom input {
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: white;
  width: 100%;
}

.hint { font-size: 11px; color: #64748b; }
.error-msg { color: #f87171; font-size: 13px; margin-top: 5px; }
.success-msg { color: #10b981; font-size: 13px; margin-top: 5px; }

.modal-footer-custom {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-btn-custom {
  padding: 10px 20px;
  background: var(--primary-color);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.outline-btn-custom {
  padding: 10px 20px;
  background: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 8px;
  cursor: pointer;
}

.loader {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
