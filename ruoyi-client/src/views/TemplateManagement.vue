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
      <header class="content-header">
        <h1>Template Management</h1>
        <div class="header-actions">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input v-model="queryParams.templateName" placeholder="Search templates..." @keyup.enter="handleQuery">
          </div>
        </div>
      </header>

      <div class="table-container">
        <table class="custom-table" v-loading="loading">
          <thead>
            <tr>
              <th>ID</th>
              <th>Template Name</th>
              <th>Code</th>
              <th>Create Time</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in templateList" :key="item.templateId">
              <td>{{ item.templateId }}</td>
              <td>
                <div class="template-name-cell">
                  <i class="fas fa-file-word"></i>
                  {{ item.templateName }}
                </div>
              </td>
              <td><span class="badge">{{ item.templateCode }}</span></td>
              <td>{{ item.createTime }}</td>
              <td>
                <div class="action-btns">
                  <button class="icon-btn edit" @click="handleUpdate(item)" title="Edit">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="icon-btn delete" @click="handleDelete(item)" title="Delete">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div v-if="templateList.length === 0 && !loading" class="empty-state">
           <i class="fas fa-folder-open"></i>
           <p>No templates found.</p>
           <router-link to="/lab" class="primary-btn">Go to Lab</router-link>
        </div>
      </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listTemplate, getTemplate, delTemplate, updateTemplate } from '../api/template'
import api from '../api/request'

const router = useRouter()
const loading = ref(true)
const templateList = ref([])
const open = ref(false)
const form = ref({})
const queryParams = ref({
  templateName: ''
})

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
  font-family: 'Outfit', sans-serif;
}

/* Sidebar - Inherited structure */
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
  padding: 12px 16px;
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

.nav-links li.active i, .nav-links a:hover i {
  color: white;
}

.nav-links i { width: 20px; text-align: center; }

.sidebar-footer {
  margin-top: auto;
  padding: 20px;
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

.logout-btn:hover { background: rgba(239, 68, 68, 0.2); }

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 40px;
  background: var(--bg-dark);
  min-height: 100vh;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.content-header h1 {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(to right, #ffffff, #94a3b8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.search-box {
  position: relative;
  width: 300px;
}

.search-box i {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-dim);
}

.search-box input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 15px;
  color: white;
  transition: all 0.3s;
}

.search-box input:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.table-container {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.custom-table {
  width: 100%;
  border-collapse: collapse;
}

.custom-table th {
  text-align: left;
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  color: var(--text-dim);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.custom-table td {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: var(--text-main);
}

.template-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
}

.template-name-cell i {
  color: #6366f1;
}

.badge {
  padding: 4px 10px;
  background: rgba(99, 102, 241, 0.1);
  color: #818cf8;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  font-family: monospace;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  color: var(--text-dim);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn.edit:hover {
  background: rgba(99, 102, 241, 0.1);
  color: #818cf8;
  border-color: #818cf8;
}

.icon-btn.delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #f87171;
  border-color: #f87171;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
  color: var(--text-dim);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 20px;
  opacity: 0.3;
}

.empty-state .primary-btn {
  display: inline-block;
  margin-top: 20px;
  text-decoration: none;
}

/* Modal Styles - Shared from History.vue */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40px;
}

.modal-content {
  width: 100%;
  max-width: 500px;
  background: #0f172a;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 { font-size: 20px; margin: 0; }
.header-left p { color: var(--text-dim); margin: 4px 0 0; font-size: 13px; }

.close-btn { background: none; border: none; color: var(--text-dim); font-size: 20px; cursor: pointer; }

.modal-body { padding: 24px; }

.modal-footer {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.profile-form { display: flex; flex-direction: column; gap: 16px; }

.form-group-custom { display: flex; flex-direction: column; gap: 6px; }
.form-group-custom label { font-size: 12px; color: var(--text-dim); text-transform: uppercase; }
.form-group-custom input, .custom-textarea {
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  color: white;
  width: 100%;
}

.primary-btn {
  padding: 12px 24px;
  background: var(--primary-color);
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.outline-btn {
  padding: 12px 24px;
  background: none;
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  color: white;
  cursor: pointer;
}

.error-msg { color: #f87171; font-size: 13px; margin-top: 10px; }
.success-msg { color: #10b981; font-size: 13px; margin-top: 10px; }

.loader {
  width: 18px; height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
