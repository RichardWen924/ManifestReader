<template>
  <div class="template-management-page">
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
        <li :class="{ active: $route.path === '/templates' }">
          <router-link to="/templates"><i class="fas fa-layer-group"></i> Template Management</router-link>
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
        <header class="modal-header">
          <div class="header-left">
            <h2>Edit Profile (修改个人信息)</h2>
            <p>Update your company name and password</p>
          </div>
          <button @click="closeProfileModal" class="close-btn"><i class="fas fa-times"></i></button>
        </header>

        <div class="modal-body">
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

        <footer class="modal-footer">
          <button @click="closeProfileModal" class="outline-btn">Cancel</button>
          <button @click="handleUpdateProfile" :disabled="profileLoading" class="primary-btn">
            <span v-if="!profileLoading">Save Changes</span>
            <span v-else class="loader"></span>
          </button>
        </footer>
      </div>
    </div>

    <main class="main-content">
      <!-- Notion-style page header -->
      <div class="page-header">
        <div class="page-icon"><i class="fas fa-layer-group"></i></div>
        <div class="page-title-area">
          <h1>Template Management</h1>
          <p class="page-subtitle">Manage your document templates</p>
        </div>
      </div>

      <!-- Toolbar row -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="record-count" v-if="templateList.length > 0">{{ templateList.length }} templates</span>
        </div>
        <div class="toolbar-right">
          <div class="search-inline">
            <i class="fas fa-search"></i>
            <input v-model="queryParams.templateName" placeholder="Filter..." @keyup.enter="handleQuery">
          </div>
          <router-link to="/lab" class="new-btn">
            <i class="fas fa-plus"></i> New
          </router-link>
        </div>
      </div>

      <!-- Notion-style table -->
      <div class="notion-table-wrap">
        <table class="notion-table">
          <thead>
            <tr>
              <th class="col-check">
                <input type="checkbox" class="n-checkbox" :checked="allSelected" @change="toggleSelectAll">
              </th>
              <th class="col-name">Name</th>
              <th class="col-code">Code</th>
              <th class="col-time">Created</th>
              <th class="col-actions"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in templateList" :key="item.templateId"
                :class="{ 'row-checked': selectedIds.includes(item.templateId) }">
              <td class="col-check">
                <input type="checkbox" class="n-checkbox"
                       :checked="selectedIds.includes(item.templateId)"
                       @change="toggleSelect(item.templateId)">
              </td>
              <td class="col-name">
                <div class="name-cell">
                  <i class="fas fa-file-alt file-icon"></i>
                  <span>{{ item.templateName }}</span>
                </div>
              </td>
              <td class="col-code"><span class="code-tag">{{ item.templateCode }}</span></td>
              <td class="col-time">{{ formatTime(item.createTime) }}</td>
              <td class="col-actions">
                <div class="row-actions">
                  <button class="act-btn" @click="handleUpdate(item)" title="Edit">
                    <i class="fas fa-pen"></i>
                  </button>
                  <button class="act-btn act-danger" @click="handleDelete(item)" title="Delete">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="templateList.length === 0 && !loading" class="empty-state">
          <div class="empty-icon"><i class="fas fa-file-circle-plus"></i></div>
          <p>No templates yet</p>
          <router-link to="/lab" class="empty-action">Create your first template</router-link>
        </div>
      </div>

      <!-- Floating selection bar (Notion-style) -->
      <transition name="slide-up">
        <div v-if="selectedIds.length > 0" class="floating-bar">
          <span class="bar-count">{{ selectedIds.length }} selected</span>
          <button class="bar-btn bar-delete" @click="handleBatchDelete">
            <i class="fas fa-trash-alt"></i> Delete
          </button>
          <button class="bar-btn bar-clear" @click="selectedIds = []">
            <i class="fas fa-times"></i> Clear
          </button>
        </div>
      </transition>
    </main>

    <!-- Edit Template Modal -->
    <div v-if="open" class="modal-overlay" @click.self="open = false">
      <div class="modal-content profile-modal card">
        <header class="modal-header">
          <div class="header-left">
            <h2>Edit Template</h2>
            <p>Modify template information</p>
          </div>
          <button @click="open = false" class="close-btn"><i class="fas fa-times"></i></button>
        </header>

        <div class="modal-body">
          <form @submit.prevent="submitForm" class="profile-form">
            <div class="form-group-custom">
              <label>Template Name</label>
              <input v-model="form.templateName" placeholder="Enter template name">
            </div>
            <div class="form-group-custom">
              <label>Template Code</label>
              <input v-model="form.templateCode" placeholder="Enter template code">
            </div>
            <div class="form-group-custom">
              <label>Remark</label>
              <textarea v-model="form.remark" rows="3" class="custom-textarea" placeholder="Enter remark"></textarea>
            </div>
          </form>
        </div>

        <footer class="modal-footer">
          <button @click="open = false" class="outline-btn">Cancel</button>
          <button @click="submitForm" class="primary-btn">Save</button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listTemplate, getTemplate, delTemplate, updateTemplate } from '../api/template'
