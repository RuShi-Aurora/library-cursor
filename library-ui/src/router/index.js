import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/books',
      name: 'Books',
      component: () => import('../views/Books.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/books/add',
      name: 'AddBook',
      component: () => import('../views/AddBook.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/books/:id',
      name: 'BookDetail',
      component: () => import('../views/BookDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('../views/Admin.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/users',
      name: 'UserManagement',
      component: () => import('../views/users/UserList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/borrows',
      name: 'BorrowManagement',
      component: () => import('../views/borrows/BorrowList.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/maintenance',
      name: 'SystemMaintenance',
      component: () => import('../views/admin/Maintenance.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/NotFound.vue')
    }
  ]
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果需要认证且未登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } 
  // 如果需要管理员权限但不是管理员
  else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Home' })
  }
  // 如果已登录且访问登录页
  else if (to.name === 'Login' && userStore.isLoggedIn) {
    next({ name: 'Home' })
  }
  // 其他情况正常访问
  else {
    next()
  }
})

export default router 