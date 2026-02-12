<template>
  <div class="template-management-page">
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
        <div class="upgrade-link" @click="router.push('/upgrade')" title="Account Upgrade">
          <i class="fas fa-shopping-cart"></i>
          <span>Account Upgrade</span>
        </div>
        <div class="user-profile" @click="router.push('/profile')">
          <div class="user-avatar">{{ userAbbr }}</div>
          <div class="user-info">
            <div class="name-row">
              <span class="name">{{ currentUserDisplay }}</span>
              <span v-if="isVip" class="vip-badge">VIP</span>
            </div>
            <span class="role">{{ isVip ? 'Premium Member' : 'Shipper' }}</span>
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <i class="fas fa-sign-out-alt"></i> Logout
        </button>
      </div>
    </nav>
    
    <!-- Profile Edit Modal Removed (Moved to Profile page) -->

    <main class="main-content">
      <!-- Notion-style page header -->
      <div class="page-header">
        <div class="header-main">
          <div class="page-icon"><i class="fas fa-layer-group"></i></div>
          <div class="page-title-area">
            <h1>Template Management</h1>
            <p class="page-subtitle">Manage your document templates</p>
          </div>
        </div>
        <div class="header-actions">
          <router-link to="/lab" class="new-btn primary-gradient-btn">
            <i class="fas fa-plus"></i> New Template
          </router-link>
        </div>
      </div>

      <!-- Toolbar row -->
      <div class="toolbar">
        <div class="toolbar-left">
          <div class="search-input-wrapper">
            <i class="fas fa-search"></i>
            <input v-model="queryParams.templateName" placeholder="Search templates..." @keyup.enter="handleQuery">
          </div>
          <span class="record-count" v-if="templateList.length > 0">{{ templateList.length }} templates record</span>
        </div>
      </div>

      <!-- Notion-style table -->
      <div class="table-container">
        <table class="notion-table">
          <thead>
            <tr>
              <th class="check-cell">
                <input type="checkbox" class="n-checkbox" :checked="allSelected" @change="toggleSelectAll">
              </th>
              <th class="col-name">Name</th>
              <th class="col-code">Code</th>
              <th class="col-time">Created</th>
              <th class="actions-cell"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in templateList" :key="item.templateId"
                :class="{ 'row-checked': selectedIds.includes(item.templateId) }">
              <td class="check-cell">
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
              <td class="col-code"><span class="template-code">{{ item.templateCode }}</span></td>
              <td class="col-time">{{ formatTime(item.createTime) }}</td>
              <td class="actions-cell">
                <div class="row-actions">
                  <button class="icon-btn" @click="handleUpdate(item)" title="Edit">
                    <i class="fas fa-pen"></i>
                  </button>
                  <button class="icon-btn delete-btn" @click="handleDelete(item)" title="Delete">
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
        <div v-if="selectedIds.length > 0" class="floating-bar" :class="{ active: selectedIds.length > 0 }">
          <span class="count">{{ selectedIds.length }} selected</span>
          <div class="actions">
            <button class="bar-btn danger" @click="handleBatchDelete">
              <i class="fas fa-trash-alt"></i> Delete Selected
            </button>
            <button class="bar-btn" @click="selectedIds = []">
              <i class="fas fa-times"></i> Clear Selection
            </button>
          </div>
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
// @author Richard
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listTemplate, getTemplate, delTemplate, updateTemplate } from '../api/template'
import api from '../api/request'

const router = useRouter()
const loading = ref(true)
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
const currentUserDisplay = computed(() => currentUser.value)
const userAbbr = ref('Loading...')
const isVip = ref(false)
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

const fetchUserData = async () => {
  try {
    const res = await api.get('/client-api/current-user')
    if (res.code === 200 || res.code === 0) {
      userAbbr.value = res.data.companyAbbr
      isVip.value = res.data.vipStatus === '1'
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
      if (!isVip.value && res.data && res.data.length > 2) {
        templateList.value = res.data.slice(0, 2)
      } else {
        templateList.value = res.data
      }
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
    localStorage.removeItem('client_user')
    router.push('/login')
  } catch (err) {
    localStorage.removeItem('client_user')
    router.push('/login')
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
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upgrade-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(37, 99, 235, 0.05));
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  color: #fbbf24;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.upgrade-link:hover {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.upgrade-link i {
  font-size: 16px;
  color: #f59e0b;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.vip-badge {
  padding: 2px 6px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border-radius: 4px;
  color: white;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.5px;
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

/* Notion-style Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 24px;
}

.primary-gradient-btn {
  background: var(--primary-gradient) !important;
  color: white !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.primary-gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

.page-icon {
  font-size: 48px;
  color: var(--text-main);
}

.page-title-area h1 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 4px;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-dim);
}

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--text-dim);
  font-size: 14px;
}

.search-input-wrapper {
  position: relative;
  width: 320px;
}

.search-input-wrapper i {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-dim);
  font-size: 15px;
  pointer-events: none;
}

.search-input-wrapper input {
  width: 100%;
  padding: 10px 16px 10px 42px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-main);
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-input-wrapper input:focus {
  outline: none;
  background: white;
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
  width: 360px;
}

.new-btn {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.new-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

/* Table Design */
.table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.notion-table {
  width: 100%;
  border-collapse: collapse;
}

.notion-table th {
  text-align: left;
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-color);
  background: #f8fafc;
}

.notion-table tr {
  transition: background 0.2s;
}

.notion-table tr:hover {
  background: #f8fafc;
}

.notion-table td {
  padding: 16px;
  font-size: 14px;
  color: var(--text-main);
  border-bottom: 1px solid #f1f5f9;
}

.check-cell { width: 40px; }
.actions-cell { width: 100px; text-align: right; }

.template-code {
  font-family: monospace;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--primary-color);
}

.row-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

tr:hover .row-actions {
  opacity: 1;
}

.icon-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #e2e8f0;
  color: var(--text-main);
}

.delete-btn:hover {
  color: #ef4444;
}

/* Floating Selection Bar */
.floating-bar {
  position: fixed;
  bottom: 0px;
  left: 50%;
  transform: translateX(-50%) translateY(100px);
  background: #1e293b;
  color: white;
  padding: 16px 32px;
  border-radius: 20px 20px 0 0;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 -10px 25px -5px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.floating-bar.active {
  transform: translateX(-50%) translateY(-24px);
}

.floating-bar .count { font-weight: 600; border-right: 1px solid rgba(255,255,255,0.2); padding-right: 20px; }
.floating-bar .actions { display: flex; gap: 12px; }

.bar-btn {
  background: none;
  border: none;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.bar-btn:hover { background: rgba(255, 255, 255, 0.1); }
.bar-btn.danger { color: #f87171; }
.bar-btn.danger:hover { background: rgba(239, 68, 68, 0.2); }

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  color: var(--text-dim);
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 24px;
  color: #e2e8f0;
}

/* Modal Styling */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 { font-size: 18px; font-weight: 700; margin: 0; }

.modal-body { padding: 24px; }

.form-group-custom label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-dim);
  margin-bottom: 8px;
}

.form-group-custom input, .form-group-custom textarea {
  width: 100%;
  padding: 10px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-btn {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.outline-btn {
  background: white;
  border: 1px solid var(--border-color);
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
</style>