import api from '../api/request'

const router = useRouter()
const loading = ref(true)
const templateList = ref([])
const open = ref(false)
const form = ref({})
const selectedIds = ref([])
const queryParams = ref({
  templateName: ''
})

const allSelected = computed(() => {
  return templateList.value.length > 0 && selectedIds.value.length === templateList.value.length
})

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = templateList.value.map(t => t.templateId)
  }
}

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handleBatchDelete = async () => {
  if (!confirm(`Are you sure to delete ${selectedIds.value.length} selected templates?`)) return
  try {
    const res = await delTemplate(selectedIds.value.join(','))
    if (res.code === 200 || res.code === 0) {
      alert('Deleted successfully')
      selectedIds.value = []
      getList()
    }
  } catch (err) {
    alert('Batch delete failed')
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  if (isNaN(d)) return timeStr
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
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
      profileForm.value.companyName = res.data.companyName
      profileForm.value.companyAbbr = res.data.companyAbbr
    }
  } catch (err) {
    console.error('Failed to fetch user data:', err)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listTemplate(queryParams.value)
    if (res.code === 200 || res.code === 0) {
      templateList.value = res.data
    }
  } catch (err) {
    console.error('Fetch templates failed', err)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  getList()
}

const handleUpdate = (row) => {
  form.value = { ...row }
  open.value = true
}

const handleDelete = async (row) => {
  if (confirm(`Are you sure to delete template "${row.templateName}"?`)) {
    try {
      const res = await delTemplate(row.templateId)
      if (res.code === 200 || res.code === 0) {
        alert('Deleted successfully')
        getList()
      }
    } catch (err) {
      alert('Delete failed')
    }
  }
}

const submitForm = async () => {
  try {
    const res = await updateTemplate(form.value)
    if (res.code === 200 || res.code === 0) {
      alert('Updated successfully')
      open.value = false
      getList()
    }
  } catch (err) {
    alert('Update failed')
  }
}

