<template>
  <div class="dashboard-page">
    <!-- Sidebar -->
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
        <div class="user-profile active" @click="router.push('/profile')">
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

    <main class="main-content">
      <header class="content-header" style="display: flex; justify-content: space-between; align-items: flex-start;">
        <div>
          <h1>{{ $t('profilePage.title') }}</h1>
          <p>{{ $t('profilePage.subtitle') }}</p>
        </div>
        <LanguageSwitcher />
      </header>

      <section class="profile-layout">
        <div class="profile-main">
          <!-- User Info Cards -->
          <div class="info-grid">
            <div class="card glass-card info-card">
              <div class="card-header">
                <h3><i class="fas fa-user-circle"></i> {{ $t('profilePage.basicInfo') }}</h3>
                <button class="edit-profile-btn" @click="openEditModal">
                  <i class="fas fa-edit"></i> {{ $t('profilePage.editProfile') }}
                </button>
              </div>
              <div class="info-content">
                <div class="info-item">
                  <label>{{ $t('profilePage.companyName') }}</label>
                  <span>{{ userData.companyName || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('profilePage.companyCode') }}</label>
                  <span>{{ userData.companyCode || '-' }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('profilePage.shiplineAbbr') }}</label>
                  <span class="abbr-tag">{{ userData.companyAbbr || '-' }}</span>
                </div>
              </div>
            </div>

            <div class="card glass-card membership-card">
              <div class="card-header">
                <h3><i class="fas fa-gem"></i> {{ $t('profilePage.membershipPlan') }}</h3>
                <span :class="['plan-badge', isVip ? 'premium' : 'free']">
                  {{ isVip ? 'PREMIUM' : 'FREE PLAN' }}
                </span>
              </div>
              <div class="info-content">
                <div class="info-item">
                  <label>{{ $t('profilePage.packageType') }}</label>
                  <span class="highlight">{{ userData.packageType || $t('profilePage.freeEdition') }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('profilePage.expiryDate') }}</label>
                  <span>{{ formatTime(userData.expiryDate) || $t('profilePage.noExpiration') }}</span>
                </div>
                <div class="membership-cta" v-if="!isVip">
                  <p>{{ $t('profilePage.upgradeHint') }}</p>
                  <button class="primary-btn" @click="router.push('/upgrade')">{{ $t('upgrade.upgradeNow') }}</button>
                </div>
              </div>
            </div>
          </div>

          <!-- Quota Section -->
          <div class="card glass-card quota-card">
            <h3><i class="fas fa-chart-pie"></i> {{ $t('profilePage.usageQuotas') }}</h3>
            <div class="quota-grid">
              <div class="quota-item">
                <div class="quota-info">
                  <div class="quota-text">
                    <label>{{ $t('profilePage.exportQuota') }}</label>
                    <span class="quota-count">{{ isVip ? $t('profilePage.unlimited') : (QUOTA_LIMIT - userData.dataCount) + ' / ' + QUOTA_LIMIT }}</span>
                  </div>
                  <div class="quota-progress-bg">
                    <div class="quota-progress-bar" :style="{ width: isVip ? '100%' : (userData.dataCount / QUOTA_LIMIT * 100) + '%' }"></div>
                  </div>
                </div>
                <p class="quota-hint">{{ $t('profilePage.exportHint') }}</p>
              </div>

              <div class="quota-item">
                <div class="quota-info">
                  <div class="quota-text">
                    <label>{{ $t('profilePage.templateQuota') }}</label>
                    <span class="quota-count">{{ isVip ? $t('profilePage.unlimited') : (TEMPLATE_LIMIT - templateCount) + ' / ' + TEMPLATE_LIMIT }}</span>
                  </div>
                  <div class="quota-progress-bg template">
                    <div class="quota-progress-bar template" :style="{ width: isVip ? '100%' : (templateCount / TEMPLATE_LIMIT * 100) + '%' }"></div>
                  </div>
                </div>
                <p class="quota-hint">{{ $t('profilePage.templateHint') }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Profile Edit Modal (Reusing existing logic) -->
      <div v-if="isProfileModalOpen" class="modal-overlay" @click.self="closeProfileModal">
        <div class="modal-content profile-modal card">
          <header class="modal-header">
            <div class="header-left">
              <h2>{{ $t('profilePage.editModal.title') }}</h2>
              <p>{{ $t('profilePage.editModal.subtitle') }}</p>
            </div>
            <button @click="closeProfileModal" class="close-btn"><i class="fas fa-times"></i></button>
          </header>

          <div class="modal-body">
            <form @submit.prevent="handleUpdateProfile" class="profile-form">
              <div class="form-group-custom">
                <label>{{ $t('profilePage.companyName') }}</label>
                <input v-model="profileForm.companyName" :placeholder="$t('profilePage.companyName')">
              </div>

              <div class="form-group-custom">
                <label>{{ $t('profilePage.shiplineAbbr') }} (4 Abbr)</label>
                <input v-model="profileForm.companyAbbr" placeholder="e.g. MSKU" maxlength="4">
              </div>

              <div class="form-group-custom">
                <label>{{ $t('profilePage.editModal.oldPassword') }}</label>
                <input v-model="profileForm.oldPassword" type="password" :placeholder="$t('profilePage.editModal.oldPassword')">
              </div>

              <div class="form-group-custom">
                <label>{{ $t('profilePage.editModal.newPassword') }}</label>
                <input v-model="profileForm.password" type="password" :placeholder="$t('profilePage.editModal.newPassword')">
              </div>

              <div class="form-group-custom">
                <label>{{ $t('profilePage.editModal.confirmPassword') }}</label>
                <input v-model="profileForm.confirmPassword" type="password" :placeholder="$t('profilePage.editModal.confirmPassword')">
              </div>
            </form>
          </div>

          <footer class="modal-footer">
            <button @click="closeProfileModal" class="outline-btn">{{ $t('profilePage.editModal.cancel') }}</button>
            <button @click="handleUpdateProfile" :disabled="profileLoading" class="primary-btn">
              <span v-if="!profileLoading">{{ $t('profilePage.editModal.save') }}</span>
              <span v-else class="loader"></span>
            </button>
          </footer>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
// @author Richard
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/request'
import { listTemplate } from '../api/template'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const router = useRouter()
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
const userAbbr = ref('...')
const isVip = ref(false)
const userData = ref({})
const templateCount = ref(0)
const QUOTA_LIMIT = 4
const TEMPLATE_LIMIT = 2

// Profile Edit Modal States
const isProfileModalOpen = ref(false)
const profileLoading = ref(false)
const profileForm = ref({
  companyName: '',
  companyAbbr: '',
  oldPassword: '',
  password: '',
  confirmPassword: ''
})

const fetchUserData = async () => {
  try {
    const res = await api.get('/client-api/current-user')
    if (res.code === 200 || res.code === 0) {
      userData.value = res.data
      userAbbr.value = res.data.companyAbbr
      isVip.value = res.data.vipStatus === '1'
      profileForm.value.companyName = res.data.companyName
      profileForm.value.companyAbbr = res.data.companyAbbr
    }
  } catch (err) {
    console.error('Failed to fetch user data:', err)
  }
}

const fetchTemplateCount = async () => {
  try {
    const res = await listTemplate({})
    if (res.code === 200 || res.code === 0) {
      templateCount.value = res.total !== undefined ? res.total : (res.rows ? res.rows.length : 0)
    }
  } catch (err) {
    console.error('Failed to fetch template count:', err)
  }
}

const openEditModal = () => {
  isProfileModalOpen.value = true
}

const closeProfileModal = () => {
  isProfileModalOpen.value = false
}

const handleUpdateProfile = async () => {
  if (profileForm.value.password) {
    if (!profileForm.value.oldPassword) {
      alert(t('profilePage.editModal.requiredOld'))
      return
    }
    if (profileForm.value.password !== profileForm.value.confirmPassword) {
      alert(t('profilePage.editModal.mismatch'))
      return
    }
  }

  profileLoading.value = true
  try {
    const res = await api.post('/client-api/update-profile', {
      companyName: profileForm.value.companyName,
      companyAbbr: profileForm.value.companyAbbr,
      oldPassword: profileForm.value.oldPassword,
      password: profileForm.value.password
    })
    if (res.code === 200 || res.code === 0) {
      alert(t('profilePage.editModal.success'))
      fetchUserData()
      closeProfileModal()
    } else {
      alert(res.msg || t('profilePage.editModal.updateFailed'))
    }
  } catch (err) {
    alert(err.message || t('profilePage.editModal.networkError'))
  } finally {
    profileLoading.value = false
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

const formatTime = (time) => {
  if (!time) return null
  const date = new Date(time)
  return date.toLocaleString()
}

onMounted(async () => {
  try {
    await api.get('/client-api/check-auth')
    await fetchUserData()
    await fetchTemplateCount()
  } catch (err) {
    router.push('/login')
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-light);
}

/* Sidebar (Common across views) */
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

.nav-links li.active a {
  background: #eff6ff;
  color: var(--primary-color);
  font-weight: 600;
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
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-profile.active {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
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

.vip-badge {
  padding: 2px 6px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border-radius: 4px;
  color: white;
  font-size: 9px;
  font-weight: 800;
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
}

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 48px 56px;
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
}

.content-header p {
  color: var(--text-dim);
  font-size: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.glass-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 10px;
}

.edit-profile-btn {
  background: #eff6ff;
  border: none;
  padding: 8px 16px;
  border-radius: 10px;
  color: var(--primary-color);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-profile-btn:hover { background: #dbeafe; }

.info-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-dim);
  text-transform: uppercase;
}

.info-item span {
  font-size: 15px;
  color: var(--text-main);
  font-weight: 600;
}

.abbr-tag {
  display: inline-block;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  font-family: monospace;
}

.plan-badge {
  font-size: 11px;
  font-weight: 800;
  padding: 4px 12px;
  border-radius: 20px;
}

.plan-badge.premium { background: #fef3c7; color: #d97706; }
.plan-badge.free { background: #f1f5f9; color: #64748b; }

.highlight { color: var(--primary-color) !important; font-weight: 800 !important; }

.membership-cta {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.membership-cta p {
  font-size: 13px;
  color: var(--text-dim);
  margin-bottom: 12px;
}

/* Quota Styles */
.quota-card h3 { margin-bottom: 24px; }

.quota-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.quota-info {
  margin-bottom: 12px;
}

.quota-text {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 8px;
}

.quota-text label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
}

.quota-count {
  font-size: 16px;
  font-weight: 800;
  color: var(--primary-color);
}

.quota-progress-bg {
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
}

.quota-progress-bar {
  height: 100%;
  background: var(--primary-gradient);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.quota-progress-bg.template .quota-progress-bar {
  background: linear-gradient(90deg, #10b981, #059669);
}

.quota-hint {
  font-size: 12px;
  color: var(--text-dim);
}

/* Modal Repurposed Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 24px;
  width: 100%;
  max-width: 500px;
  overflow: hidden;
}

.modal-header {
  padding: 24px 32px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-body { padding: 32px; }
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
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.modal-footer {
  padding: 24px 32px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-btn {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}

.outline-btn {
  background: white;
  border: 1px solid #e2e8f0;
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}
</style>
