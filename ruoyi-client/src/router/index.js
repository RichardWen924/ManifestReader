// @author Richard
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import History from '../views/History.vue'
import Edit from '../views/Edit.vue'
import FreightTemplateLab from '../views/FreightTemplateLab.vue'
import TemplateManagement from '../views/TemplateManagement.vue'
import Guide from '../views/Guide.vue'
import Upgrade from '../views/Upgrade.vue'

import Profile from '../views/Profile.vue'

const routes = [
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/', component: Dashboard, meta: { requiresAuth: true } },
    { path: '/history', component: History, meta: { requiresAuth: true } },
    { path: '/edit/:blNo', component: Edit, meta: { requiresAuth: true } },
    { path: '/lab', component: FreightTemplateLab, meta: { requiresAuth: true } },
    { path: '/templates', component: TemplateManagement, meta: { requiresAuth: true } },
    { path: '/guide', component: Guide, meta: { requiresAuth: true } },
    { path: '/upgrade', component: Upgrade, meta: { requiresAuth: true } },
    { path: '/profile', component: Profile, meta: { requiresAuth: true } }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const isAuthenticated = localStorage.getItem('client_user')
    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login')
    } else {
        next()
    }
})

export default router