const handleLogout = async () => {
  try {
    await api.post('/client-api/logout')
    localStorage.removeItem('client_token')
    localStorage.removeItem('client_user')
    router.push('/login')
  } catch (err) {
    console.error('Logout failed:', err)
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

onMounted(() => {
  getList()
  fetchUserData()
})
</script>

<style scoped>
.template-management-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-dark);
  color: white;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* ===== Sidebar (unified) ===== */
.sidebar {
  width: 260px;
  background: var(--bg-card);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.sidebar-brand { display: flex; align-items: center; gap: 12px; margin-bottom: 40px; }
.sidebar-logo { width: 36px; height: 36px; background: var(--primary-gradient); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; }
.sidebar-logo-text h2 { font-size: 16px; margin: 0; font-weight: 700; }
.sidebar-logo-text span { font-size: 11px; color: rgba(255,255,255,0.4); }

.nav-links { list-style: none; padding: 0; flex: 1; }
.nav-links li a { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-radius: 10px; color: rgba(255,255,255,0.55); text-decoration: none; font-size: 14px; font-weight: 500; transition: all 0.2s; margin-bottom: 4px; }
.nav-links li a:hover { background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.85); }
.nav-links li a.active { background: rgba(99, 102, 241, 0.12); color: #a5b4fc; }
.nav-links li a i { width: 20px; text-align: center; font-size: 15px; }
.nav-links li a .badge-count { margin-left: auto; background: rgba(99,102,241,0.2); color: #818cf8; padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: 600; }

.sidebar-footer { border-top: 1px solid rgba(255,255,255,0.06); padding-top: 16px; display: flex; flex-direction: column; gap: 12px; }
.user-profile { display: flex; align-items: center; gap: 12px; }
.user-avatar { width: 36px; height: 36px; border-radius: 10px; background: var(--primary-gradient); display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px; }
.user-info p { margin: 0; }
.user-info .name { font-size: 14px; font-weight: 600; }
.user-info .role { font-size: 12px; color: rgba(255,255,255,0.4); }
.logout-btn { padding: 10px 16px; background: rgba(239,68,68,0.08); border: 1px solid rgba(239,68,68,0.15); border-radius: 10px; color: #f87171; font-weight: 500; font-size: 13px; cursor: pointer; transition: all 0.2s; }
.logout-btn:hover { background: rgba(239,68,68,0.15); }

/* ===== Main Content ===== */
.main-content {
  flex: 1;
  padding: 48px 56px;
  overflow-y: auto;
  position: relative;
}

/* ===== Page Header (Notion-style) ===== */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}
.page-icon {
  width: 48px; height: 48px;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.12);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; color: #818cf8;
}
.page-title-area h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }
.page-subtitle { margin: 4px 0 0; font-size: 14px; color: rgba(255,255,255,0.4); }

/* ===== Toolbar ===== */
.toolbar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.toolbar-left { display: flex; align-items: center; gap: 12px; }
.toolbar-right { display: flex; align-items: center; gap: 10px; }
.record-count { font-size: 13px; color: rgba(255,255,255,0.35); font-weight: 500; }

.search-inline {
  display: flex; align-items: center; gap: 8px;
  padding: 7px 14px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px; transition: all 0.2s;
}
.search-inline:focus-within { border-color: rgba(99,102,241,0.4); background: rgba(255,255,255,0.06); }
.search-inline i { font-size: 12px; color: rgba(255,255,255,0.3); }
.search-inline input { background: none; border: none; color: white; font-size: 13px; outline: none; width: 140px; }
.search-inline input::placeholder { color: rgba(255,255,255,0.25); }

.new-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 16px;
  background: rgba(99,102,241,0.15); border: 1px solid rgba(99,102,241,0.3);
  border-radius: 8px; color: #a5b4fc; font-weight: 600; font-size: 13px;
  text-decoration: none; transition: all 0.2s;
}
.new-btn:hover { background: rgba(99,102,241,0.25); border-color: #818cf8; }

/* ===== Notion Table ===== */
.notion-table-wrap { min-height: 200px; }
.notion-table { width: 100%; border-collapse: collapse; }

.notion-table thead th {
  padding: 8px 12px; font-size: 12px; font-weight: 500;
  color: rgba(255,255,255,0.35); text-transform: uppercase;
  letter-spacing: 0.6px; border-bottom: 1px solid rgba(255,255,255,0.06);
  text-align: left;
}

.notion-table tbody tr { transition: background 0.15s; }
.notion-table tbody tr:hover { background: rgba(255,255,255,0.03); }
.notion-table tbody tr.row-checked { background: rgba(99,102,241,0.06); }

.notion-table tbody td {
  padding: 12px; font-size: 14px; color: rgba(255,255,255,0.75);
  border-bottom: 1px solid rgba(255,255,255,0.04); vertical-align: middle;
}

.col-check { width: 40px; }
.col-name { min-width: 200px; }
.col-code { width: 180px; }
.col-time { width: 150px; }
.col-actions { width: 100px; }

.n-checkbox { width: 16px; height: 16px; accent-color: #818cf8; cursor: pointer; border-radius: 4px; }

.name-cell { display: flex; align-items: center; gap: 10px; }
.file-icon { color: #60a5fa; font-size: 15px; }
.name-cell span { font-weight: 500; color: rgba(255,255,255,0.9); }

.code-tag {
  padding: 3px 10px; background: rgba(99,102,241,0.1); color: #a5b4fc;
  border-radius: 6px; font-size: 12px; font-weight: 600; font-family: 'JetBrains Mono', monospace;
}

.row-actions { display: flex; gap: 4px; opacity: 0; transition: opacity 0.15s; }
.notion-table tbody tr:hover .row-actions { opacity: 1; }
.act-btn {
  width: 30px; height: 30px; border: none;
  background: rgba(255,255,255,0.06); border-radius: 6px;
  color: rgba(255,255,255,0.5); cursor: pointer; font-size: 12px;
  display: flex; align-items: center; justify-content: center; transition: all 0.15s;
}
.act-btn:hover { background: rgba(255,255,255,0.1); color: white; }
.act-danger:hover { background: rgba(239,68,68,0.15); color: #f87171; }

/* ===== Empty State ===== */
.empty-state { text-align: center; padding: 72px 24px; }
.empty-icon { font-size: 40px; color: rgba(255,255,255,0.15); margin-bottom: 16px; }
.empty-state p { color: rgba(255,255,255,0.35); font-size: 15px; margin-bottom: 16px; }
.empty-action { color: #818cf8; font-weight: 600; font-size: 14px; text-decoration: none; }
.empty-action:hover { text-decoration: underline; }

/* ===== Floating Selection Bar ===== */
.floating-bar {
  position: fixed; bottom: 28px; left: 50%; transform: translateX(-50%);
  display: flex; align-items: center; gap: 12px; padding: 12px 24px;
  background: #1e293b; border: 1px solid rgba(99,102,241,0.3); border-radius: 14px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.4), 0 0 0 1px rgba(255,255,255,0.05);
  z-index: 999;
}
.bar-count { font-size: 13px; font-weight: 600; color: #a5b4fc; padding-right: 8px; border-right: 1px solid rgba(255,255,255,0.1); }
.bar-btn {
  display: flex; align-items: center; gap: 6px; padding: 7px 14px;
  border-radius: 8px; border: none; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.15s;
}
.bar-delete { background: rgba(239,68,68,0.15); color: #f87171; }
.bar-delete:hover { background: rgba(239,68,68,0.25); }
.bar-clear { background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.5); }
.bar-clear:hover { background: rgba(255,255,255,0.1); color: white; }

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateX(-50%) translateY(20px); }

/* ===== Modal ===== */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px);
  z-index: 1000; display: flex; align-items: center; justify-content: center;
}
.modal-content {
  background: var(--bg-card); border-radius: 16px; border: 1px solid rgba(255,255,255,0.08);
  width: 500px; max-height: 85vh; display: flex; flex-direction: column;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 24px 28px 16px; border-bottom: 1px solid rgba(255,255,255,0.06);
}
.modal-header h2 { margin: 0; font-size: 18px; font-weight: 700; }
.modal-header p { margin: 4px 0 0; font-size: 13px; color: rgba(255,255,255,0.4); }
.close-btn { background: none; border: none; color: rgba(255,255,255,0.3); font-size: 16px; cursor: pointer; padding: 4px; }
.close-btn:hover { color: white; }

.modal-body { padding: 24px 28px; overflow-y: auto; flex: 1; }
.modal-footer {
  padding: 16px 28px; border-top: 1px solid rgba(255,255,255,0.06);
  display: flex; justify-content: flex-end; gap: 12px;
}

/* Form */
.form-group-custom { margin-bottom: 20px; }
.form-group-custom label { display: block; font-size: 13px; font-weight: 600; color: rgba(255,255,255,0.5); margin-bottom: 8px; }
.form-group-custom input,
.form-group-custom textarea {
  width: 100%; padding: 10px 14px; background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; color: white;
  font-size: 14px; transition: border-color 0.2s; box-sizing: border-box;
}
.form-group-custom input:focus,
.form-group-custom textarea:focus { outline: none; border-color: rgba(99,102,241,0.5); }

/* Buttons */
.outline-btn {
  padding: 9px 18px; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.1);
  border-radius: 8px; color: rgba(255,255,255,0.7); font-weight: 500; font-size: 13px;
  cursor: pointer; transition: all 0.2s;
}
.outline-btn:hover { background: rgba(255,255,255,0.08); color: white; }
.primary-btn {
  padding: 9px 18px; background: var(--primary-gradient); border: none; border-radius: 8px;
  color: white; font-weight: 600; font-size: 13px; cursor: pointer; transition: opacity 0.2s;
}
.primary-btn:hover { opacity: 0.85; }

.loader {
  width: 18px; height: 18px; border: 2px solid rgba(255,255,255,0.2);
  border-radius: 50%; border-top-color: white; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>

