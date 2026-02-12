<template>
  <div class="history-page">
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
          <router-link to="/"><i class="fas fa-file-invoice"></i> {{ $t('sidebar.docGen') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/history' }">
          <router-link to="/history"><i class="fas fa-history"></i> {{ $t('sidebar.history') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/lab' }">
          <router-link to="/lab"><i class="fas fa-flask"></i> {{ $t('sidebar.lab') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/templates' }">
          <router-link to="/templates"><i class="fas fa-layer-group"></i> {{ $t('sidebar.templates') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/guide' }">
          <router-link to="/guide"><i class="fas fa-question-circle"></i> {{ $t('sidebar.guide') }}</router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="upgrade-link" @click="router.push('/upgrade')" :title="$t('sidebar.upgrade')">
          <i class="fas fa-shopping-cart"></i>
          <span>{{ $t('sidebar.upgrade') }}</span>
        </div>
        <div class="user-profile" @click="router.push('/profile')">
          <div class="user-avatar">{{ userAbbr }}</div>
          <div class="user-info">
            <div class="name-row">
              <span class="name">{{ currentUser }}</span>
              <span v-if="isVip" class="vip-badge">VIP</span>
            </div>
            <span class="role">{{ isVip ? $t('profile.premium') : $t('profile.shipper') }}</span>
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <i class="fas fa-sign-out-alt"></i> {{ $t('sidebar.logout') }}
        </button>
      </div>
    </nav>
    
    <!-- Profile Edit Modal Removed (Moved to Profile page) -->
    
    <main class="main-content">
      <header class="content-header">
        <div class="header-text">
          <h1>{{ $t('history.title') }}</h1>
          <p>{{ $t('history.subtitle') }}</p>
        </div>
        <LanguageSwitcher />
      </header>
      
      <section class="table-section card">
        <div class="table-filters">
          <div class="search-box">
            <i class="fas fa-search search-icon"></i>
            <input v-model="searchQuery" @input="fetchRecords" :placeholder="$t('history.searchPlaceholder')">
          </div>
        </div>
        
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>{{ $t('history.columns.bookingNo') }}</th>
                <th>{{ $t('history.columns.blNo') }}</th>
                <th>{{ $t('history.columns.docNo') }}</th>
                <th>{{ $t('history.columns.vessel') }}</th>
                <th>{{ $t('history.columns.weight') }}</th>
                <th>{{ $t('history.columns.volume') }}</th>
                <th>{{ $t('history.columns.package') }}</th>
                <th>{{ $t('history.columns.createdBy') }}</th>
                <th>{{ $t('history.columns.createdAt') }}</th>
                <th>{{ $t('history.columns.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in records" :key="record.id">
                <td>{{ record.bookingNo }}</td>
                <td>{{ record.blNo }}</td>
                <td>{{ record.docNo }}</td>
                <td>{{ record.vesselVoyage }}</td>
                <td>{{ record.grossWeightKgs }}</td>
                <td>{{ record.measurementCbm }}</td>
                <td>{{ record.packageQuantity }} {{ record.packageUnit }}</td>
                <td>{{ record.createBy }}</td>
                <td class="dim">{{ record.createdAt }}</td>
                <td>
                  <div class="row-actions">
                    <button @click="editRecord(record)" class="icon-btn edit-btn" :title="$t('common.edit')">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button @click="exportPdf(record)" class="icon-btn export-btn" :title="$t('common.export')">
                      <i class="fas fa-file-export"></i>
                    </button>
                    <button @click="handleDelete(record)" class="icon-btn delete-btn" :title="$t('common.delete')">
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="records.length === 0">
                <td colspan="11" class="empty-state">No records found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
      
      <Pagination
        v-if="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchRecords"
      />
    </main>
  </div>
</template>

<script setup>
// @author Richard
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import api from '../api/request'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'
import Pagination from '../components/Pagination.vue'

const router = useRouter()
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
const userAbbr = ref('Loading...')
const isVip = ref(false)

const records = ref([])
const total = ref(0)
const searchQuery = ref('')
const queryParams = ref({
  pageNum: 1,
  pageSize: 10
})

const fetchUserData = async () => {
  try {
    const res = await api.get('/client-api/current-user')
    if (res.code === 200 || res.code === 0) {
      userAbbr.value = res.data.companyAbbr
      isVip.value = res.data.vipStatus === '1'
      profileForm.value.companyName = res.data.companyName
      profileForm.value.companyAbbr = res.data.companyAbbr
    }
  } catch (err) {
    console.error('Failed to fetch user data:', err)
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

const fetchRecords = async () => {
  try {
    const res = await api.get('/client-api/list', {
      params: { 
        blNo: searchQuery.value,
        pageNum: queryParams.value.pageNum,
        pageSize: queryParams.value.pageSize
      }
    })
    records.value = res.rows || []
    total.value = res.total || 0
  } catch (err) {
    console.error('Fetch failed:', err)
  }
}


const editRecord = (record) => {
  router.push(`/edit/${record.blNo}`)
}

const exportPdf = async (record) => {
  try {
    // 构造导出 DTO
    const dto = {
      businessData: record,
      uuid: '' // 历史记录通常不需要 UUID，直接使用记录数据
    }
    
    const response = await api.post('/client-api/export-pdf', dto, {
      responseType: 'blob'
    })
    
    // 下载文件
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `BL_${record.bookingNo}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    alert('Export failed: ' + err.message)
  }
}

const handleDelete = async (record) => {
  if (!confirm(`Are you sure you want to delete record ${record.bookingNo}?`)) {
    return
  }
  
  try {
    await api.post(`/client-api/remove/${record.id}`)
    alert('Record deleted successfully')
    fetchRecords()
  } catch (err) {
    alert('Delete failed: ' + err.message)
  }
}

onMounted(async () => {
  fetchRecords()
  await fetchUserData()
})
</script>

<style scoped>
.history-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-light);
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

.content-header {
  margin-bottom: 40px;
}

.content-header h1 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.content-header p {
  color: var(--text-dim);
  font-size: 16px;
}

/* Card */
.card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  box-shadow: var(--shadow-sm);
  padding: 32px;
}

.table-filters {
  margin-bottom: 24px;
}

.search-box {
  position: relative;
  max-width: 400px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-dim);
}

.search-box input {
  width: 100%;
  padding: 12px 14px 12px 42px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-main);
  font-size: 14px;
  transition: all 0.2s;
}

.search-box input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

/* Table */
.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  padding: 12px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-color);
}

td {
  padding: 16px;
  font-size: 14px;
  color: var(--text-main);
  border-bottom: 1px solid #f1f5f9;
}

.bold {
  font-weight: 600;
}

.dim {
  color: var(--text-dim);
}

.empty-state {
  text-align: center;
  padding: 48px;
  color: var(--text-dim);
}

.row-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-dim);
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f8fafc;
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.export-btn:hover {
  color: #16a34a;
  border-color: #16a34a;
}

.delete-btn:hover {
  color: #ef4444;
  border-color: #ef4444;
}

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

.modal-content {
  width: 100%;
  max-width: 1100px;
  max-height: 90vh;
  background: white;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.profile-modal {
  max-width: 500px !important;
}

.modal-header {
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0;
}

.header-left p {
  color: var(--text-dim);
  margin: 4px 0 0 0;
  font-size: 14px;
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 20px;
  cursor: pointer;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

.modal-footer {
  padding: 20px 32px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Buttons */
.primary-btn {
  padding: 10px 20px;
  background: var(--primary-color);
  border: none;
  border-radius: 10px;
  color: white;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.outline-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid var(--border-color);
  color: var(--text-main);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.primary-btn:hover { background: #2563eb; transform: translateY(-1px); }
.outline-btn:hover { background: #f8fafc; border-color: #cbd5e1; }

.loader {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: var(--primary-color);
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

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
</style>
