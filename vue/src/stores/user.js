import {defineStore} from 'pinia'
import router, {setRoutes} from "@/router";

/**
 * pinia的使用
 * */

export const useUserStore = defineStore('manager', {
    state: () => ({
        managerInfo: {},
        currentPathName: ''
    }),
    getters: {
        getUserId() {
            return this.managerInfo.user ? this.managerInfo.user.id : 0
        },
        getUser() {
            return this.managerInfo.user || {}
        },
        getBearerToken() {
            return this.managerInfo.token ? 'Bearer ' + this.managerInfo.token : ''
        },
        getToken() {
            return this.managerInfo.token || ""
        },
        getMenus() {
            return this.managerInfo.menus || []
        },
        getAuths() {
            return this.managerInfo.auths.length ? this.managerInfo.auths.map(v => v.auth) : []
        },
        getPathName() {
            return this.currentPathName || ""
        }
    },
    actions: {
        setManagerInfo(managerInfo) {
            this.managerInfo = managerInfo
            // 设置路由
            setRoutes(managerInfo.menus)
        },
        setUser(user) {
            this.managerInfo.user = JSON.parse(JSON.stringify(user)) // 变成独立的user，和原来的对象不相关
        },
        logout() {
            localStorage.removeItem('manager')
            router.push('/login')
        },
        setPath () {
            this.currentPathName = localStorage.getItem("currentPathName")
        },
    },
    // 开启数据持久化
    persist: true
})
