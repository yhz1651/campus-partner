import {createRouter, createWebHistory} from 'vue-router'
import {useUserStore} from "@/stores/user"

const modules = import.meta.glob('../views/*.vue')

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'Layout',
            redirect: '/home',
            component: () => import('../layout/Layout.vue'),
            children: [
                {path: 'home', name: 'Home', component: () => import('../views/Home.vue')},
                {path: 'person', name: 'Person', component: () => import('../views/Person.vue')},
                {path: 'password', name: 'Password', component: () => import('../views/Password.vue')},
                {path: 'myMessage', name: 'MyMessage', component: () => import('../views/MyMessage.vue')},
            ]
        },
        {
            path: '/login',
            name: 'Login',
            component: () => import('../views/Login.vue')
        },
        {
            path: '/register',
            name: 'Register',
            component: () => import('../views/Register.vue')
        },
        {
            path: '/404',
            name: '404',
            component: () => import('../views/404.vue')
        },
        // 前台页面路由
        {
            path: '/front',
            name: 'Front',
            redirect: '/front/home',
            component: () => import('../layout/Front.vue'),
            children: [
                {path: 'home', name: 'FrontHome', component: () => import('../views/front/Home.vue')},
                {path: 'person', name: 'FrontPerson', component: () => import('../views/front/Person.vue')},
                {path: 'password', name: 'FrontPassword', component: () => import('../views/front/Password.vue')},
                {path: 'activity', name: 'FrontActivity', component: () => import('../views/front/Activity.vue')},
                {path: 'chatRoom', name: 'FrontChatRoom', component: () => import('../views/front/ChatRoom.vue')},
                {path: 'dynamic', name: 'FrontDynamic', component: () => import('../views/front/Dynamic.vue')},
                {
                    path: 'dynamicDetail',
                    name: 'FrontDynamicDetail',
                    component: () => import('../views/front/DynamicDetail.vue')
                },
                {path: 'infoDetail', name: 'FrontInfoDetail', component: () => import('../views/front/InfoDetail.vue')},
                {path: 'myDynamic', name: 'FrontMyDynamic', component: () => import('../views/front/MyDynamic.vue')},
                {path: 'myReserve', name: 'FrontMyReserve', component: () => import('../views/front/MyReserve.vue')},
                {path: 'myCollect', name: 'FrontMyCollect', component: () => import('../views/front/MyCollect.vue')},
                {path: 'myFollow', name: 'FrontMyFollow', component: () => import('../views/front/MyFollow.vue')},
                {path: 'myFans', name: 'FrontMyFans', component: () => import('../views/front/MyFans.vue')},
                {path: 'myMessage', name: 'FrontMyMessage', component: () => import('../views/front/MyMessage.vue')},
                {path: 'info', name: 'FrontInfo', component: () => import('../views/front/Info.vue')},
                {path: 'user', name: 'FrontUser', component: () => import('../views/front/User.vue')},
                {path: 'userDetail', name: 'FrontUserDetail', component: () => import('../views/front/UserDetail.vue')},
            ]
        }
    ]
})

// 注意：刷新页面会导致页面路由重置
export const setRoutes = (menus) => {
    if (!menus || !menus.length) {
        const manager = localStorage.getItem('manager')
        if (!manager) {
            return
        }
        menus = JSON.parse(manager).managerInfo.menus
    }

    if (menus.length) {
        // 开始渲染 未来的不确定的  用户添加的路由
        menus.forEach(item => {   // 所有的页面都需要设置路由，而目录不需要设置路由
            if (item.path) {  // 当且仅当path不为空的时候才去设置路由
                router.addRoute('Layout', {
                    path: item.path,
                    name: item.page,
                    component: modules['../views/' + item.page + '.vue']
                })
            } else {
                if (item.children && item.children.length) {
                    item.children.forEach(sub => {
                        if (sub.path) {
                            router.addRoute('Layout', {
                                path: sub.path,
                                name: sub.page,
                                component: modules['../views/' + sub.page + '.vue']
                            })
                        }
                    })
                }
            }
        })
    }
}

setRoutes()


// 路由守卫
router.beforeEach((to, from, next) => {
        const store = useUserStore()  // 拿到用户对象信息
        const user = store.managerInfo.user
        const hasUser = user && user.id
        const noPermissionPaths = ['/login', '/register', '/404']   // 定义无需登录的路由
        if (!hasUser && !noPermissionPaths.includes(to.path)) {  // 用户没登录并且跳转的界面不是login ，否则发生无限循环跳转
            // 获取缓存的用户数据
            // 如果to.path === '/login' 的时候   !noPermissionPaths.includes(to.path) 是返回 false 的，也就不会进 next("/login")
            next("/login")
        } else if (hasUser && user.role === 'USER' && to.path === '/home') { // 用户角色不能访问后台
            next("/front/home")
        } else {
            if (!to.matched.length) {
                next('/404')
            } else {
                next()
            }
        }
    }
)

export default router
